# 🗺️ 개발 로드맵

> **버전**: v0.1 (초안)  
> **최종 수정**: 2025.11.30  
> **개발 규모**: 1인 ~ 3인

---

## 📅 전체 일정 개요

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          2025 Q4 ~ 2026 Q2                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  Phase 0        Phase 1           Phase 2           Phase 3                 │
│  ┌─────────┐    ┌─────────────┐   ┌─────────────┐   ┌─────────────┐        │
│  │ 기획/PoC │───▶│    MVP     │──▶│   개인화    │──▶│   고도화    │        │
│  │ 2~3주   │    │   8~10주    │   │   6~8주    │   │  지속 개선   │        │
│  └─────────┘    └─────────────┘   └─────────────┘   └─────────────┘        │
│                                                                              │
│  12월           1~2월              3~4월             5월~                    │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔬 Phase 0: 기획 & PoC (2~3주)

### 목표
- 핵심 AI 모델 검증
- 기술 스택 확정 (Python + Java + Node.js + Docker + PostgreSQL)
- 상세 기획 완료
- 개발 환경 구축

### 기술 스택 확정
- **Backend API**: Python (FastAPI) - AI 모델 연동용
- **Backend Service**: Java (Spring Boot) - 비즈니스 로직, 고성능 처리
- **Frontend**: Node.js (Next.js 14.x) - React 기반 웹 애플리케이션
  - **언어**: TypeScript 5.x
  - **스타일링**: TailwindCSS 3.x
  - **3D 렌더링**: Three.js 0.160+ (React Three Fiber 8.x)
  - **상태관리**: Zustand 4.x, React Query 5.x
  - **시각화 도구**:
    - **2D 이미지 표시**: Next.js `<Image>` 컴포넌트 (브라우저 네이티브)
    - **3D 아바타 뷰어**: Three.js + React Three Fiber (WebGL 기반)
    - **결과 갤러리**: CSS Grid/Flexbox 레이아웃
    - **이미지 비교**: Before/After 슬라이더 (react-compare-image 또는 커스텀)
- **컴파일/빌드**: Node.js (npm/pnpm), Docker (컨테이너화)
- **데이터베이스**: PostgreSQL 16.x
- **인프라**: Docker Compose (로컬), Docker (프로덕션)

### 체크리스트

#### Week 1: AI 모델 검증 & 기술 스택 PoC

**Day 1-2: AI 모델 검증**

**목표**: 각 AI 모델의 품질, 속도, 리소스 사용량을 검증하여 최종 모델 조합 결정

##### 로컬 테스트 환경 구축

**옵션 1: 로컬 GPU 환경 (권장 - RTX 3080 이상)**
- [ ] CUDA 설치 확인
  ```bash
  nvidia-smi  # GPU 확인
  python -c "import torch; print(torch.cuda.is_available())"  # PyTorch CUDA 확인
  ```
- [ ] Python 가상환경 생성
  ```bash
  python -m venv venv-ai-test
  # Windows
  venv-ai-test\Scripts\activate
  # Linux/Mac
  source venv-ai-test/bin/activate
  ```
- [ ] 필수 라이브러리 설치
  ```bash
  # PyTorch (CUDA 11.8)
  pip install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cu118
  
  # AI 모델 라이브러리
  pip install transformers diffusers accelerate
  pip install opencv-python pillow numpy
  
  # MediaPipe
  pip install mediapipe
  
  # SAM (Segment Anything)
  pip install git+https://github.com/facebookresearch/segment-anything.git
  ```
- [ ] 테스트 스크립트 디렉토리 생성
  ```
  ai-test/
  ├── test_idm_vton.py
  ├── test_sam.py
  ├── test_mediapipe.py
  ├── test_images/
  │   ├── person/        # 테스트용 사람 이미지
  │   └── garment/       # 테스트용 의상 이미지
  └── results/           # 결과 저장
  ```

**옵션 2: Google Colab (GPU 무료 사용)**
- [ ] Colab 노트북 생성
  - 새 노트북: https://colab.research.google.com
  - 런타임 → 런타임 유형 변경 → GPU (T4) 선택
- [ ] 필수 라이브러리 설치
  ```python
  !pip install diffusers transformers accelerate
  !pip install opencv-python pillow
  !pip install mediapipe
  !pip install git+https://github.com/facebookresearch/segment-anything.git
  ```
- [ ] Google Drive 마운트 (이미지 저장용)
  ```python
  from google.colab import drive
  drive.mount('/content/drive')
  ```

**옵션 3: Hugging Face Space (빠른 테스트)**
- [ ] Hugging Face 계정 생성
- [ ] Space에서 IDM-VTON 데모 테스트
  - https://huggingface.co/spaces/yisol/IDM-VTON
  - 샘플 이미지로 테스트
  - 결과 품질 확인

##### IDM-VTON 로컬 테스트

**테스트 스크립트 작성**
```python
# test_idm_vton.py
import torch
from diffusers import StableDiffusionPipeline
from PIL import Image
import time
import os

def test_idm_vton():
    print("=== IDM-VTON 테스트 시작 ===")
    
    # 1. 모델 로드
    print("모델 로딩 중...")
    start_time = time.time()
    
    device = "cuda" if torch.cuda.is_available() else "cpu"
    print(f"사용 장치: {device}")
    
    # IDM-VTON 모델 로드 (실제 모델 경로 확인 필요)
    pipe = StableDiffusionPipeline.from_pretrained(
        "yisol/IDM-VTON",
        torch_dtype=torch.float16 if device == "cuda" else torch.float32
    ).to(device)
    
    load_time = time.time() - start_time
    print(f"모델 로드 시간: {load_time:.2f}초")
    
    if device == "cuda":
        gpu_memory = torch.cuda.memory_allocated() / 1024**3
        print(f"GPU 메모리 사용량: {gpu_memory:.2f}GB")
    
    # 2. 테스트 이미지 로드
    person_image = Image.open("test_images/person/test_person.jpg")
    garment_image = Image.open("test_images/garment/test_garment.jpg")
    
    # 3. Try-On 실행
    print("Try-On 처리 중...")
    start_time = time.time()
    
    result = pipe(
        prompt="a person wearing a garment, high quality, realistic",
        person_image=person_image,
        garment_image=garment_image,
        num_inference_steps=30,
        guidance_scale=7.5
    )
    
    process_time = time.time() - start_time
    print(f"처리 시간: {process_time:.2f}초")
    
    # 4. 결과 저장
    os.makedirs("results", exist_ok=True)
    result.images[0].save("results/idm_vton_result.jpg")
    print("결과 저장 완료: results/idm_vton_result.jpg")
    
    # 5. 품질 평가 (주관적 - 수동 평가)
    print("\n=== 품질 평가 (수동) ===")
    print("결과 이미지를 확인하고 점수를 입력하세요:")
    print("1. 자연스러움 (1-5점): ")
    print("2. 의상 핏 (1-5점): ")
    print("3. 얼굴 보존 (1-5점): ")
    
    return {
        "load_time": load_time,
        "process_time": process_time,
        "gpu_memory_gb": gpu_memory if device == "cuda" else 0,
        "result_path": "results/idm_vton_result.jpg"
    }

if __name__ == "__main__":
    results = test_idm_vton()
    print("\n=== 테스트 결과 ===")
    for key, value in results.items():
        print(f"{key}: {value}")
```

**테스트 실행**
```bash
# 로컬 GPU 환경
cd ai-test
python test_idm_vton.py

# Colab
# 위 스크립트를 Colab 셀에 복사하여 실행
```

**측정 항목**
- [ ] 모델 로드 시간 (목표: 30초 이내)
- [ ] 처리 시간 (목표: 30초 이내)
- [ ] GPU 메모리 사용량 (목표: 24GB 이하)
- [ ] 품질 평가 (5개 이상 샘플로 테스트)
  - 자연스러움 (1-5점)
  - 의상 핏 정확도 (1-5점)
  - 얼굴 보존 품질 (1-5점)

##### SAM (Segment Anything) 테스트

**테스트 스크립트 작성**
```python
# test_sam.py
from segment_anything import sam_model_registry, SamPredictor
import cv2
import numpy as np
import time
import torch
import os

def test_sam():
    print("=== SAM 테스트 시작 ===")
    
    # 1. 모델 체크포인트 다운로드 (최초 1회)
    # https://github.com/facebookresearch/segment-anything#model-checkpoints
    # sam_vit_h_4b8939.pth 다운로드 필요
    
    checkpoint_path = "sam_vit_h_4b8939.pth"
    if not os.path.exists(checkpoint_path):
        print("모델 체크포인트를 다운로드하세요:")
        print("https://dl.fbaipublicfiles.com/segment_anything/sam_vit_h_4b8939.pth")
        return None
    
    # 2. 모델 로드
    print("SAM 모델 로딩 중...")
    device = "cuda" if torch.cuda.is_available() else "cpu"
    sam = sam_model_registry["vit_h"](checkpoint=checkpoint_path)
    sam.to(device=device)
    predictor = SamPredictor(sam)
    
    # 3. 테스트 이미지 로드
    image = cv2.imread("test_images/garment/test_garment.jpg")
    image_rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
    
    # 4. 세그멘테이션 실행
    print("세그멘테이션 처리 중...")
    start_time = time.time()
    
    predictor.set_image(image_rgb)
    
    # 포인트 기반 세그멘테이션 (이미지 중앙 클릭 가정)
    input_point = np.array([[image.shape[1]//2, image.shape[0]//2]])
    input_label = np.array([1])  # 전경 포인트
    
    masks, scores, logits = predictor.predict(
        point_coords=input_point,
        point_labels=input_label,
        multimask_output=True,
    )
    
    process_time = time.time() - start_time
    print(f"처리 시간: {process_time:.2f}초")
    
    # 5. 결과 저장 (최고 점수 마스크)
    best_mask_idx = np.argmax(scores)
    best_mask = masks[best_mask_idx]
    
    os.makedirs("results", exist_ok=True)
    mask_image = (best_mask * 255).astype(np.uint8)
    cv2.imwrite("results/sam_mask.png", mask_image)
    
    # 마스크를 원본 이미지에 적용
    masked_image = image.copy()
    masked_image[best_mask == 0] = [0, 0, 0]  # 배경 제거
    cv2.imwrite("results/sam_masked.jpg", masked_image)
    
    print("결과 저장 완료")
    print(f"마스크 품질 점수: {scores[best_mask_idx]:.3f}")
    
    return {
        "process_time": process_time,
        "mask_score": float(scores[best_mask_idx]),
        "result_path": "results/sam_mask.png"
    }

if __name__ == "__main__":
    results = test_sam()
    if results:
        print("\n=== 테스트 결과 ===")
        for key, value in results.items():
            print(f"{key}: {value}")
```

