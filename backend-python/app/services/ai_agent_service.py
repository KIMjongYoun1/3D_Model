"""
AI 에이전트 서비스 (안정화 버전)
"""
import time
import json
import torch
import re
from typing import Dict, Any, List
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

    async def analyze_document(self, text: str) -> Dict[str, Any]:
        """문서 분석 파이프라인"""
        chunks = document_processor.split_text(text)
        all_results = []
        
        # 첫 번째 청크만 분석하여 속도와 안정성 확보
        chunk = chunks[0] if chunks else text
        result = await self._call_ai(chunk)
        return result

    async def _call_ai(self, text: str) -> Dict[str, Any]:
        prompt = f"""
        Analyze the following text and provide a JSON response with this structure:
        {{
            "summary": "5-line summary in Korean",
            "keywords": ["keyword1", "keyword2", "keyword3", "keyword4", "keyword5"],
            "relations": [
                {{"source": "keywordA", "target": "keywordB", "label": "description"}}
            ]
        }}
        Text: {text}
        """

        if self.cloud_available and self._check_rate_limit():
            print("🚀 [Mode: Cloud] Requesting Gemini...")
            try:
                self.request_history.append(time.time())
                # 모델 이름을 'gemini-flash-latest'로 수정 (호환성 확인됨)
                response = self.client.models.generate_content(
                    model="gemini-flash-latest",
                    contents=prompt
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
