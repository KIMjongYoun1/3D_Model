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
        """API 호출 제한 체크 (분당 14회 제한)"""
        current_time = time.time()
        self.request_history = [t for t in self.request_history if current_time - t < 60]
        return len(self.request_history) < 14

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

    async def _call_ai_with_tier(self, text: str, category: str, knowledge: str, options: Dict[str, Any]) -> Dict[str, Any]:
        """카테고리 티어에 따른 모델 호출 분기"""
        cat_info = CATEGORIES[category]
        options = options or {}
        render_type = options.get("render_type", "auto")

        # 프롬프트 구성
        prompt = self._build_specialized_prompt(text, category, knowledge, render_type)

        # 1. 보안 및 비용 절감을 위해 고성능 로컬 모델(Llama 3.2) 우선 시도 (Ollama)
        if cat_info.tier in [ModelTier.FLASH, ModelTier.PRO]:
            ollama_result = await self._call_ollama("llama3.2", prompt)
            if ollama_result and ollama_result.get("keywords"):
                return ollama_result

        # 2. Cloud (Gemini) - Ollama 실패 시 또는 특정 티어에서 사용
        if self.cloud_available and self._check_rate_limit():
            model_name = "gemini-1.5-pro-latest" if cat_info.tier == ModelTier.PRO else "gemini-1.5-flash-latest"
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
                print(f"⚠️ Cloud API 실패: {e}. 로컬 모델로 전환합니다.")
        
        # 3. 최후의 수단: 초경량 로컬 모델 (TinyLlama - Transformers)
        return await self._run_local_model(prompt)

    def _build_specialized_prompt(self, text: str, category: str, knowledge: str, render_type: str) -> str:
        """카테고리별 특화 프롬프트 생성 (RAG 강화 버전)"""
        cat_info = CATEGORIES[category]
        
        return f"""
        당신은 {cat_info.description} 분야의 시각화 전문가입니다.
        제공된 [데이터]를 분석하여 3D 지식 맵(JSON)으로 변환하세요.

        [지식 베이스 및 시각화 규칙]
        {knowledge if knowledge else "관련된 특정 규칙이 없습니다. 일반적인 시각화 원칙을 따르세요."}
        
        위 지식 베이스에 명시된 규칙(색상, 관계, 중요도 등)이 있다면 반드시 결과에 반영하십시오.
        또한, 각 키워드(keyword)의 'definition' 필드 마지막에 참고한 지식의 제목을 "[출처: 제목]" 형태로 명시하세요.

        [출력 형식]
        {{
            "summary": "전체 데이터의 핵심 요약 (한국어)",
            "suggested_render": "{render_type}",
            "keywords": [
                {{
                    "term": "핵심 키워드",
                    "value": "데이터 값",
                    "definition": "전문적 해석 및 [출처: 지식제목]",
                    "importance": 1-10,
                    "color_hint": "지식 베이스에서 제안한 색상 코드 (없으면 생략)"
                }}
            ],
            "relations": [
                {{"source": "키워드A", "target": "키워드B", "label": "관계 설명", "strength": 1-10}}
            ]
        }}

        [데이터]
        {text[:2000]} # 토큰 절약을 위해 텍스트 제한
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