**Colab에서 SAM 테스트 (간단한 방법)**
```python
# Colab 노트북
!pip install git+https://github.com/facebookresearch/segment-anything.git
!wget https://dl.fbaipublicfiles.com/segment_anything/sam_vit_h_4b8939.pth

import torch
from segment_anything import sam_model_registry, SamPredictor
import cv2
from google.colab.patches import cv2_imshow

# 모델 로드
sam = sam_model_registry["vit_h"](checkpoint="sam_vit_h_4b8939.pth")
sam.to(device="cuda")
predictor = SamPredictor(sam)

# 이미지 업로드 및 처리
from google.colab import files
uploaded = files.upload()
image = cv2.imread(list(uploaded.keys())[0])

predictor.set_image(image)
masks, scores, logits = predictor.predict(
    point_coords=np.array([[image.shape[1]//2, image.shape[0]//2]]),
    point_labels=np.array([1]),
    multimask_output=True,
)

# 결과 시각화
cv2_imshow(masks[np.argmax(scores)] * 255)
```

##### MediaPipe Face Mesh 테스트

**테스트 스크립트 작성**
```python
# test_mediapipe.py
import cv2
import mediapipe as mp
import numpy as np
import time
import os

def test_mediapipe():
    print("=== MediaPipe Face Mesh 테스트 시작 ===")
    
    # 1. MediaPipe 초기화
    mp_face_mesh = mp.solutions.face_mesh
    face_mesh = mp_face_mesh.FaceMesh(
        static_image_mode=True,
        max_num_faces=1,
        refine_landmarks=True,
        min_detection_confidence=0.5
    )
    
    mp_drawing = mp.solutions.drawing_utils
    mp_drawing_styles = mp.solutions.drawing_styles
    
    # 2. 테스트 이미지 로드
    image = cv2.imread("test_images/person/test_face.jpg")
    image_rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
    
    # 3. 얼굴 랜드마크 추출
    print("얼굴 랜드마크 추출 중...")
    start_time = time.time()
    
    results = face_mesh.process(image_rgb)
    
    process_time = time.time() - start_time
    print(f"처리 시간: {process_time:.4f}초")
    
    # 4. 랜드마크 시각화 및 저장
    if results.multi_face_landmarks:
        face_landmarks = results.multi_face_landmarks[0]
        
        # 랜드마크 그리기
        annotated_image = image.copy()
        mp_drawing.draw_landmarks(
            annotated_image,
            face_landmarks,
            mp_face_mesh.FACEMESH_CONTOURS,
            None,
            mp_drawing_styles.get_default_face_mesh_contours_style()
        )
        
        os.makedirs("results", exist_ok=True)
        cv2.imwrite("results/mediapipe_landmarks.jpg", annotated_image)
        
        # 랜드마크 좌표 추출
        landmarks = []
        h, w = image.shape[:2]
        for landmark in face_landmarks.landmark:
            landmarks.append([
                landmark.x * w,
                landmark.y * h,
                landmark.z
            ])
        
        print(f"랜드마크 개수: {len(landmarks)}")
        print(f"결과 저장: results/mediapipe_landmarks.jpg")
        
        return {
            "process_time": process_time,
            "landmark_count": len(landmarks),
            "landmarks": np.array(landmarks),
            "result_path": "results/mediapipe_landmarks.jpg"
        }
    else:
        print("⚠️ 얼굴을 감지하지 못했습니다.")
        print("다른 이미지로 시도하세요.")
        return None

if __name__ == "__main__":
    results = test_mediapipe()
    if results:
        print("\n=== 테스트 결과 ===")
        print(f"처리 시간: {results['process_time']:.4f}초")
        print(f"랜드마크 개수: {results['landmark_count']}")
```

**Colab에서 MediaPipe 테스트**
```python
# Colab 노트북
!pip install mediapipe opencv-python

import cv2
import mediapipe as mp
from google.colab.patches import cv2_imshow
from google.colab import files

# 이미지 업로드
uploaded = files.upload()
image = cv2.imread(list(uploaded.keys())[0])

# Face Mesh 처리
mp_face_mesh = mp.solutions.face_mesh
face_mesh = mp_face_mesh.FaceMesh()

results = face_mesh.process(cv2.cvtColor(image, cv2.COLOR_BGR2RGB))

# 결과 시각화
if results.multi_face_landmarks:
    mp_drawing = mp.solutions.drawing_utils
    mp_drawing.draw_landmarks(image, results.multi_face_landmarks[0])
    cv2_imshow(image)
    print(f"랜드마크 개수: {len(results.multi_face_landmarks[0].landmark)}")
```

##### OOTDiffusion 비교 테스트

**Hugging Face Space 테스트**
- [ ] OOTDiffusion Space 접속
  - https://huggingface.co/spaces/levihsu/OOTDiffusion
- [ ] IDM-VTON과 동일한 샘플 이미지로 테스트
- [ ] 결과 비교
  - 품질 비교 (시각적 평가)
  - 처리 속도 비교 (Space에서 표시되는 시간)
  - 라이선스 확인 (상용 가능 여부)

**비교 체크리스트**
- [ ] 품질: IDM-VTON vs OOTDiffusion (1-5점)
- [ ] 속도: 처리 시간 비교
- [ ] 라이선스: 상용 가능 여부 확인
- [ ] GPU 요구사항: VRAM 사용량 비교

##### 최종 모델 조합 결정

**파이프라인 통합 테스트**
```python
# test_pipeline.py
"""
전체 파이프라인 테스트:
1. SAM으로 의상 세그멘테이션
2. MediaPipe로 얼굴 랜드마크 추출
3. IDM-VTON으로 Try-On 생성
"""

def test_full_pipeline():
    print("=== 전체 파이프라인 테스트 ===")
    
    # 1. SAM 세그멘테이션
    print("1. 의상 세그멘테이션 중...")
    garment_mask = sam_service.segment_garment("test_images/garment/test_garment.jpg")
    
    # 2. MediaPipe 얼굴 처리
    print("2. 얼굴 랜드마크 추출 중...")
    face_landmarks = mediapipe_service.extract_landmarks("test_images/person/test_person.jpg")
    
    # 3. IDM-VTON Try-On
    print("3. Try-On 생성 중...")
    result = idm_vton_service.generate_tryon(
        person_image="test_images/person/test_person.jpg",
        garment_image="test_images/garment/test_garment.jpg",
        garment_mask=garment_mask
    )
    
    # 4. 전체 처리 시간 측정
    total_time = measure_total_time()
    
    # 5. 결과 저장
    result.save("results/full_pipeline_result.jpg")
    
    return {
        "result": "results/full_pipeline_result.jpg",
        "total_time": total_time,
        "quality_score": None  # 수동 평가
    }
```

**결정 기준**
- [ ] 각 모델별 품질 점수 (5점 만점, 5개 이상 샘플)
- [ ] 처리 속도 (목표: 전체 파이프라인 60초 이내)
- [ ] GPU 메모리 사용량 (목표: 24GB 이하)
- [ ] 라이선스 호환성 (상용 가능 여부)
- [ ] 통합 파이프라인 테스트 결과

**최종 모델 조합 문서 작성**
- [ ] 선택한 모델 및 버전 명시
  - Try-On: IDM-VTON (버전)
  - 세그멘테이션: SAM (vit_h)
  - 얼굴 처리: MediaPipe Face Mesh
- [ ] 선택 이유 문서화
- [ ] 실제 측정된 처리 시간 및 리소스 사용량 기록
- [ ] 대안 모델 및 롤백 계획

**Day 3-4: 개발 환경 세팅 (Python)**
- [ ] Python 개발 환경 구축
  - Python 3.11+ 설치 확인
  - 가상환경 생성 (`python -m venv venv`)
  - FastAPI 프로젝트 구조 생성
  - 필수 라이브러리 설치
    ```bash
    pip install fastapi uvicorn sqlalchemy pydantic
    pip install python-jose[cryptography] passlib[bcrypt]
    pip install celery redis
    pip install torch torchvision
    pip install pillow opencv-python
    ```
  - `requirements.txt` 작성
- [ ] Python 프로젝트 구조 생성
  ```
  backend-python/
  ├── app/
  │   ├── api/
  │   ├── core/
  │   ├── models/
  │   ├── schemas/
  │   ├── services/
  │   └── tasks/
  ├── tests/
  └── main.py
  ```

**Day 5: 개발 환경 세팅 (Java)**
- [ ] Java 개발 환경 구축
  - Java 17+ 설치 확인
  - Spring Boot 프로젝트 생성 (Spring Initializr)
  - Maven/Gradle 설정
  - 필수 의존성 추가
    ```xml
    <!-- Spring Boot Web -->
    <!-- Spring Data JPA -->
    <!-- PostgreSQL Driver -->
    <!-- Spring Security -->
    <!-- JWT -->
    ```
- [ ] Java 프로젝트 구조 생성
  ```
  backend-java/
  ├── src/main/java/
  │   └── com/virtualtryon/
  │       ├── controller/
  │       ├── service/
  │       ├── repository/
  │       ├── entity/
  │       └── config/
  └── pom.xml
  ```

**Day 6-7: 개발 환경 세팅 (Node.js + Docker)**
- [ ] Node.js 개발 환경 구축
  - Node.js 20.x 설치 확인
  - pnpm 설치 (`npm install -g pnpm`)
  - Next.js 프로젝트 생성
    ```bash
    pnpm create next-app@latest frontend --typescript --tailwind --app
    ```
  - 필수 패키지 설치
    ```bash
    pnpm add zustand @tanstack/react-query three @react-three/fiber
    pnpm add axios zod
    ```
- [ ] Docker 환경 구축
  - Docker Desktop 설치 확인
  - Docker Compose 파일 작성
    ```yaml
    version: '3.8'
    services:
      postgres:
        image: postgres:16
        environment:
          POSTGRES_DB: tryon_db
          POSTGRES_USER: user
          POSTGRES_PASSWORD: password
        ports:
          - "5432:5432"
        volumes:
          - postgres_data:/var/lib/postgresql/data
      
      redis:
        image: redis:7-alpine
        ports:
          - "6379:6379"
    ```
  - `.dockerignore` 파일 작성
