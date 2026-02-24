"""
AI 에이전트 서비스 (고도화 버전)
- 카테고리별 모델 티어링 (Local, Flash, Pro)
- 지식 베이스(RAG) 연동
- 한국어 주석 및 설명 포함
"""
import time
import json
import torch
import re
import httpx
from typing import Dict, Any, List, Optional
from google import genai
from transformers import pipeline
from sqlalchemy.orm import Session

from app.core.config import settings
from app.core.document_processor import document_processor
from app.core.categories import detect_category, CATEGORIES, ModelTier
from app.models.knowledge import KnowledgeBase

class AIAgentService:
    def __init__(self):
        self.cloud_available = False
        try:
            if settings.gemini_api_key:
                self.client = genai.Client(api_key=settings.gemini_api_key)
                self.cloud_available = True
            else:
                print("⚠️ GEMINI_API_KEY가 설정되지 않았습니다.")
        except Exception as e:
            print(f"⚠️ Gemini 초기화 에러: {e}")
        
        self.local_pipeline = None
        self.local_model_id = "TinyLlama/TinyLlama-1.1B-Chat-v1.0"
        self.ollama_base_url = "http://localhost:11434"
        self.request_history = []

    async def _call_ollama(self, model: str, prompt: str) -> Optional[Dict[str, Any]]:
        """Ollama API를 통한 로컬 모델(Phi-4 등) 호출"""
        print(f"🦙 [Mode: Ollama] Requesting {model}...")
        try:
            async with httpx.AsyncClient(timeout=60.0) as client:
                response = await client.post(
                    f"{self.ollama_base_url}/api/generate",
                    json={
                        "model": model,
                        "prompt": prompt,
                        "stream": False,
                        "options": {
                            "temperature": 0.2,
                            "top_p": 0.9
                        }
                    }
                )
                if response.status_code == 200:
                    response_text = response.json().get("response", "")
                    return self._parse_json(response_text)
                return None
        except Exception as e:
            print(f"⚠️ Ollama 연결 실패 ({model}): {e}")
            return None

    def _check_rate_limit(self) -> bool:
        """Gemini API 호출 제한 체크 (분당 14회). 소진 시 False → Ollama 폴백"""
        current_time = time.time()
        self.request_history = [t for t in self.request_history if current_time - t < 60]
        return len(self.request_history) < 14

    def _is_quota_exhausted(self, e: Exception) -> bool:
        """Gemini 할당량/쿼터 소진 에러 여부 (429, 403 등)"""
        msg = str(e).lower()
        return "429" in msg or "quota" in msg or "resource exhausted" in msg or "rate limit" in msg

    async def analyze_document(self, text: str, db: Optional[Session] = None, service_db: Optional[Session] = None, options: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
        """
        문서 분석 메인 파이프라인 (RAG 고도화 버전)
        
        Args:
            text: 분석할 텍스트
            db: AI DB 세션 (quantum_ai) - 상관관계 규칙 등
            service_db: Service DB 세션 (quantum_service) - knowledge_base RAG 조회용 (READ ONLY)
            options: 분석 옵션 (main_category, sub_category, render_type 등)
        """
        # 1. 카테고리 결정 (사용자 입력 우선, 없으면 자동 감지)
        main_cat = options.get("main_category")
        sub_cat = options.get("sub_category")
        
        if main_cat and sub_cat:
            category = f"{main_cat}_{sub_cat}"
        else:
            category = detect_category(text)
            
        cat_info = CATEGORIES.get(category, CATEGORIES["GENERAL_DOC"])
        
        # 2. 고도화된 지식 베이스(RAG) 조회 - Service DB에서 읽기 전용
        knowledge_context = ""
        knowledge_items = []
        if service_db:
            # knowledge_base 테이블은 quantum_service DB에 위치 (Java Admin WAS가 관리)
            # 모든 티어에서 지식 베이스를 활용할 수 있도록 확장 (기존 PRO 전용에서 변경)
            # 최신순으로 상위 5개의 관련 지식 추출
            knowledge_items = service_db.query(KnowledgeBase).filter(
                KnowledgeBase.category.like(f"{main_cat}%") if main_cat else KnowledgeBase.category == category,
                KnowledgeBase.is_active == True
            ).order_by(KnowledgeBase.updated_at.desc()).limit(5).all()
            
            if knowledge_items:
                knowledge_context = "\n[중요 지식 베이스 및 시각화 규칙]\n"
                for k in knowledge_items:
                    # 지식의 출처와 내용을 구조화하여 주입
                    knowledge_context += f"- [{k.title}]: {k.content} (출처: {k.source_url or '내부 지식'})\n"

        # 3. 분석 수행 (티어별 모델 선택)
        result = await self._call_ai_with_tier(text, category, knowledge_context, options)
        
        # 결과에 카테고리 정보 및 RAG 활용 여부 추가
        result["detected_category"] = category
        result["model_tier"] = cat_info.tier.value
        result["rag_applied"] = len(knowledge_items) > 0
        return result

    async def analyze_structured_table(
        self,
        parsed_table: List[Dict[str, Any]],
        db: Optional[Session] = None,
        service_db: Optional[Session] = None,
        options: Optional[Dict[str, Any]] = None
    ) -> Dict[str, Any]:
        """
        파싱된 테이블 데이터를 AI가 분석 (요약, 키워드, 관계 추출).
        시각화는 파서 결과를 사용하고, AI는 인사이트 보강용.
        """
        # 구조화된 데이터를 JSON 문자열로 (상위 30행만, 토큰 절약)
        sample = parsed_table[:30] if len(parsed_table) > 30 else parsed_table
        data_str = json.dumps(sample, ensure_ascii=False, indent=2)
        
        options = options or {}
        main_cat = options.get("main_category")
        sub_cat = options.get("sub_category")
        category = f"{main_cat}_{sub_cat}" if (main_cat and sub_cat) else "GENERAL_DOC"
        cat_info = CATEGORIES.get(category, CATEGORIES["GENERAL_DOC"])
        
        knowledge_context = ""
        if service_db and main_cat:
            knowledge_items = service_db.query(KnowledgeBase).filter(
                KnowledgeBase.category.like(f"{main_cat}%"),
                KnowledgeBase.is_active == True
            ).order_by(KnowledgeBase.updated_at.desc()).limit(3).all()
            if knowledge_items:
                knowledge_context = "\n[참고 지식]\n" + "\n".join(f"- {k.title}: {k.content[:200]}" for k in knowledge_items)
        
        prompt = f"""다음은 이미 구조화된 테이블 데이터입니다. 파싱은 완료되었으므로, 데이터를 분석하여 인사이트를 추출하세요.

[출력 형식] JSON만 출력:
{{
    "summary": "데이터의 핵심 요약 (한국어, 1~2문장)",
    "keywords": [{{"term": "핵심 키워드", "value": "대표값", "definition": "해석", "importance": 1-10}}],
    "relations": [{{"source": "항목A", "target": "항목B", "label": "관계", "strength": 1-10}}],
    "suggested_2d_viz": "table" 또는 "card" 또는 "chart" 또는 "network"
}}
suggested_2d_viz 규칙: 수치(금액·기온 등) 많으면 chart, 관계/구조가 있으면 network, 일반 텍스트/판례/소스는 table, 키워드 중심이면 card.

{knowledge_context}

[구조화된 테이블 데이터]
{data_str[:3000]}
"""
        try:
            # 1. Gemini 우선. 한도 소진 시 Ollama 폴백
            if not self.cloud_available:
                print("⚠️ Gemini API 키 미설정 → Ollama로 전환합니다.")
            elif not self._check_rate_limit():
                print("⚠️ Gemini 호출 한도 소진 (분당 14회) → Ollama로 전환합니다.")
            if self.cloud_available and self._check_rate_limit():
                try:
                    self.request_history.append(time.time())
                    model_name = "gemini-2.5-flash"
                    response = self.client.models.generate_content(model=model_name, contents=prompt)
                    if response and response.text:
                        result = self._parse_json(response.text)
                        result["detected_category"] = category
                        result["model_tier"] = "flash"
                        result["suggested_render"] = "settlement"
                        return result
                except Exception as e:
                    if self._is_quota_exhausted(e):
                        print(f"⚠️ Gemini 호출 한도 소진 → Ollama로 전환합니다.")
                    else:
                        print(f"⚠️ Gemini 실패: {e}. Ollama로 전환합니다.")
            # 2. Ollama 폴백 (Gemini 실패/미설정 시)
            if cat_info.tier in [ModelTier.FLASH, ModelTier.PRO]:
                result = await self._call_ollama("llama3.2", prompt)
                if result and (result.get("summary") or result.get("keywords")):
                    result["detected_category"] = category
                    result["model_tier"] = cat_info.tier.value
                    result["suggested_render"] = "settlement"
                    return result
            # 3. TinyLlama 최후 수단
            result = await self._run_local_model(prompt)
            result["detected_category"] = category
            result["model_tier"] = "local"
            result["suggested_render"] = "settlement"
            return result
        except Exception as e:
            print(f"⚠️ 구조화 테이블 AI 분석 실패: {e}")
            return {
                "summary": f"테이블 ({len(parsed_table)}행) — AI 분석 없이 시각화",
                "keywords": [],
                "relations": [],
                "detected_category": category,
                "model_tier": "none",
                "suggested_render": "settlement",
                "suggested_2d_viz": "table"
            }

    async def _call_ai_with_tier(self, text: str, category: str, knowledge: str, options: Dict[str, Any]) -> Dict[str, Any]:
        """카테고리 티어에 따른 모델 호출 분기"""
        cat_info = CATEGORIES[category]
        options = options or {}
        render_type = options.get("render_type", "auto")

        # 프롬프트 구성
        prompt = self._build_specialized_prompt(text, category, knowledge, render_type)

        # 1. Gemini 우선 (API 키 있으면). 한도 소진 시 Ollama 폴백
        if not self.cloud_available:
            print("⚠️ Gemini API 키 미설정 → Ollama로 전환합니다.")
        elif not self._check_rate_limit():
            print("⚠️ Gemini 호출 한도 소진 (분당 14회) → Ollama로 전환합니다.")
        elif self.cloud_available and self._check_rate_limit():
            model_name = "gemini-2.5-pro" if cat_info.tier == ModelTier.PRO else "gemini-2.5-flash"
            print(f"🚀 [Mode: Cloud] Requesting {model_name} for category {category}...")
            
            try:
                self.request_history.append(time.time())
                # Pro 모델인 경우 구글 검색(Grounding) 활용
                config = {"tools": [{"google_search": {}}]} if cat_info.tier == ModelTier.PRO else {}
                
                response = self.client.models.generate_content(
                    model=model_name,
                    contents=prompt,
                    config=config
                )
                if response and response.text:
                    return self._parse_json(response.text)
            except Exception as e:
                if self._is_quota_exhausted(e):
                    print(f"⚠️ Gemini 호출 한도 소진 → Ollama로 전환합니다.")
                else:
                    print(f"⚠️ Cloud API 실패: {e}. Ollama로 전환합니다.")
        
        # 2. Ollama 폴백 (Gemini 실패/미설정 시)
        if cat_info.tier in [ModelTier.FLASH, ModelTier.PRO]:
            ollama_result = await self._call_ollama("llama3.2", prompt)
            if ollama_result and ollama_result.get("keywords"):
                return ollama_result
        
        # 3. 최후의 수단: TinyLlama (Transformers)
        return await self._run_local_model(prompt)

    def _build_specialized_prompt(self, text: str, category: str, knowledge: str, render_type: str) -> str:
        """카테고리별 특화 프롬프트 생성 (RAG 강화 버전)"""
        cat_info = CATEGORIES[category]
        use_settlement = render_type == "settlement" or any(k in text for k in ["만원", "원", "금액", "매출", "매입", "비용"])
        suggested_render = "settlement" if use_settlement else render_type

        table_data_instruction = ""
        if use_settlement:
            table_data_instruction = """
        [차트/정산용] 숫자(금액·비율)가 있으면 반드시 "table_data" 배열을 포함하세요.
        형식: [{"항목":"이름","금액":숫자},{"항목":"이름2","금액":숫자2},...]
        예: "매출 12500만원, 매입 7200" → "table_data":[{"항목":"매출","금액":12500},{"항목":"매입","금액":7200}]
        """

        return f"""
        당신은 {cat_info.description} 분야의 시각화 전문가입니다.
        [데이터]를 분석하여 JSON으로 변환하세요.{table_data_instruction}

        [지식 베이스] {knowledge if knowledge else "해당 없음."}

        [출력 형식] JSON만 출력:
        {{
            "summary": "핵심 요약 (한국어)",
            "suggested_render": "{suggested_render}",
            "suggested_2d_viz": "table" 또는 "card" 또는 "chart" 또는 "network",
            "table_data": [{{"항목": "항목명", "금액": 숫자}}],
            "keywords": [{{"term": "키워드", "value": "값", "definition": "해석", "importance": 1-10}}],
            "relations": [{{"source": "A", "target": "B", "label": "관계", "strength": 1-10}}]
        }}
        suggested_2d_viz 규칙: 수치(금액·기온) 많으면 chart, 관계/의존성 있으면 network, 판례·소스코드·텍스트는 table, 키워드 중심이면 card.

        [데이터]
        {text[:2000]}
        """

    async def _run_local_model(self, prompt: str) -> Dict[str, Any]:
        """로컬 AI 모델(TinyLlama) 실행"""
        print("🏠 [Mode: Local] Running Internal Model...")
        try:
            if self.local_pipeline is None:
                self.local_pipeline = pipeline(
                    "text-generation", 
                    model=self.local_model_id, 
                    device_map="auto" if torch.cuda.is_available() else None,
                    torch_dtype=torch.float32
                )
            
            outputs = self.local_pipeline(prompt, max_new_tokens=512, do_sample=True, temperature=0.7)
            return self._parse_json(outputs[0]["generated_text"])
        except Exception as e:
            print(f"❌ 로컬 모델 실행 실패: {e}")
            return {"summary": "분석 실패", "keywords": [], "relations": []}

    def _parse_json(self, text: str) -> Dict[str, Any]:
        """AI 응답에서 JSON 추출 및 파싱"""
        try:
            json_str = re.search(r'\{.*\}', text, re.DOTALL).group()
            return json.loads(json_str)
        except:
            return {"summary": "데이터 파싱 에러", "keywords": [], "relations": []}

ai_agent_service = AIAgentService()
