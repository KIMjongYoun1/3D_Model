import os
from google import genai
from dotenv import load_dotenv

load_dotenv()
API_KEY = os.getenv("GEMINI_API_KEY")

def list_models():
    if not API_KEY:
        print("❌ GEMINI_API_KEY not found")
        return

    client = genai.Client(api_key=API_KEY)
    print("🔍 사용 가능한 모델 목록 조회 중...")
    
    try:
        # 모델 목록 조회
        for model in client.models.list():
            print(f"- Name: {model.name}, Supported: {model.supported_actions}")
    except Exception as e:
        print(f"❌ 목록 조회 실패: {e}")

if __name__ == "__main__":
    list_models()