- [ ] 프로젝트 루트 구조 생성
  ```
  3D_Model/
  ├── backend-python/    # FastAPI (AI 연동)
  ├── backend-java/      # Spring Boot (비즈니스 로직)
  ├── frontend/          # Next.js
  ├── docker-compose.yml
  └── README.md
  ```

#### Week 2: 데이터베이스 & 인프라 설정

**Day 1-2: PostgreSQL 설정**
- [ ] PostgreSQL 로컬 설치/확인
  - Docker Compose로 PostgreSQL 실행
  - `psql` 접속 테스트
- [ ] 데이터베이스 스키마 설계
  - ERD 기반으로 테이블 구조 설계
  - 마이그레이션 스크립트 작성
    ```sql
    -- users 테이블
    CREATE TABLE users (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      email VARCHAR(255) UNIQUE NOT NULL,
      password_hash VARCHAR(255) NOT NULL,
      ...
    );
    ```
- [ ] Python (SQLAlchemy) 모델 생성
  ```python
  # app/models/user.py
  from sqlalchemy import Column, String, DateTime
  class User(Base):
      __tablename__ = "users"
      id = Column(UUID, primary_key=True)
      email = Column(String(255), unique=True, nullable=False)
      ...
  ```
- [ ] Java (JPA) 엔티티 생성
  ```java
  // entity/User.java
  @Entity
  @Table(name = "users")
  public class User {
      @Id
      @GeneratedValue(strategy = GenerationType.UUID)
      private UUID id;
      ...
  }
  ```
- [ ] 마이그레이션 도구 설정
  - Python: Alembic 설정
  - Java: Flyway 또는 Liquibase 설정

**Day 3-4: Docker 컨테이너 구성**
- [ ] Python 서비스 Dockerfile 작성
  ```dockerfile
  FROM python:3.11-slim
  WORKDIR /app
  COPY requirements.txt .
  RUN pip install --no-cache-dir -r requirements.txt
  COPY . .
  CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
  ```
- [ ] Java 서비스 Dockerfile 작성
  ```dockerfile
  FROM maven:3.9-eclipse-temurin-17 AS build
  WORKDIR /app
  COPY pom.xml .
  COPY src ./src
  RUN mvn clean package -DskipTests
  
  FROM eclipse-temurin:17-jre
  WORKDIR /app
  COPY --from=build /app/target/*.jar app.jar
  CMD ["java", "-jar", "app.jar"]
  ```
- [ ] Next.js Dockerfile 작성
  ```dockerfile
  FROM node:20-alpine AS builder
  WORKDIR /app
  COPY package*.json ./
  RUN npm ci
  COPY . .
  RUN npm run build
  
  FROM node:20-alpine
  WORKDIR /app
  COPY --from=builder /app/.next ./.next
  COPY --from=builder /app/public ./public
  COPY --from=builder /app/package*.json ./
  RUN npm ci --only=production
  CMD ["npm", "start"]
  ```
- [ ] Docker Compose 통합 설정
  ```yaml
  services:
    postgres: ...
    redis: ...
    backend-python:
      build: ./backend-python
      ports:
        - "8000:8000"
    backend-java:
      build: ./backend-java
      ports:
        - "8080:8080"
    frontend:
      build: ./frontend
      ports:
        - "3000:3000"
  ```

**Day 5: GPU 서버 & 클라우드 설정**
- [ ] GPU 서버 선정
  - RunPod vs Lambda Labs 비교
  - 가격/성능 분석
  - 테스트 계정 생성
- [ ] 클라우드 서비스 계정 생성
  - Vercel (Frontend 배포)
  - Railway/Render (Backend 배포)
  - Cloudflare R2 (이미지 저장소)
  - Supabase (PostgreSQL 호스팅, 선택사항)
- [ ] 환경 변수 관리 설정
  - `.env.example` 파일 생성
  - 각 서비스별 환경 변수 정의
  - Secrets 관리 방법 문서화

**Day 6-7: 프로젝트 저장소 & CI/CD 기초**
- [ ] GitHub 저장소 생성
  - 저장소 구조 설정
  - `.gitignore` 작성 (Python, Java, Node.js)
  - 브랜치 전략 정의 (Git Flow)
- [ ] CI/CD 파이프라인 기초 설정
  - GitHub Actions 워크플로우 작성
    - Python 코드 품질 검사 (pylint, black)
    - Java 코드 빌드 테스트 (Maven)
    - Node.js 빌드 테스트 (pnpm)
  - Docker 이미지 빌드 자동화

#### Week 3: 설계 & 문서화

**Day 1-2: UI/UX 설계**
- [ ] Figma 디자인 시스템 구축
  - 색상 팔레트 정의
  - 타이포그래피 시스템
  - 컴포넌트 라이브러리
- [ ] 주요 화면 설계
  - 로그인/회원가입 페이지
  - 메인 대시보드
  - 의상 업로드 페이지
  - Try-On 결과 페이지
  - 아바타 설정 페이지
- [ ] 프로토타이핑
  - 사용자 플로우 프로토타입
  - 인터랙션 정의

**Day 3-4: API 명세서 작성**
- [ ] OpenAPI/Swagger 명세서 작성
  - Python FastAPI: 자동 생성 (`/docs` 엔드포인트)
  - Java Spring Boot: Swagger 설정
- [ ] API 엔드포인트 정의
  ```
  # 인증
  POST /api/v1/auth/register
  POST /api/v1/auth/login
  GET  /api/v1/auth/me
  
  # 의상 관리
  POST   /api/v1/garments
  GET    /api/v1/garments
  DELETE /api/v1/garments/{id}
  
  # Try-On
  POST /api/v1/tryon
  GET  /api/v1/tryon/{job_id}/status
  GET  /api/v1/tryon/{job_id}/result
  ```
- [ ] 요청/응답 스키마 정의
  - Pydantic 모델 (Python)
  - DTO 클래스 (Java)
- [ ] 에러 코드 정의
  - 공통 에러 응답 형식
  - HTTP 상태 코드 매핑

**Day 5: ERD 확정 & 마이그레이션 준비**
- [ ] ERD 최종 검토
  - 테이블 관계 확인
  - 인덱스 설계
  - 제약조건 확인
- [ ] 마이그레이션 스크립트 작성
  - 초기 스키마 마이그레이션
  - 시드 데이터 스크립트
- [ ] 데이터베이스 백업 전략 수립

**Day 6-7: 개발 컨벤션 & 문서화**
- [ ] 코딩 컨벤션 문서 작성
  - Python: PEP 8, Black 포맷터
  - Java: Google Java Style Guide
  - TypeScript/JavaScript: ESLint, Prettier
- [ ] Git 컨벤션 정의
  - 커밋 메시지 규칙
  - 브랜치 네이밍 규칙
  - PR 템플릿 작성
- [ ] 개발 환경 설정 가이드 작성
  - 로컬 개발 환경 구축 가이드
  - Docker Compose 실행 가이드
  - 환경 변수 설정 가이드

### 산출물
- [ ] AI 모델 검증 보고서 (품질, 속도, GPU 사용량)
- [ ] 기술 스택 확정 문서 (Python + Java + Node.js + Docker + PostgreSQL)
- [ ] Figma 디자인 시스템 & 프로토타입
- [ ] OpenAPI 명세서 (Swagger 문서)
- [ ] ERD 최종본 & 마이그레이션 스크립트
- [ ] 개발 환경 설정 가이드
- [ ] 코딩 컨벤션 문서

---

## 🚀 Phase 1: MVP (8~10주)

### 목표
> 핵심 기능이 동작하는 최소 제품 및 추상 데이터 시각화 PoC

### 기술 스택 역할 분담
- **Python (FastAPI)**: AI 모델 연동, 이미지 처리, 추상 데이터(JSON/Log)의 3D 좌표 변환
- **Java (Spring Boot)**: 비즈니스 로직, 사용자 관리, 시각화 프로젝트 메타데이터 관리
- **Node.js (Next.js)**: 프론트엔드, Three.js 기반 범용 3D 뷰어 구현
- **PostgreSQL**: 메인 데이터베이스 (Python과 Java 모두 접근)
- **Docker**: 모든 서비스 컨테이너화

### 핵심 기능
1. ✅ 회원가입/로그인
2. ✅ 옷 사진 업로드 및 가상 피팅
3. ✅ **추상 데이터 시각화**: JSON 파일을 업로드하면 3D 구조로 변환하여 출력
4. ✅ 결과 저장/관리 및 시나리오 갤러리

### 스프린트 상세

#### Sprint 1 (Week 1): 인프라 & 데이터베이스 구축

**Day 1-2: Docker 환경 완성**
- [ ] Docker Compose 최종 설정
  ```yaml
  services:
    postgres:
      image: postgres:16
      environment:
        POSTGRES_DB: tryon_db
        POSTGRES_USER: tryon_user
        POSTGRES_PASSWORD: ${DB_PASSWORD}
      ports:
        - "5432:5432"
      volumes:
        - postgres_data:/var/lib/postgresql/data
        - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    
    redis:
      image: redis:7-alpine
      ports:
        - "6379:6379"
      volumes:
        - redis_data:/data
    
    backend-python:
      build:
        context: ./backend-python
        dockerfile: Dockerfile
      ports:
        - "8000:8000"
      environment:
        - DATABASE_URL=postgresql://tryon_user:${DB_PASSWORD}@postgres:5432/tryon_db
        - REDIS_URL=redis://redis:6379
      depends_on:
        - postgres
        - redis
    
    backend-java:
      build:
        context: ./backend-java
        dockerfile: Dockerfile
      ports:
        - "8080:8080"
      environment:
        - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/tryon_db
        - SPRING_DATASOURCE_USERNAME=tryon_user
        - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
      depends_on:
        - postgres
    
    frontend:
      build:
        context: ./frontend
        dockerfile: Dockerfile
      ports:
        - "3000:3000"
      environment:
        - NEXT_PUBLIC_API_URL=http://localhost:8080
        - NEXT_PUBLIC_PYTHON_API_URL=http://localhost:8000
      depends_on:
        - backend-java
        - backend-python
  ```
- [ ] `.env` 파일 템플릿 작성
- [ ] Docker 네트워크 설정 (서비스 간 통신)
- [ ] 볼륨 마운트 설정 (데이터 영속성)

**Day 3-4: PostgreSQL 스키마 구축**
- [ ] 데이터베이스 초기화 스크립트 작성
  ```sql
  -- init.sql
  CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
  CREATE EXTENSION IF NOT EXISTS "pg_trgm"; -- 텍스트 검색용
  ```
