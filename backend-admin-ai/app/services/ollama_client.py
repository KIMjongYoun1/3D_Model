"""
LLM 클라이언트
- Ollama (Llama 3.2) 우선 사용
- Ollama 연결 실패 시 Gemini API로 폴백
"""
import httpx
import json
from app.core.config import settings


class LLMClient:
    """Ollama + Gemini 폴백 LLM 클라이언트"""

    def __init__(self):
        self.ollama_url = settings.ollama_base_url
        self.ollama_model = settings.ollama_model
        self.gemini_api_key = settings.gemini_api_key
        self._ollama_available: bool | None = None

    async def check_ollama(self) -> bool:
        """Ollama 서버 상태 확인"""
        try:
            async with httpx.AsyncClient(timeout=3.0) as client:
                resp = await client.get(f"{self.ollama_url}/api/tags")
                return resp.status_code == 200
        except Exception:
            return False

    async def generate(self, prompt: str, system_prompt: str = "") -> dict:
        """
        LLM 응답 생성
        1. Ollama 시도
        2. 실패 시 Gemini 폴백
        3. 둘 다 실패 시 기본 응답
        """
        # Ollama 시도
        ollama_result = await self._call_ollama(prompt, system_prompt)
        if ollama_result is not None:
            return {"text": ollama_result, "model": f"ollama/{self.ollama_model}"}

        # Gemini 폴백
        if self.gemini_api_key:
            gemini_result = await self._call_gemini(prompt, system_prompt)
            if gemini_result is not None:
                return {"text": gemini_result, "model": "gemini-flash"}

        # 둘 다 실패
        return {
            "text": self._fallback_response(prompt),
            "model": "fallback",
        }

    async def _call_ollama(self, prompt: str, system_prompt: str = "") -> str | None:
        """Ollama API 호출"""
        try:
            payload = {
                "model": self.ollama_model,
                "prompt": prompt,
                "stream": False,
            }
            if system_prompt:
                payload["system"] = system_prompt

            async with httpx.AsyncClient(timeout=60.0) as client:
                resp = await client.post(
                    f"{self.ollama_url}/api/generate",
                    json=payload,
                )
                if resp.status_code == 200:
                    data = resp.json()
                    return data.get("response", "")
                return None
        except Exception as e:
            print(f"[Ollama] 연결 실패: {e}")
            return None

    async def _call_gemini(self, prompt: str, system_prompt: str = "") -> str | None:
        """Gemini API 폴백 호출"""
        try:
            url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key={self.gemini_api_key}"

            full_prompt = f"{system_prompt}\n\n{prompt}" if system_prompt else prompt

            payload = {
                "contents": [
                    {"parts": [{"text": full_prompt}]}
                ]
            }

            async with httpx.AsyncClient(timeout=30.0) as client:
                resp = await client.post(url, json=payload)
                if resp.status_code == 200:
                    data = resp.json()
                    candidates = data.get("candidates", [])
                    if candidates:
                        parts = candidates[0].get("content", {}).get("parts", [])
                        if parts:
                            return parts[0].get("text", "")
                return None
        except Exception as e:
            print(f"[Gemini] 호출 실패: {e}")
            return None

    def _fallback_response(self, prompt: str) -> str:
        """LLM 연결 실패 시 기본 응답"""
        return (
            "현재 AI 모델에 연결할 수 없습니다.\n\n"
            "다음을 확인해주세요:\n"
            "1. Ollama가 실행 중인지 확인: `ollama serve`\n"
            "2. 모델이 설치되어 있는지 확인: `ollama pull llama3.2`\n"
            "3. 또는 GEMINI_API_KEY 환경변수를 설정해주세요.\n\n"
            f"요청하신 프롬프트: {prompt[:100]}..."
        )


# 싱글톤 인스턴스
llm_client = LLMClient()
