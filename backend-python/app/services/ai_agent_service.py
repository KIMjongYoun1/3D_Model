"""
AI 에이전트 서비스 (안정화 버전)
"""
import time
import json
import torch
import re
from typing import Dict, Any, List, Optional
from google import genai
from transformers import pipeline
from app.core.config import settings
from app.core.document_processor import document_processor

class AIAgentService:
    def __init__(self):
        self.cloud_available = False
        try:
            if settings.gemini_api_key:
                # API 키가 있으면 클라이언트 생성
                self.client = genai.Client(api_key=settings.gemini_api_key)
                self.cloud_available = True
            else:
                print("⚠️ GEMINI_API_KEY가 설정되지 않았습니다.")
        except Exception as e:
            print(f"⚠️ Gemini 초기화 에러: {e}")
        
        self.local_pipeline = None
        self.local_model_id = "TinyLlama/TinyLlama-1.1B-Chat-v1.0"
        self.request_history = []

    def _check_rate_limit(self) -> bool:
        current_time = time.time()
        self.request_history = [t for t in self.request_history if current_time - t < 60]
        return len(self.request_history) < 14

    async def analyze_document(self, text: str, options: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
        """문서 분석 파이프라인"""
        chunks = document_processor.split_text(text)
        
        # 첫 번째 청크만 분석하여 속도와 안정성 확보
        chunk = chunks[0] if chunks else text
        result = await self._call_ai(chunk, options)
        return result

    async def _call_ai(self, text: str, options: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
        options = options or {}
        render_type = options.get("render_type", "auto")
        
        render_instruction = ""
        if render_type == "settlement":
            render_instruction = "이 데이터는 '정산/통계' 목적입니다. 수치 데이터 위주로 'table_data' 필드에 리스트 형태로 추출하세요."
        elif render_type == "diagram":
            render_instruction = "이 데이터는 '관계도/구조' 목적입니다. 키워드 간의 연결 관계 위주로 추출하세요."

        prompt = f"""
        당신은 데이터의 핵심 구조를 파악하고 실시간 검색을 통해 전문적인 근거를 제시하는 3D 지식 맵 전문가입니다.
        입력된 데이터를 분석하고, 필요한 경우 구글 검색을 통해 기술 용어, 법조항, 최신 사례 등의 근거를 찾아 다음 JSON 구조로 응답하세요.

        {{
            "summary": "전체 데이터에 대한 5줄 핵심 요약 (한국어)",
            "suggested_render": "{render_type}",
            "keywords": [
                {{
                    "term": "핵심 키워드",
                    "value": "실제 값/문장",
                    "definition": "기능 설명/해석",
                    "importance": 1-10,
                    "references": [
                        {{"title": "출처 명칭 (예: 법령명, 기술문서 제목)", "url": "원문 링크", "snippet": "참고한 핵심 문구 요약"}}
                    ]
                }}
            ],
            "table_data": [
                {{"항목": "값", "금액": 1000, "비고": "..."}}
            ],
            "relations": [
                {{"source": "키워드A", "target": "키워드B", "label": "관계 설명", "strength": 1-10}}
            ]
        }}

        [중요 지침]
        1. 전문 용어나 법적 근거가 필요한 경우 반드시 실시간 검색을 활용하여 'references'를 채우세요.
        2. 저작권을 존중하여 참고한 문서의 제목과 정확한 URL을 제공하세요.
        3. 모든 설명과 해석은 한국어로 작성하세요.
        4. 반드시 JSON 형식만 출력하세요.

        데이터:
        {text}
        """

        if self.cloud_available and self._check_rate_limit():
            print("🚀 [Mode: Cloud] Requesting Gemini with Google Search...")
            try:
                self.request_history.append(time.time())
                # 구글 검색(Grounding) 도구 활성화
                response = self.client.models.generate_content(
                    model="gemini-flash-latest",
                    contents=prompt,
                    config={
                        "tools": [{"google_search": {}}]
                    }
                )
                if response and response.text:
                    return self._parse_json(response.text)
            except Exception as e:
                print(f"⚠️ Cloud API 실패: {e}. 로컬 모델로 전환합니다.")
        
        return await self._run_local_model(prompt)

    async def _run_local_model(self, prompt: str) -> Dict[str, Any]:
        print("🏠 [Mode: Local] Running Internal Model...")
        try:
            if self.local_pipeline is None:
                # CPU 환경에서도 돌아가도록 최적화 설정
                self.local_pipeline = pipeline(
                    "text-generation", 
                    model=self.local_model_id, 
                    device_map="auto" if torch.cuda.is_available() else None,
                    torch_dtype=torch.float32 # 맥/윈도우 호환성을 위해 float32 사용
                )
            
            outputs = self.local_pipeline(prompt, max_new_tokens=256, do_sample=True, temperature=0.7)
            return self._parse_json(outputs[0]["generated_text"])
        except Exception as e:
            print(f"❌ 로컬 모델 실행 실패: {e}")
            return {"summary": "분석 실패", "keywords": ["Error"], "relations": []}

    def _parse_json(self, text: str) -> Dict[str, Any]:
        try:
            json_str = re.search(r'\{.*\}', text, re.DOTALL).group()
            return json.loads(json_str)
        except:
            return {"summary": "데이터 파싱 에러", "keywords": ["Parse Error"], "relations": []}

ai_agent_service = AIAgentService()