- [ ] Python (Alembic) 마이그레이션 설정
  ```bash
  cd backend-python
  alembic init alembic
  alembic revision --autogenerate -m "Initial schema"
  alembic upgrade head
  ```
- [ ] Java (Flyway) 마이그레이션 설정
  ```xml
  <!-- pom.xml -->
  <dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
  </dependency>
  ```
  ```sql
  -- src/main/resources/db/migration/V1__Initial_schema.sql
  CREATE TABLE users (...);
  CREATE TABLE avatars (...);
  ```
- [ ] 공통 스키마 정의 문서 작성
  - Python과 Java가 동일한 스키마 사용
  - 테이블명, 컬럼명 통일
- [ ] 인덱스 최적화
  - 자주 조회되는 컬럼 인덱스 생성
  - 외래키 인덱스 확인

**Day 5-7: Python Backend 기반 구축**
- [ ] FastAPI 프로젝트 구조 완성
  ```
  backend-python/
  ├── app/
  │   ├── __init__.py
  │   ├── main.py              # FastAPI 앱 진입점
  │   ├── api/
  │   │   ├── v1/
  │   │   │   ├── auth.py      # 인증 API
  │   │   │   ├── garments.py  # 의상 관리 API
  │   │   │   └── tryon.py     # Try-On API
  │   ├── core/
  │   │   ├── config.py        # 설정 관리
  │   │   ├── security.py      # JWT, 비밀번호 해싱
  │   │   └── database.py      # DB 연결
  │   ├── models/
  │   │   ├── user.py          # SQLAlchemy 모델
  │   │   ├── garment.py
  │   │   └── tryon_result.py
  │   ├── schemas/
  │   │   ├── user.py          # Pydantic 스키마
  │   │   └── garment.py
  │   ├── services/
  │   │   ├── auth_service.py
  │   │   └── storage_service.py
  │   └── tasks/
  │       ├── celery_app.py    # Celery 설정
  │       └── ai_tasks.py      # AI 처리 작업
  ├── alembic/                 # 마이그레이션
  ├── tests/
  ├── requirements.txt
  └── Dockerfile
  ```
- [ ] PostgreSQL 연동 (SQLAlchemy)
  ```python
  # app/core/database.py
  from sqlalchemy import create_engine
  from sqlalchemy.ext.declarative import declarative_base
  from sqlalchemy.orm import sessionmaker
  
  engine = create_engine(DATABASE_URL)
  SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
  Base = declarative_base()
  ```
- [ ] Redis 연동 (Celery 브로커)
  ```python
  # app/tasks/celery_app.py
  from celery import Celery
  
  celery_app = Celery(
    "tryon_worker",
    broker="redis://redis:6379/0",
    backend="redis://redis:6379/0"
  )
  ```
- [ ] 환경 변수 관리 (pydantic-settings)
  ```python
  # app/core/config.py
  from pydantic_settings import BaseSettings
  
  class Settings(BaseSettings):
    database_url: str
    redis_url: str
    secret_key: str
    jwt_algorithm: str = "HS256"
    
    class Config:
      env_file = ".env"
  ```

#### Sprint 2 (Week 2): Java Backend 기반 구축

**Day 1-3: Spring Boot 프로젝트 구조**
- [ ] Spring Boot 프로젝트 구조 완성
  ```
  backend-java/
  ├── src/main/java/com/virtualtryon/
  │   ├── VirtualTryOnApplication.java
  │   ├── controller/
  │   │   ├── AuthController.java
  │   │   ├── UserController.java
  │   │   └── GarmentController.java
  │   ├── service/
  │   │   ├── AuthService.java
  │   │   ├── UserService.java
  │   │   └── GarmentService.java
  │   ├── repository/
  │   │   ├── UserRepository.java
  │   │   └── GarmentRepository.java
  │   ├── entity/
  │   │   ├── User.java
  │   │   └── Garment.java
  │   ├── dto/
  │   │   ├── UserDTO.java
  │   │   └── GarmentDTO.java
  │   └── config/
  │       ├── DatabaseConfig.java
  │       ├── SecurityConfig.java
  │       └── WebConfig.java
  ├── src/main/resources/
  │   ├── application.yml
  │   └── db/migration/        # Flyway 마이그레이션
  ├── pom.xml
  └── Dockerfile
  ```
- [ ] PostgreSQL 연동 (Spring Data JPA)
  ```yaml
  # application.yml
  spring:
    datasource:
      url: jdbc:postgresql://postgres:5432/tryon_db
      username: ${DB_USERNAME}
      password: ${DB_PASSWORD}
    jpa:
      hibernate:
        ddl-auto: validate  # Flyway 사용 시 validate
      show-sql: true
    flyway:
      enabled: true
      locations: classpath:db/migration
  ```
- [ ] JPA 엔티티 생성
  ```java
  @Entity
  @Table(name = "users")
  public class User {
      @Id
      @GeneratedValue(strategy = GenerationType.UUID)
      private UUID id;
      
      @Column(unique = true, nullable = false)
      private String email;
      
      // ...
  }
  ```
- [ ] Repository 인터페이스 생성
  ```java
  @Repository
  public interface UserRepository extends JpaRepository<User, UUID> {
      Optional<User> findByEmail(String email);
  }
  ```

**Day 4-5: 인증 시스템 구축**
- [ ] Spring Security 설정
  ```java
  @Configuration
  @EnableWebSecurity
  public class SecurityConfig {
      @Bean
      public SecurityFilterChain filterChain(HttpSecurity http) {
          http.csrf().disable()
              .authorizeHttpRequests()
              .requestMatchers("/api/v1/auth/**").permitAll()
              .anyRequest().authenticated()
              .and()
              .sessionManagement()
              .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
          return http.build();
      }
  }
  ```
- [ ] JWT 토큰 생성/검증
  ```java
  @Service
  public class JwtService {
      public String generateToken(User user) { ... }
      public boolean validateToken(String token) { ... }
      public String extractEmail(String token) { ... }
  }
  ```
- [ ] 비밀번호 암호화 (BCrypt)
  ```java
  @Service
  public class PasswordService {
      private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
      
      public String encode(String rawPassword) {
          return encoder.encode(rawPassword);
      }
      
      public boolean matches(String rawPassword, String encodedPassword) {
          return encoder.matches(rawPassword, encodedPassword);
      }
  }
  ```
- [ ] 인증 API 엔드포인트 구현
  - `POST /api/v1/auth/register` - 회원가입
  - `POST /api/v1/auth/login` - 로그인
  - `GET /api/v1/auth/me` - 현재 사용자 정보

**Day 6-7: Python-Java 통신 설정**
- [ ] 서비스 간 통신 방식 정의
  - Java → Python: HTTP REST API 호출
  - Python → Java: HTTP REST API 호출
  - 공통 데이터 형식: JSON
- [ ] Python API 클라이언트 (Java에서 사용)
  ```java
  @Service
  public class PythonApiClient {
      private final RestTemplate restTemplate;
      private final String pythonApiUrl = "http://backend-python:8000";
      
      public TryOnResult requestTryOn(TryOnRequest request) {
          // Python FastAPI 호출
      }
  }
  ```
- [ ] Java API 클라이언트 (Python에서 사용)
  ```python
  # app/services/java_client.py
  import httpx
  
  class JavaApiClient:
      def __init__(self, base_url: str = "http://backend-java:8080"):
          self.base_url = base_url
          self.client = httpx.AsyncClient()
      
      async def get_user(self, user_id: str):
          # Java Spring Boot API 호출
          response = await self.client.get(f"{self.base_url}/api/v1/users/{user_id}")
          return response.json()
  ```

#### Sprint 3 (Week 3-4): Frontend 기반 구축

**Day 1-3: Next.js 프로젝트 구조**
- [ ] Next.js 14 App Router 구조 완성
  ```
  frontend/
  ├── app/
  │   ├── layout.tsx           # 루트 레이아웃
  │   ├── page.tsx              # 홈 페이지
  │   ├── (auth)/
  │   │   ├── login/
  │   │   │   └── page.tsx
  │   │   └── register/
  │   │       └── page.tsx
  │   ├── (main)/
  │   │   ├── dashboard/
  │   │   │   └── page.tsx
  │   │   └── tryon/
  │   │       └── page.tsx
  │   └── api/                  # API Routes (필요시)
  ├── components/
  │   ├── ui/                   # 기본 UI 컴포넌트
  │   │   ├── Button.tsx
  │   │   ├── Input.tsx
  │   │   └── Card.tsx
  │   ├── layout/
  │   │   ├── Header.tsx
  │   │   └── Footer.tsx
  │   └── features/
  │       ├── auth/
  │       └── tryon/
  ├── lib/
  │   ├── api/
  │   │   ├── java-client.ts    # Java API 클라이언트
  │   │   └── python-client.ts  # Python API 클라이언트
  │   └── utils/
  ├── stores/                    # Zustand 스토어
  │   └── auth-store.ts
  ├── hooks/                     # 커스텀 훅
  ├── public/
  ├── tailwind.config.ts
  ├── tsconfig.json
  ├── package.json
  └── Dockerfile
  ```
- [ ] TailwindCSS 설정
  ```typescript
  // tailwind.config.ts
  export default {
    content: [
      "./app/**/*.{js,ts,jsx,tsx}",
      "./components/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
      extend: {
        colors: {
          primary: {...},
        },
      },
    },
  }
  ```
- [ ] TypeScript 설정
  ```json
  // tsconfig.json
  {
    "compilerOptions": {
      "target": "ES2020",
      "lib": ["dom", "dom.iterable", "esnext"],
      "module": "esnext",
      "moduleResolution": "bundler",
      "strict": true,
      "paths": {
        "@/*": ["./*"]
      }
    }
  }
  ```

**Day 4-5: API 클라이언트 구축**
- [ ] Java API 클라이언트 (TypeScript)
  ```typescript
  // lib/api/java-client.ts
  import axios from 'axios';
  
  const javaApi = axios.create({
    baseURL: process.env.NEXT_PUBLIC_JAVA_API_URL || 'http://localhost:8080',
  });
  
  export const authApi = {
    register: (data: RegisterRequest) => 
      javaApi.post('/api/v1/auth/register', data),
    login: (data: LoginRequest) => 
      javaApi.post('/api/v1/auth/login', data),
    getMe: () => 
      javaApi.get('/api/v1/auth/me'),
  };
  ```
