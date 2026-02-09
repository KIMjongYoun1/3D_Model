"""
Quantum Studio 카테고리 체계 정의
- 데이터의 성격에 따라 세분화된 카테고리를 정의합니다.
- 각 카테고리는 감지를 위한 키워드와 권장 모델 티어를 가집니다.
"""
from enum import Enum
from typing import List, Dict, Any

class ModelTier(Enum):
    LOCAL = "local"      # TinyLlama (비용 0, 속도 최상)
    FLASH = "flash"      # Gemini Flash (가성비, 고속)
    PRO = "pro"          # Gemini Pro + RAG (고성능, 전문 추론)

class CategoryInfo:
    def __init__(self, name: str, tier: ModelTier, keywords: List[str], description: str):
        self.name = name
        self.tier = tier
        self.keywords = keywords
        self.description = description

# 세분화된 카테고리 정의
CATEGORIES = {
    "FINANCE_TAX": CategoryInfo(
        "FINANCE_TAX", 
        ModelTier.PRO, 
        ["세금", "부가세", "vat", "과세", "면세", "세무", "공제"],
        "세무 및 세금 관련 데이터 분석 (전문 지식 필요)"
    ),
    "FINANCE_SETTLEMENT": CategoryInfo(
        "FINANCE_SETTLEMENT", 
        ModelTier.FLASH, 
        ["정산", "입금", "출금", "계좌", "송금", "거래내역", "결제"],
        "일반적인 정산 및 금융 거래 분석"
    ),
    "INFRA_ARCHITECTURE": CategoryInfo(
        "INFRA_ARCHITECTURE", 
        ModelTier.PRO, 
        ["kubernetes", "k8s", "docker", "aws", "vpc", "subnet", "msa", "아키텍처"],
        "복잡한 클라우드/인프라 구조 분석"
    ),
    "INFRA_LOG": CategoryInfo(
        "INFRA_LOG", 
        ModelTier.LOCAL, 
        ["error", "warn", "info", "debug", "exception", "stacktrace", "로그"],
        "단순 서버 로그 패턴 분석 및 구조화"
    ),
    "LEGAL_COMPLIANCE": CategoryInfo(
        "LEGAL_COMPLIANCE", 
        ModelTier.PRO, 
        ["약관", "준수", "법률", "조항", "규정", "위반", "개인정보"],
        "법률 및 컴플라이언스 체크"
    ),
    "GENERAL_DOC": CategoryInfo(
        "GENERAL_DOC", 
        ModelTier.FLASH, 
        [], 
        "기타 일반 문서 요약 및 분석"
    )
}

def detect_category(text: str) -> str:
    """
    입력 텍스트를 분석하여 가장 적합한 카테고리를 반환합니다. (경량 키워드 매칭)
    """
    text_lower = text.lower()
    best_category = "GENERAL_DOC"
    max_matches = 0
    
    for cat_id, info in CATEGORIES.items():
        if not info.keywords: continue
        matches = sum(1 for kw in info.keywords if kw.lower() in text_lower)
        if matches > max_matches:
            max_matches = matches
            best_category = cat_id
            
    return best_category
