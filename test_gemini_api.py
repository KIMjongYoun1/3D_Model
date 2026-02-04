import os
from google import genai
from dotenv import load_dotenv

# .env 파일 로드
load_dotenv()

# 환경 변수에서 API 키 가져오기
API_KEY = os.getenv("GEMINI_API_KEY")

def test_gemini():
    if not API_KEY:
        print("❌ 에러: .env 파일에서 GEMINI_API_KEY를 찾을 수 없습니다.")
        return

    print(f"🔗 Gemini 서버 연결 시도 중... (Key: {API_KEY[:10]}...)")
    
    try:
        # 클라이언트 초기화
        client = genai.Client(api_key=API_KEY)
        
        # 간단한 텍스트 생성 테스트 (확인된 작동 모델: gemini-flash-latest)
        response = client.models.generate_content(
            model="gemini-flash-latest",
            contents="이 메시지가 보인다면 'API 연결 성공'이라고 답해줘."
        )
        
        print("\n" + "="*40)
        print("✅ Gemini API 호출 성공!")
        print(f"🤖 응답: {response.text.strip()}")
        print("="*40)
        
    except Exception as e:
        print("\n" + "="*40)
        print("❌ API 호출 실패")
        print(f"에러 유형: {type(e).__name__}")
        print(f"에러 메시지: {str(e)}")
        print("="*40)
        print("\n💡 해결 팁:")
        if "403" in str(e):
            print("- API 키의 권한을 확인하세요. (API restricted?)")
        elif "404" in str(e):
            print("- 모델 이름(gemini-1.5-flash)이 정확한지 확인하세요.")
        elif "API key not valid" in str(e):
            print("- API 키가 올바른지 다시 확인하세요.")

if __name__ == "__main__":
    test_gemini()