- [ ] Python API 클라이언트 (TypeScript)
  ```typescript
  // lib/api/python-client.ts
  const pythonApi = axios.create({
    baseURL: process.env.NEXT_PUBLIC_PYTHON_API_URL || 'http://localhost:8000',
  });
  
  export const tryonApi = {
    uploadGarment: (file: File) => {
      const formData = new FormData();
      formData.append('file', file);
      return pythonApi.post('/api/v1/garments/upload', formData);
    },
    requestTryOn: (data: TryOnRequest) => 
      pythonApi.post('/api/v1/tryon', data),
    getStatus: (jobId: string) => 
      pythonApi.get(`/api/v1/tryon/${jobId}/status`),
  };
  ```
- [ ] React Query 설정
  ```typescript
  // app/providers.tsx
  'use client';
  import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
  
  const queryClient = new QueryClient();
  
  export function Providers({ children }: { children: React.ReactNode }) {
    return (
      <QueryClientProvider client={queryClient}>
        {children}
      </QueryClientProvider>
    );
  }
  ```

**Day 6-7: 인증 UI 구현**
- [ ] 로그인 페이지
  - 이메일/비밀번호 입력 폼
  - 유효성 검사 (Zod)
  - 에러 처리
  - 로딩 상태
- [ ] 회원가입 페이지
  - 이메일, 비밀번호, 이름 입력
  - 비밀번호 확인
  - 약관 동의
- [ ] 인증 상태 관리 (Zustand)
  ```typescript
  // stores/auth-store.ts
  import { create } from 'zustand';
  
  interface AuthState {
    user: User | null;
    token: string | null;
    login: (email: string, password: string) => Promise<void>;
    logout: () => void;
  }
  
  export const useAuthStore = create<AuthState>((set) => ({
    user: null,
    token: null,
    login: async (email, password) => {
      const response = await authApi.login({ email, password });
      set({ user: response.data.user, token: response.data.token });
    },
    logout: () => set({ user: null, token: null }),
  }));
  ```
- [ ] 인증 미들웨어 (라우트 보호)
  ```typescript
  // middleware.ts
  import { NextResponse } from 'next/server';
  import type { NextRequest } from 'next/server';
  
  export function middleware(request: NextRequest) {
    const token = request.cookies.get('token');
    if (!token && request.nextUrl.pathname.startsWith('/dashboard')) {
      return NextResponse.redirect(new URL('/login', request.url));
    }
  }
  ```

#### Sprint 4 (Week 5-6): 이미지 업로드 & 저장소 연동

**Day 1-2: 이미지 업로드 API (Python)**
- [ ] 파일 업로드 엔드포인트 구현
  ```python
  # app/api/v1/garments.py
  from fastapi import UploadFile, File
  from app.services.storage_service import StorageService
  
  @router.post("/upload")
  async def upload_garment(
      file: UploadFile = File(...),
      current_user: User = Depends(get_current_user)
  ):
      # 파일 검증 (크기, 형식)
      # Cloudflare R2에 업로드
      # DB에 메타데이터 저장
      pass
  ```
- [ ] Cloudflare R2 연동
  ```python
  # app/services/storage_service.py
  import boto3
  from botocore.config import Config
  
  class StorageService:
      def __init__(self):
          self.s3_client = boto3.client(
              's3',
              endpoint_url=R2_ENDPOINT,
              aws_access_key_id=R2_ACCESS_KEY,
              aws_secret_access_key=R2_SECRET_KEY,
              config=Config(signature_version='s3v4')
          )
      
      async def upload_file(self, file: bytes, key: str) -> str:
          self.s3_client.put_object(
              Bucket=R2_BUCKET,
              Key=key,
              Body=file
          )
          return f"{R2_PUBLIC_URL}/{key}"
  ```
- [ ] 이미지 전처리
  - 크기 조정
  - 형식 변환 (WebP)
  - 썸네일 생성

**Day 3-4: 의상 관리 API (Java)**
- [ ] 의상 CRUD API 구현
  ```java
  @RestController
  @RequestMapping("/api/v1/garments")
  public class GarmentController {
      @Autowired
      private GarmentService garmentService;
      
      @PostMapping
      public ResponseEntity<GarmentDTO> createGarment(
          @RequestBody CreateGarmentRequest request
      ) {
          GarmentDTO garment = garmentService.createGarment(request);
          return ResponseEntity.ok(garment);
      }
      
      @GetMapping
      public ResponseEntity<List<GarmentDTO>>> getGarments() {
          List<GarmentDTO> garments = garmentService.getUserGarments();
          return ResponseEntity.ok(garments);
      }
  }
  ```
- [ ] 의상 메타데이터 관리
  - 카테고리 (상의/하의/원피스)
  - 색상 추출
  - 태그 시스템

**Day 5-7: Frontend 업로드 UI & 시각화**
- [ ] 이미지 업로드 컴포넌트
  ```typescript
  // components/features/tryon/ImageUpload.tsx
  'use client';
  import { useDropzone } from 'react-dropzone';
  
  export function ImageUpload({ onUpload }: Props) {
    const { getRootProps, getInputProps } = useDropzone({
      accept: { 'image/*': ['.jpeg', '.jpg', '.png', '.webp'] },
      maxSize: 10 * 1024 * 1024, // 10MB
      onDrop: async (files) => {
        const file = files[0];
        const formData = new FormData();
        formData.append('file', file);
        const response = await tryonApi.uploadGarment(file);
        onUpload(response.data);
      },
    });
    
    return (
      <div {...getRootProps()}>
        <input {...getInputProps()} />
        <p>이미지를 드래그하거나 클릭하여 업로드</p>
      </div>
    );
  }
  ```
- [ ] 업로드 진행 상태 표시 (Progress Bar)
- [ ] 이미지 미리보기 (브라우저 `<img>` 태그 사용)
- [ ] 업로드된 의상 목록 표시 (그리드 레이아웃)

#### Sprint 5 (Week 7-8): AI Pipeline 구축

**Day 1-3: AI 모델 래퍼 개발 (Python)**
- [ ] SAM 세그멘테이션 래퍼
  ```python
  # app/services/ai/segmentation.py
  from segment_anything import sam_model_registry, SamPredictor
  import torch
  import cv2
  
  class SegmentationService:
      def __init__(self):
          self.device = "cuda" if torch.cuda.is_available() else "cpu"
          sam = sam_model_registry["vit_h"](checkpoint="sam_vit_h_4b8939.pth")
          sam.to(device=self.device)
          self.predictor = SamPredictor(sam)
      
      def segment_garment(self, image_path: str) -> np.ndarray:
          image = cv2.imread(image_path)
          self.predictor.set_image(image)
          # 세그멘테이션 수행
          masks, scores, logits = self.predictor.predict(...)
          return masks[0]  # 최고 점수 마스크 반환
  ```
- [ ] IDM-VTON 래퍼
  ```python
  # app/services/ai/tryon.py
  from diffusers import StableDiffusionPipeline
  import torch
  
  class TryOnService:
      def __init__(self):
          self.device = "cuda" if torch.cuda.is_available() else "cpu"
          self.pipeline = StableDiffusionPipeline.from_pretrained(
              "yisol/IDM-VTON",
              torch_dtype=torch.float16
          ).to(self.device)
      
      async def generate_tryon(
          self, 
          person_image: str, 
          garment_image: str,
          garment_mask: np.ndarray
      ) -> str:
          # IDM-VTON 파이프라인 실행
          result = self.pipeline(
              prompt="...",
              person_image=person_image,
              garment_image=garment_image,
              garment_mask=garment_mask
          )
          return result.images[0]
  ```
- [ ] AI 파이프라인 통합
  ```python
  # app/services/ai/pipeline.py
  class AIPipeline:
      def __init__(self):
          self.segmentation = SegmentationService()
          self.tryon = TryOnService()
      
      async def process_tryon(
          self,
          person_image_path: str,
          garment_image_path: str
      ) -> str:
          # 1. 의상 세그멘테이션
          garment_mask = self.segmentation.segment_garment(garment_image_path)
          
          # 2. Try-On 생성
          result_image = await self.tryon.generate_tryon(
              person_image_path,
              garment_image_path,
              garment_mask
          )
          
          # 3. 결과 저장
          result_url = await storage_service.save_image(result_image)
          return result_url
  ```

**Day 4-5: Celery 작업 큐 설정**
- [ ] Celery Worker 설정
  ```python
  # app/tasks/celery_app.py
  from celery import Celery
  from app.core.config import settings
  
  celery_app = Celery(
      "tryon_worker",
      broker=f"redis://{settings.REDIS_HOST}:{settings.REDIS_PORT}/0",
      backend=f"redis://{settings.REDIS_HOST}:{settings.REDIS_PORT}/0"
  )
  
  celery_app.conf.update(
      task_serializer='json',
      accept_content=['json'],
      result_serializer='json',
      timezone='UTC',
      enable_utc=True,
  )
  ```
- [ ] Try-On 작업 태스크 정의
  ```python
  # app/tasks/ai_tasks.py
  from app.tasks.celery_app import celery_app
  from app.services.ai.pipeline import AIPipeline
  
  @celery_app.task(bind=True)
  def process_tryon_task(
      self,
      user_id: str,
      person_image_url: str,
      garment_image_url: str
  ):
      try:
          pipeline = AIPipeline()
          result_url = await pipeline.process_tryon(
              person_image_url,
              garment_image_url
          )
          
          # DB에 결과 저장 (Java API 호출)
          java_client.save_tryon_result(user_id, result_url)
          
          return {"status": "completed", "result_url": result_url}
      except Exception as exc:
          self.retry(exc=exc, countdown=60, max_retries=3)
  ```
- [ ] 작업 상태 추적 (Redis)
  ```python
  # 작업 시작 시
  redis_client.set(f"job:{job_id}:status", "processing")
  
  # 작업 완료 시
  redis_client.set(f"job:{job_id}:status", "completed")
  redis_client.set(f"job:{job_id}:result", result_url)
  ```

**Day 6-7: GPU 서버 배포**
- [ ] RunPod GPU 서버 설정
  - Docker 이미지 빌드 (Python + AI 모델)
  - RunPod 템플릿 생성
  - API 엔드포인트 설정
