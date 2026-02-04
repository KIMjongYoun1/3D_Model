import os
from google import genai
from dotenv import load_dotenv

load_dotenv()
API_KEY = os.getenv("GEMINI_API_KEY")

def find_and_test_model():
    client = genai.Client(api_key=API_KEY)
    models = [m.name for m in client.models.list()]
    
    target_models = ["models/gemini-1.5-flash", "models/gemini-flash-latest", "models/gemini-2.0-flash"]
    
    for model_name in target_models:
        # models/ 접두어 제거 버전도 시도
        short_name = model_name.replace("models/", "")
        
        for name in [model_name, short_name]:
            print(f"\n尝试 모델: {name} ...")
            try:
                response = client.models.generate_content(
                    model=name,
                    contents="Hi, are you working?"
                )
                print(f"✅ 성공! 모델: {name}")
                print(f"🤖 응답: {response.text}")
                return
            except Exception as e:
                print(f"❌ 실패 ({name}): {e}")

if __name__ == "__main__":
    find_and_test_model()