- [ ] GPU 서버 Dockerfile
  ```dockerfile
  FROM nvidia/cuda:11.8.0-cudnn8-runtime-ubuntu22.04
  WORKDIR /app
  
  # Python 설치
  RUN apt-get update && apt-get install -y python3.11 python3-pip
  
  # 의존성 설치
  COPY requirements.txt .
  RUN pip install --no-cache-dir -r requirements.txt
  RUN pip install torch torchvision --index-url https://download.pytorch.org/whl/cu118
  
  # AI 모델 다운로드 (또는 볼륨 마운트)
  COPY models/ ./models/
  
  # Celery Worker 실행
  CMD ["celery", "-A", "app.tasks.celery_app", "worker", "--loglevel=info"]
  ```
- [ ] GPU 서버와 메인 서버 통신
  - HTTP API로 작업 요청
  - Webhook으로 결과 수신

#### Sprint 6 (Week 9): Try-On API 통합

**Day 1-3: Try-On API 구현 (Python)**
- [ ] Try-On 요청 엔드포인트
  ```python
  # app/api/v1/tryon.py
  @router.post("/tryon")
  async def request_tryon(
      request: TryOnRequest,
      current_user: User = Depends(get_current_user)
  ):
      # 1. 작업 ID 생성
      job_id = str(uuid.uuid4())
      
      # 2. Celery 작업 큐에 추가
      task = process_tryon_task.delay(
          user_id=str(current_user.id),
          person_image_url=request.person_image_url,
          garment_image_url=request.garment_image_url
      )
      
      # 3. 작업 상태 저장 (Redis)
      redis_client.set(f"job:{job_id}:task_id", task.id)
      redis_client.set(f"job:{job_id}:status", "pending")
      
      return {"job_id": job_id, "status": "pending"}
  ```
- [ ] 작업 상태 조회 API
  ```python
  @router.get("/tryon/{job_id}/status")
  async def get_tryon_status(
      job_id: str,
      current_user: User = Depends(get_current_user)
  ):
      status = redis_client.get(f"job:{job_id}:status")
      if status == "completed":
          result_url = redis_client.get(f"job:{job_id}:result")
          return {"status": "completed", "result_url": result_url}
      return {"status": status}
  ```
- [ ] 결과 조회 API
  ```python
  @router.get("/tryon/{job_id}/result")
  async def get_tryon_result(
      job_id: str,
      current_user: User = Depends(get_current_user)
  ):
      # Java API에서 결과 조회
      result = await java_client.get_tryon_result(job_id)
      return result
  ```

**Day 4-5: Try-On API 구현 (Java)**
- [ ] Try-On 결과 저장 API
  ```java
  @PostMapping("/tryon/results")
  public ResponseEntity<TryOnResultDTO> saveTryOnResult(
      @RequestBody SaveTryOnResultRequest request
  ) {
      TryOnResult result = tryOnService.saveResult(
          request.getUserId(),
          request.getGarmentId(),
          request.getResultImageUrl()
      );
      return ResponseEntity.ok(TryOnResultDTO.from(result));
  }
  ```
- [ ] Try-On 결과 조회 API
  ```java
  @GetMapping("/tryon/results/{jobId}")
  public ResponseEntity<TryOnResultDTO> getTryOnResult(
      @PathVariable String jobId
  ) {
      TryOnResult result = tryOnService.getResult(jobId);
      return ResponseEntity.ok(TryOnResultDTO.from(result));
  }
  ```
- [ ] 사용자별 결과 목록 API
  ```java
  @GetMapping("/tryon/results")
  public ResponseEntity<List<TryOnResultDTO>> getUserResults() {
      List<TryOnResult> results = tryOnService.getUserResults();
      return ResponseEntity.ok(results.stream()
          .map(TryOnResultDTO::from)
          .collect(Collectors.toList()));
  }
  ```

**Day 6-7: Frontend Try-On UI**
- [ ] Try-On 요청 페이지
  ```typescript
  // app/(main)/tryon/page.tsx
  'use client';
  export default function TryOnPage() {
    const [selectedGarment, setSelectedGarment] = useState<Garment | null>(null);
    const [jobId, setJobId] = useState<string | null>(null);
    
    const handleTryOn = async () => {
      const response = await tryonApi.requestTryOn({
        garment_id: selectedGarment.id,
        person_image_url: defaultPersonImageUrl, // 기본 마네킹
      });
      setJobId(response.data.job_id);
      
      // 폴링으로 상태 확인
      pollJobStatus(response.data.job_id);
    };
    
    return (
      <div>
        <GarmentSelector onSelect={setSelectedGarment} />
        <Button onClick={handleTryOn}>Try-On 시작</Button>
        {jobId && <JobStatus jobId={jobId} />}
      </div>
    );
  }
  ```
- [ ] 작업 상태 폴링
  ```typescript
  const pollJobStatus = async (jobId: string) => {
    const interval = setInterval(async () => {
      const status = await tryonApi.getStatus(jobId);
      if (status.status === 'completed') {
        clearInterval(interval);
        setResult(status.result_url);
      } else if (status.status === 'failed') {
        clearInterval(interval);
        showError('처리 실패');
      }
    }, 2000); // 2초마다 확인
  };
  ```
- [ ] Try-On 결과 표시 컴포넌트 (시각화)
  ```typescript
  // components/features/tryon/ResultViewer.tsx
  'use client';
  import Image from 'next/image';
  
  export function ResultViewer({ resultUrl }: { resultUrl: string }) {
    return (
      <div className="relative w-full h-[600px]">
        <Image
          src={resultUrl}
          alt="Try-On 결과"
          fill
          className="object-contain"
          priority
        />
      </div>
    );
  }
  ```
- [ ] 결과 갤러리 컴포넌트
  - 그리드 레이아웃 (CSS Grid 또는 Flexbox)
  - 이미지 미리보기 (Next.js `<Image>` 컴포넌트)
  - 다운로드 기능 (브라우저 `download` 속성 또는 API 호출)
  - 즐겨찾기 기능 (Zustand 상태 관리)
- [ ] 결과 비교 뷰 (Before/After)
  ```typescript
  // 원본 이미지와 Try-On 결과를 나란히 표시
  <div className="grid grid-cols-2 gap-4">
    <Image src={originalUrl} alt="원본" />
    <Image src={resultUrl} alt="Try-On 결과" />
  </div>
  ```

#### Sprint 7 (Week 10): 안정화 & 배포

**Day 1-2: 에러 핸들링 강화**
- [ ] Python 에러 핸들링
  ```python
  # app/core/exceptions.py
  class TryOnException(Exception):
      pass
  
  class ModelLoadException(TryOnException):
      pass
  
  # 전역 예외 핸들러
  @app.exception_handler(TryOnException)
  async def tryon_exception_handler(request, exc):
      return JSONResponse(
          status_code=500,
          content={"detail": str(exc)}
      )
  ```
- [ ] Java 에러 핸들링
  ```java
  @ControllerAdvice
  public class GlobalExceptionHandler {
      @ExceptionHandler(ResourceNotFoundException.class)
      public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException e) {
          return ResponseEntity.status(404)
              .body(new ErrorResponse(e.getMessage()));
      }
  }
  ```
- [ ] Frontend 에러 처리
  - React Error Boundary
  - API 에러 메시지 표시
  - 재시도 로직

**Day 3-4: 성능 최적화**
- [ ] 데이터베이스 쿼리 최적화
  - N+1 문제 해결 (JPA Fetch Join)
  - 인덱스 추가
  - 쿼리 로그 분석
- [ ] 이미지 최적화
  - WebP 형식 변환
  - 썸네일 생성
  - CDN 설정 (Cloudflare)
- [ ] API 응답 시간 개선
  - 캐싱 전략 (Redis)
  - 비동기 처리 최적화
- [ ] Frontend 최적화
  - 이미지 lazy loading
  - 코드 스플리팅
  - 번들 크기 최적화

**Day 5-6: 배포 파이프라인**
- [ ] GitHub Actions 워크플로우
  ```yaml
  # .github/workflows/deploy.yml
  name: Deploy
  on:
    push:
      branches: [main]
  
  jobs:
    build:
      runs-on: ubuntu-latest
      steps:
        - uses: actions/checkout@v3
        - name: Build Docker images
          run: |
            docker build -t backend-python ./backend-python
            docker build -t backend-java ./backend-java
            docker build -t frontend ./frontend
        - name: Push to registry
          run: |
            docker push ${{ secrets.REGISTRY }}/backend-python
            docker push ${{ secrets.REGISTRY }}/backend-java
            docker push ${{ secrets.REGISTRY }}/frontend
    
    deploy:
      needs: build
      runs-on: ubuntu-latest
      steps:
        - name: Deploy to production
          run: |
            # 배포 스크립트 실행
  ```
- [ ] 환경별 설정 분리
  - 개발 환경 (Docker Compose)
  - 스테이징 환경
  - 프로덕션 환경
- [ ] 데이터베이스 마이그레이션 자동화
  - Alembic (Python)
  - Flyway (Java)
  - 배포 시 자동 실행

**Day 7: MVP 테스트**
- [ ] 통합 테스트
  - 전체 플로우 테스트 (회원가입 → 업로드 → Try-On → 결과 확인)
  - API 통합 테스트
- [ ] 성능 테스트
  - 동시 사용자 테스트
  - 응답 시간 측정
  - GPU 서버 부하 테스트
- [ ] 사용자 테스트
  - 베타 테스터 모집
  - 피드백 수집
  - 버그 리포트 정리

### MVP 마일스톤
```
┌────────────────────────────────────────────────────────────────┐
│  MVP 완료 기준                                                  │
├────────────────────────────────────────────────────────────────┤
│  ✓ 사용자가 가입하고 로그인할 수 있다                            │
│  ✓ 옷 사진을 업로드할 수 있다                                    │
│  ✓ 기본 마네킹에 옷이 적용된 이미지를 받을 수 있다                │
│  ✓ 결과물을 저장하고 다운로드할 수 있다                          │
│  ✓ 모바일/데스크톱에서 사용 가능하다                             │
└────────────────────────────────────────────────────────────────┘
```

---

## 👤 Phase 2: 개인화 (6~8주)

### 목표
> 개인 얼굴이 반영된 3D 아바타

### 기술 스택 활용
- **Python**: MediaPipe Face Mesh, 얼굴 처리, 아바타 생성
- **Java**: 아바타 데이터 관리, 체형 파라미터 처리
- **Node.js (Three.js)**: 3D 렌더링, 인터랙션

### 핵심 기능
1. ✅ 얼굴 사진 등록
2. ✅ 개인화 아바타 생성
3. ✅ 체형 파라미터 설정
4. ✅ 아바타에 옷 착용

### 스프린트 상세

#### Sprint 8 (Week 11-12): 얼굴 처리 & 아바타 생성

**Day 1-3: MediaPipe Face Mesh 통합 (Python)**
- [ ] MediaPipe 설치 및 설정
  ```python
  # app/services/ai/face_mesh.py
  import mediapipe as mp
  
  class FaceMeshService:
      def __init__(self):
          self.mp_face_mesh = mp.solutions.face_mesh
          self.face_mesh = self.mp_face_mesh.FaceMesh(
              static_image_mode=True,
              max_num_faces=1,
              refine_landmarks=True
          )
      
      def extract_face_landmarks(self, image_path: str) -> dict:
          image = cv2.imread(image_path)
          results = self.face_mesh.process(cv2.cvtColor(image, cv2.COLOR_BGR2RGB))
          
          if results.multi_face_landmarks:
              landmarks = results.multi_face_landmarks[0]
              # 468개 랜드마크 추출
              return {
                  "landmarks": landmarks.landmark,
                  "mesh": self._generate_mesh(landmarks)
              }
          return None
  ```
- [ ] 얼굴 → 3D 메시 변환
  ```python
  def _generate_mesh(self, landmarks) -> np.ndarray:
      # MediaPipe 랜드마크를 3D 메시로 변환
      vertices = []
      for landmark in landmarks.landmark:
          vertices.append([landmark.x, landmark.y, landmark.z])
      return np.array(vertices)
  ```
- [ ] 아바타 텍스처 생성
  ```python
  def generate_avatar_texture(self, face_image: str) -> str:
      # 얼굴 이미지에서 텍스처 추출
      # UV 매핑 생성
      # 텍스처 이미지 저장
      return texture_url
  ```

**Day 4-5: 아바타 API 구현 (Java)**
- [ ] 아바타 엔티티 및 Repository
  ```java
  @Entity
  @Table(name = "avatars")
  public class Avatar {
      @Id
      @GeneratedValue(strategy = GenerationType.UUID)
      private UUID id;
      
      @ManyToOne
      @JoinColumn(name = "user_id")
      private User user;
      
      private String faceImageUrl;
      private String meshDataUrl;
      private Integer bodyHeight;
      private Integer bodyWeight;
      private String bodyType;
      private Boolean isDefault;
  }
  ```
- [ ] 아바타 CRUD API
  ```java
  @RestController
  @RequestMapping("/api/v1/avatars")
  public class AvatarController {
      @PostMapping
      public ResponseEntity<AvatarDTO> createAvatar(
          @RequestBody CreateAvatarRequest request
      ) {
          Avatar avatar = avatarService.createAvatar(request);
          return ResponseEntity.ok(AvatarDTO.from(avatar));
      }
      
      @GetMapping
      public ResponseEntity<List<AvatarDTO>> getAvatars() {
          List<Avatar> avatars = avatarService.getUserAvatars();
          return ResponseEntity.ok(avatars.stream()
              .map(AvatarDTO::from)
              .collect(Collectors.toList()));
      }
      
      @PutMapping("/{id}")
      public ResponseEntity<AvatarDTO> updateAvatar(
          @PathVariable UUID id,
          @RequestBody UpdateAvatarRequest request
      ) {
          Avatar avatar = avatarService.updateAvatar(id, request);
          return ResponseEntity.ok(AvatarDTO.from(avatar));
      }
  }
  ```
- [ ] 체형 파라미터 API
  ```java
  @PutMapping("/{id}/body-params")
  public ResponseEntity<AvatarDTO> updateBodyParams(
      @PathVariable UUID id,
      @RequestBody BodyParamsRequest request
  ) {
      Avatar avatar = avatarService.updateBodyParams(
          id,
          request.getHeight(),
          request.getWeight(),
          request.getBodyType()
      );
      return ResponseEntity.ok(AvatarDTO.from(avatar));
  }
  ```

**Day 6-7: 아바타 생성 파이프라인 통합 (Python)**
- [ ] 얼굴 등록 API
  ```python
  # app/api/v1/avatars.py
  @router.post("/avatars/register-face")
  async def register_face(
      file: UploadFile = File(...),
      current_user: User = Depends(get_current_user)
  ):
      # 1. 얼굴 이미지 업로드
      face_url = await storage_service.upload_file(file)
      
      # 2. 얼굴 랜드마크 추출
      face_service = FaceMeshService()
      landmarks = face_service.extract_face_landmarks(face_url)
      
      # 3. 3D 메시 생성
      mesh_data = face_service._generate_mesh(landmarks)
      mesh_url = await storage_service.save_mesh(mesh_data)
      
      # 4. Java API에 아바타 생성 요청
      avatar = await java_client.create_avatar(
          user_id=str(current_user.id),
          face_image_url=face_url,
          mesh_data_url=mesh_url
      )
      
      return avatar
  ```
- [ ] 아바타 + Try-On 통합
  ```python
  @router.post("/tryon/with-avatar")
  async def tryon_with_avatar(
      request: TryOnWithAvatarRequest,
      current_user: User = Depends(get_current_user)
  ):
      # 1. 아바타 정보 조회 (Java API)
      avatar = await java_client.get_avatar(request.avatar_id)
      
      # 2. 아바타 이미지와 의상 이미지로 Try-On
      result = await ai_pipeline.process_tryon_with_avatar(
          avatar_image_url=avatar.face_image_url,
          garment_image_url=request.garment_image_url
      )
      
      return result
  ```

#### Sprint 9 (Week 13-14): Frontend 아바타 UI

**Day 1-3: 얼굴 등록 & 체형 설정 UI**
- [ ] 얼굴 등록 페이지
  ```typescript
  // app/(main)/avatar/register/page.tsx
  export default function RegisterFacePage() {
    const [faceImage, setFaceImage] = useState<File | null>(null);
    const [preview, setPreview] = useState<string | null>(null);
    
    const handleFileSelect = (file: File) => {
      setFaceImage(file);
      const reader = new FileReader();
      reader.onload = (e) => setPreview(e.target?.result as string);
      reader.readAsDataURL(file);
    };
    
    const handleSubmit = async () => {
      const formData = new FormData();
      formData.append('file', faceImage!);
      await pythonApi.registerFace(formData);
      router.push('/avatar/setup');
    };
    
    return (
      <div>
        <ImageUpload onSelect={handleFileSelect} />
        {preview && <img src={preview} alt="얼굴 미리보기" />}
        <Button onClick={handleSubmit}>등록하기</Button>
      </div>
    );
  }
  ```
- [ ] 체형 입력 폼
  ```typescript
  // components/features/avatar/BodyParamsForm.tsx
  export function BodyParamsForm({ avatarId }: Props) {
    const [height, setHeight] = useState(170);
    const [weight, setWeight] = useState(60);
    const [bodyType, setBodyType] = useState<'slim' | 'regular' | 'athletic'>('regular');
    
    const handleSubmit = async () => {
      await javaApi.updateBodyParams(avatarId, {
        height,
        weight,
        bodyType,
      });
    };
    
    return (
      <form onSubmit={handleSubmit}>
        <Input
          type="number"
          label="키 (cm)"
          value={height}
          onChange={(e) => setHeight(Number(e.target.value))}
        />
        <Input
          type="number"
          label="몸무게 (kg)"
          value={weight}
          onChange={(e) => setWeight(Number(e.target.value))}
        />
        <Select
          label="체형"
          value={bodyType}
          onChange={setBodyType}
          options={[
            { value: 'slim', label: '슬림' },
            { value: 'regular', label: '일반' },
            { value: 'athletic', label: '운동형' },
          ]}
        />
        <Button type="submit">저장</Button>
      </form>
    );
  }
  ```
- [ ] 아바타 미리보기 컴포넌트
  - 2D 미리보기 (얼굴 이미지)
  - 체형 파라미터 표시

**Day 4-7: 아바타 관리 페이지**
- [ ] 아바타 목록 표시
- [ ] 기본 아바타 설정
- [ ] 아바타 삭제 기능
- [ ] 아바타 수정 기능

#### Sprint 10 (Week 15-16): 3D 뷰어 구현

**Day 1-3: Three.js 통합**
- [ ] Three.js 설치 및 설정
  ```bash
  pnpm add three @react-three/fiber @react-three/drei
  ```
- [ ] 3D 씬 기본 설정
  ```typescript
  // components/features/3d/Scene.tsx
  'use client';
  import { Canvas } from '@react-three/fiber';
  import { OrbitControls, PerspectiveCamera } from '@react-three/drei';
  
  export function Scene3D({ avatarUrl }: Props) {
    return (
      <Canvas>
        <PerspectiveCamera makeDefault position={[0, 0, 5]} />
        <ambientLight intensity={0.5} />
        <directionalLight position={[10, 10, 5]} />
        <AvatarModel url={avatarUrl} />
        <OrbitControls enableZoom enableRotate />
      </Canvas>
    );
  }
  ```
- [ ] GLB/glTF 모델 로더
  ```typescript
  import { useGLTF } from '@react-three/drei';
  
  function AvatarModel({ url }: Props) {
    const { scene } = useGLTF(url);
    return <primitive object={scene} />;
  }
  ```

**Day 4-5: 3D 아바타 렌더링**
- [ ] 아바타 모델 로드
  - GLB 파일 로드
  - 텍스처 적용
  - 애니메이션 (선택사항)
- [ ] 아바타에 의상 적용
  - 의상 텍스처를 아바타에 매핑
  - UV 좌표 조정
- [ ] 조명 설정
  - 환경 조명
  - 그림자 처리

**Day 6-7: 인터랙션 & 캡처**
- [ ] 회전/줌 인터랙션
  ```typescript
  import { OrbitControls } from '@react-three/drei';
  
  <OrbitControls
    enableZoom
    enableRotate
    enablePan
    minDistance={2}
    maxDistance={10}
  />
  ```
- [ ] 멀티 앵글 캡처
  ```typescript
  const captureAngles = async () => {
    const angles = [0, 45, 90, 135, 180, 225, 270, 315];
    const images = [];
    
    for (const angle of angles) {
      // 카메라 각도 조정
      camera.rotation.y = (angle * Math.PI) / 180;
      
      // 스크린샷 캡처
      const image = await captureScreenshot();
      images.push(image);
    }
    
    return images;
  };
  ```
- [ ] 결과 이미지 다운로드
  - 단일 이미지 다운로드
  - 멀티 앵글 이미지 ZIP 다운로드

### Phase 2 마일스톤
```
┌────────────────────────────────────────────────────────────────┐
│  Phase 2 완료 기준                                              │
├────────────────────────────────────────────────────────────────┤
│  ✓ 얼굴 사진으로 개인화 아바타 생성 가능                         │
│  ✓ 체형 설정 (키, 체중, 체형)이 아바타에 반영                    │
│  ✓ 내 아바타에 옷을 입힌 결과물 확인 가능                        │
│  ✓ 3D 뷰어에서 다양한 각도로 확인 가능                          │
└────────────────────────────────────────────────────────────────┘
```

---

## ⚡ Phase 3: 고도화 (지속)

### 목표
> 사용자 경험 개선 및 비즈니스 기능 추가

### 기능 확장

#### 다중 의상 조합
- [ ] 상의 + 하의 조합 API (Python)
  ```python
  @router.post("/tryon/outfit")
  async def tryon_outfit(
      request: OutfitTryOnRequest,
      current_user: User = Depends(get_current_user)
  ):
      # 상의와 하의를 동시에 처리
      result = await ai_pipeline.process_outfit(
          top_image=request.top_image_url,
          bottom_image=request.bottom_image_url,
          person_image=request.person_image_url
      )
      return result
  ```
- [ ] 코디 저장 기능 (Java)
  ```java
  @PostMapping("/outfits")
  public ResponseEntity<OutfitDTO> saveOutfit(
      @RequestBody SaveOutfitRequest request
  ) {
      Outfit outfit = outfitService.saveOutfit(request);
      return ResponseEntity.ok(OutfitDTO.from(outfit));
  }
  ```
- [ ] 코디 공유 기능
  - 공유 링크 생성
  - 공개/비공개 설정
- [ ] SNS 공유 기능 (Frontend)
  - 카카오톡 공유
  - 인스타그램 공유
  - 트위터 공유

#### 즐겨찾기 & 컬렉션
- [ ] 즐겨찾기 API (Java)
- [ ] 컬렉션 관리 API
- [ ] 컬렉션 UI (Frontend)

### 비즈니스 기능

#### 구독 플랜 & 결제
- [ ] 구독 플랜 정의 (Java)
  - Free, Basic, Pro, Unlimited
  - 플랜별 Try-On 제한
- [ ] 결제 시스템 연동
  - PG사 선택 (토스페이먼츠, 아임포트)
  - 결제 API 구현
- [ ] 사용량 추적
  - Redis로 실시간 사용량 카운트
  - PostgreSQL에 일일/월별 통계 저장
- [ ] 사용량 제한 & 알림
  - 제한 도달 시 알림
  - 업그레이드 유도

#### B2B API 제공
- [ ] API Key 인증 시스템
- [ ] 사용량 기반 과금
- [ ] API 문서 (Swagger)
- [ ] Rate Limiting

### 성능 & 품질

#### AI 모델 최적화
- [ ] 모델 파인튜닝
  - 한국인 얼굴 데이터셋으로 파인튜닝
  - 의상 데이터셋 확장
- [ ] 모델 양자화
  - INT8 양자화로 메모리 사용량 감소
  - 처리 속도 향상
- [ ] 배치 처리 최적화
  - 여러 요청을 배치로 처리
  - GPU 활용률 향상

#### 응답 속도 최적화
- [ ] 이미지 캐싱 전략
  - Redis에 결과 이미지 URL 캐싱
  - CDN 활용
- [ ] 데이터베이스 쿼리 최적화
  - 읽기 복제본 구성
  - 쿼리 인덱스 최적화
- [ ] 프론트엔드 최적화
  - 이미지 lazy loading
  - 코드 스플리팅
  - 서비스 워커 (PWA)

#### 이미지 품질 향상
- [ ] 후처리 파이프라인
  - 색상 보정
  - 선명도 향상
  - 배경 제거 개선
- [ ] A/B 테스트
  - 모델 버전 비교
  - 사용자 피드백 수집

### AR 기능 (선택)

#### 실시간 카메라 Try-On
- [ ] 웹캠 접근 (Frontend)
  ```typescript
  const stream = await navigator.mediaDevices.getUserMedia({
    video: { width: 1280, height: 720 }
  });
  ```
- [ ] 실시간 처리 (Python)
  - WebSocket 연결
  - 프레임별 처리
  - 스트리밍 응답
- [ ] 모바일 AR 앱 (향후)
  - React Native 또는 Flutter
  - ARCore/ARKit 통합

---

## 👥 역할별 업무 분담

### 1인 개발 시
모든 업무를 순차적으로 진행
- **우선순위**: Backend (Java) → Backend (Python) → AI Pipeline → Frontend
- **기술 스택별 작업 순서**:
  1. PostgreSQL 스키마 구축
  2. Java Backend (비즈니스 로직)
  3. Python Backend (AI 연동)
  4. Frontend (Next.js)
  5. Docker 통합
  6. 배포

### 2인 개발 시

| 역할 | 담당 영역 | 기술 스택 |
|------|----------|-----------|
| **개발자 A** | Backend (Java + Python), DB, AI Pipeline, 인프라 | Java, Python, PostgreSQL, Docker |
| **개발자 B** | Frontend, 3D 렌더링, UI/UX | Node.js, Next.js, Three.js, TypeScript |

**협업 포인트**:
- API 명세서 공유 (OpenAPI/Swagger)
- 데이터베이스 스키마 공유
- Docker Compose로 로컬 환경 통일

### 3인 개발 시

| 역할 | 담당 영역 | 기술 스택 |
|------|----------|-----------|
| **개발자 A** | Backend (Java), DB, 인프라 | Java, Spring Boot, PostgreSQL, Docker |
| **개발자 B** | Frontend, UI/UX, 3D | Node.js, Next.js, Three.js, TypeScript |
| **개발자 C** | Backend (Python), AI/ML Pipeline, 모델 최적화 | Python, FastAPI, PyTorch, Celery |

**협업 포인트**:
- Java ↔ Python API 통신 규격 정의
- 데이터베이스 스키마 공유
- API 문서화 (Swagger)
- Docker Compose로 통합 환경 구축

---

## 📊 진행 상태 추적

### 칸반 보드 구조

```
┌─────────────┬─────────────┬─────────────┬─────────────┐
│  Backlog    │  In Progress │   Review   │    Done     │
├─────────────┼─────────────┼─────────────┼─────────────┤
│             │             │             │             │
│  [ Task ]   │  [ Task ]   │  [ Task ]   │  [ Task ]   │
│  [ Task ]   │  [ Task ]   │             │  [ Task ]   │
│  [ Task ]   │             │             │  [ Task ]   │
│             │             │             │             │
└─────────────┴─────────────┴─────────────┴─────────────┘
```

### 권장 도구
- **GitHub Projects**: 코드와 통합된 이슈 관리
- **Linear**: 스타트업 친화적, 빠른 UI
- **Notion**: 문서 + 태스크 통합

---

## ⚠️ 리스크 & 대응

| 리스크 | 영향도 | 대응 방안 |
|--------|--------|----------|
| AI 모델 품질 미달 | 높음 | PoC에서 충분히 검증, 대안 모델 준비 |
| GPU 비용 초과 | 중간 | 캐싱 활용, 배치 처리, 사용량 제한 |
| 개발 지연 | 중간 | MVP 범위 축소, 우선순위 조정 |
| 서버 장애 | 중간 | 모니터링, 자동 복구, 백업 |

---

## 📈 KPI (성과 지표)

### Phase 1 목표
- 일일 활성 사용자 (DAU): 100명
- 회원 가입 전환율: 30%
- Try-On 완료율: 80%

### Phase 2 목표
- DAU: 500명
- 아바타 생성률: 50%
- 재방문율: 40%

---

---

## 🛠️ 기술 스택별 상세 작업

### Python (FastAPI) - AI 연동
- **역할**: AI 모델 연동, 이미지 처리, 비동기 작업 큐
- **주요 작업**:
  - AI 모델 래퍼 개발 (SAM, IDM-VTON, MediaPipe)
  - Celery 작업 큐 설정
  - 이미지 업로드/처리
  - Try-On API 구현
  - GPU 서버 배포

### Java (Spring Boot) - 비즈니스 로직
- **역할**: 사용자 관리, 데이터 처리, 비즈니스 로직
- **주요 작업**:
  - 인증/인가 시스템
  - 사용자 CRUD API
  - 의상 관리 API
  - 아바타 관리 API
  - 구독/결제 시스템

### Node.js (Next.js) - Frontend
- **역할**: 사용자 인터페이스, 3D 렌더링
- **주요 작업**:
  - UI 컴포넌트 개발
  - API 클라이언트 (Java + Python)
  - 3D 뷰어 (Three.js)
  - 상태 관리 (Zustand)
  - 서버 사이드 렌더링

### PostgreSQL - 데이터베이스
- **역할**: 메인 데이터 저장소
- **주요 작업**:
  - 스키마 설계 및 마이그레이션
  - 인덱스 최적화
  - 백업 전략
  - 읽기 복제본 구성 (Phase 3)

### Docker - 컨테이너화
- **역할**: 개발/배포 환경 통일
- **주요 작업**:
  - 각 서비스 Dockerfile 작성
  - Docker Compose 통합
  - 프로덕션 배포 설정
  - CI/CD 파이프라인

---

## 📝 개발 환경 설정 요약

### 필수 설치 항목
1. **Python 3.11+**
   - 가상환경: `python -m venv venv`
   - 패키지 관리: `pip install -r requirements.txt`

2. **Java 17+**
   - 빌드 도구: Maven 또는 Gradle
   - IDE: IntelliJ IDEA (권장) 또는 VS Code

3. **Node.js 20+**
   - 패키지 관리: `pnpm` (권장) 또는 `npm`
   - 프레임워크: Next.js 14

4. **PostgreSQL 16**
   - 로컬 설치 또는 Docker 사용

5. **Docker & Docker Compose**
   - 모든 서비스를 컨테이너로 실행

### 로컬 개발 환경 실행
```bash
# 1. 환경 변수 설정
cp .env.example .env
# .env 파일 수정

# 2. Docker Compose로 모든 서비스 실행
docker-compose up -d

# 3. 데이터베이스 마이그레이션
# Python
cd backend-python
alembic upgrade head

# Java (자동 실행)
# Flyway가 애플리케이션 시작 시 자동 실행

# 4. 서비스 확인
# Frontend: http://localhost:3000
# Java API: http://localhost:8080
# Python API: http://localhost:8000
# PostgreSQL: localhost:5432
# Redis: localhost:6379
```

---

*이 문서는 초안이며, 진행 상황에 따라 업데이트됩니다.*

