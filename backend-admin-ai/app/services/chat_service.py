"""
Chat Service - 프롬프트 의도 분류 + DB 조회 + LLM 호출 오케스트레이션

흐름:
1. 사용자 프롬프트 의도 분류 (키워드 기반)
2. 의도에 따라 quantum_service DB 조회 (READ ONLY)
3. 조회 결과 + 프롬프트를 LLM에 전달
4. LLM 응답 반환
"""
import re
from datetime import datetime, timedelta
from sqlalchemy.orm import Session
from app.core.database import execute_query
from app.services.ollama_client import llm_client


# 의도 분류 키워드 맵
INTENT_KEYWORDS = {
    "payment_analysis": [
        "결제", "매출", "거래", "수익", "payment", "revenue",
        "환불", "구독료", "과금", "청구",
    ],
    "user_statistics": [
        "사용자", "가입", "유저", "구독자", "user", "회원",
        "탈퇴", "활성", "비활성", "신규",
    ],
    "knowledge_query": [
        "지식", "법령", "법률", "세법", "knowledge", "규정",
        "부가가치세", "소득세", "법인세", "다트", "공시",
    ],
}

# 월 이름 → 숫자 변환
MONTH_MAP = {
    "1월": 1, "2월": 2, "3월": 3, "4월": 4,
    "5월": 5, "6월": 6, "7월": 7, "8월": 8,
    "9월": 9, "10월": 10, "11월": 11, "12월": 12,
    "이번 달": datetime.now().month,
    "지난 달": (datetime.now().replace(day=1) - timedelta(days=1)).month,
    "이번달": datetime.now().month,
    "지난달": (datetime.now().replace(day=1) - timedelta(days=1)).month,
}

SYSTEM_PROMPT = """당신은 Quantum Studio의 Admin AI 어시스턴트입니다.
관리자가 시스템 데이터를 분석하고 관리 업무를 지원하도록 돕습니다.

규칙:
- 오직 한국어로만 응답합니다. 영어, 일본어, 베트남어 등 다른 언어나 잘못된 문자를 섞지 마세요.
- 데이터가 제공되면 그 데이터를 기반으로 정확히 분석합니다. 제공된 링크(URL)가 있으면 답변에 그대로 포함하세요.
- 데이터가 없으면 일반적인 가이드를 제공합니다.
- 간결하고 구조화된 형태로 응답합니다 (표, 목록 등 활용).
- 민감 정보(비밀번호, 토큰 등)는 절대 노출하지 않습니다.
"""


class ChatService:
    """프롬프트 처리 오케스트레이터"""

    def __init__(self, db: Session):
        self.db = db

    async def process_prompt(self, prompt: str, context: str | None = None, category: str | None = None) -> dict:
        """
        프롬프트 처리 메인 로직:
        1. 카테고리 사용 (프론트에서 전달) 또는 의도 분류
        2. DB 조회 (필요 시)
        3. LLM 호출
        """
        valid_intents = {"payment_analysis", "user_statistics", "knowledge_query", "general"}
        intent = category if category in valid_intents else self._classify_intent(prompt)
        data_summary = None

        # 의도에 따라 DB 조회
        if intent == "payment_analysis":
            data_summary = self._query_payments(prompt)
        elif intent == "user_statistics":
            data_summary = self._query_users(prompt)
        elif intent == "knowledge_query":
            data_summary = self._query_knowledge(prompt)

        # LLM 프롬프트 조합
        full_prompt = self._build_llm_prompt(prompt, intent, data_summary, context)

        # LLM 호출
        llm_result = await llm_client.generate(full_prompt, system_prompt=SYSTEM_PROMPT)

        return {
            "answer": llm_result["text"],
            "intent": intent,
            "data_summary": data_summary,
            "model_used": llm_result["model"],
        }

    def _classify_intent(self, prompt: str) -> str:
        """키워드 기반 의도 분류"""
        prompt_lower = prompt.lower()
        scores = {}

        for intent, keywords in INTENT_KEYWORDS.items():
            score = sum(1 for kw in keywords if kw in prompt_lower)
            if score > 0:
                scores[intent] = score

        if scores:
            return max(scores, key=scores.get)
        return "general"

    def _extract_month(self, prompt: str) -> int | None:
        """프롬프트에서 월 정보 추출"""
        for label, month in MONTH_MAP.items():
            if label in prompt:
                return month
        # 숫자 + 월 패턴
        match = re.search(r"(\d{1,2})\s*월", prompt)
        if match:
            m = int(match.group(1))
            if 1 <= m <= 12:
                return m
        return None

    def _extract_year(self, prompt: str) -> int:
        """프롬프트에서 연도 추출 (없으면 현재 연도)"""
        match = re.search(r"(20\d{2})\s*년", prompt)
        if match:
            return int(match.group(1))
        return datetime.now().year

    # =============================
    # DB 조회 메서드 (READ ONLY)
    # =============================

    def _query_payments(self, prompt: str) -> str:
        """결제 데이터 조회"""
        month = self._extract_month(prompt)
        year = self._extract_year(prompt)

        try:
            if month:
                rows = execute_query(
                    self.db,
                    """
                    SELECT 
                        COUNT(*) as total_count,
                        COALESCE(SUM(amount), 0) as total_amount,
                        COALESCE(AVG(amount), 0) as avg_amount,
                        MIN(amount) as min_amount,
                        MAX(amount) as max_amount,
                        status,
                        COUNT(*) as status_count
                    FROM payments
                    WHERE EXTRACT(YEAR FROM created_at) = :year
                      AND EXTRACT(MONTH FROM created_at) = :month
                    GROUP BY status
                    """,
                    {"year": year, "month": month},
                )
                if rows:
                    summary_parts = [f"[{year}년 {month}월 결제 데이터]"]
                    for row in rows:
                        summary_parts.append(
                            f"- 상태 '{row['status']}': {row['status_count']}건, "
                            f"총액 {row['total_amount']:,.0f}원, 평균 {row['avg_amount']:,.0f}원"
                        )
                    return "\n".join(summary_parts)
                return f"{year}년 {month}월 결제 데이터가 없습니다."
            else:
                rows = execute_query(
                    self.db,
                    """
                    SELECT 
                        EXTRACT(MONTH FROM created_at)::int as month,
                        COUNT(*) as count,
                        COALESCE(SUM(amount), 0) as total
                    FROM payments
                    WHERE EXTRACT(YEAR FROM created_at) = :year
                    GROUP BY EXTRACT(MONTH FROM created_at)
                    ORDER BY month
                    """,
                    {"year": year},
                )
                if rows:
                    summary_parts = [f"[{year}년 월별 결제 요약]"]
                    for row in rows:
                        summary_parts.append(f"- {int(row['month'])}월: {row['count']}건, {row['total']:,.0f}원")
                    return "\n".join(summary_parts)
                return f"{year}년 결제 데이터가 없습니다."
        except Exception as e:
            return f"결제 데이터 조회 중 오류: {str(e)}"

    def _query_users(self, prompt: str) -> str:
        """사용자 통계 조회"""
        try:
            rows = execute_query(
                self.db,
                """
                SELECT 
                    COUNT(*) as total_users,
                    COUNT(CASE WHEN created_at >= NOW() - INTERVAL '7 days' THEN 1 END) as new_this_week,
                    COUNT(CASE WHEN created_at >= NOW() - INTERVAL '30 days' THEN 1 END) as new_this_month,
                    MIN(created_at) as first_user,
                    MAX(created_at) as latest_user
                FROM users
                """,
            )
            if rows and rows[0]["total_users"] > 0:
                r = rows[0]
                return (
                    f"[사용자 통계]\n"
                    f"- 전체 사용자: {r['total_users']}명\n"
                    f"- 최근 7일 신규: {r['new_this_week']}명\n"
                    f"- 최근 30일 신규: {r['new_this_month']}명\n"
                    f"- 최초 가입: {r['first_user']}\n"
                    f"- 최근 가입: {r['latest_user']}"
                )
            return "사용자 데이터가 없습니다."
        except Exception as e:
            return f"사용자 데이터 조회 중 오류: {str(e)}"

    def _query_knowledge(self, prompt: str) -> str:
        """지식 베이스 조회"""
        try:
            # 검색어 추출: 고정 키워드 또는 프롬프트에서 법령/규정 관련 구문
            search_terms = []
            for keyword in ["부가가치세", "소득세", "법인세", "세법", "법령", "공시", "다트"]:
                if keyword in prompt:
                    search_terms.append(keyword)
            # 고정 키워드 없으면 프롬프트에서 2자 이상 단어 추출
            if not search_terms and len(prompt.strip()) >= 2:
                stopwords = {"알려주", "알려줘", "뭐", "무엇", "어떻게", "어때", "같아", "있어", "인가요", "해주", "있나", "주세요"}
                words = re.findall(r"[가-힣0-9ㆍ·]{2,}", prompt)
                search_terms = [w for w in words if w not in stopwords and len(w) >= 2][:3]

            if search_terms:
                search_pattern = "%".join(search_terms)
                rows = execute_query(
                    self.db,
                    """
                    SELECT id, category, title,
                           COALESCE(LEFT(article_body, 2000), LEFT(content, 500)) as content_preview,
                           source_type, source_url, updated_at
                    FROM knowledge_base
                    WHERE is_active = TRUE
                      AND (title ILIKE :pattern 
                           OR content ILIKE :pattern
                           OR article_body ILIKE :pattern
                           OR category ILIKE :pattern)
                    ORDER BY updated_at DESC
                    LIMIT 10
                    """,
                    {"pattern": f"%{search_pattern}%"},
                )
            else:
                rows = execute_query(
                    self.db,
                    """
                    SELECT id, category, title,
                           COALESCE(LEFT(article_body, 2000), LEFT(content, 500)) as content_preview,
                           source_type, source_url, updated_at
                    FROM knowledge_base
                    WHERE is_active = TRUE
                    ORDER BY updated_at DESC
                    LIMIT 10
                    """,
                )

            if rows:
                summary_parts = [f"[지식 베이스 검색 결과: {len(rows)}건]"]
                for row in rows:
                    url = row.get("source_url") or ""
                    url_part = f" | 링크: {url}" if url else ""
                    summary_parts.append(
                        f"- [{row['category']}] {row['title']}\n"
                        f"  출처: {row['source_type']}{url_part} | 수정: {row['updated_at']}\n"
                        f"  내용: {row['content_preview']}"
                    )
                return "\n".join(summary_parts)
            return "관련 지식 데이터가 없습니다."
        except Exception as e:
            return f"지식 데이터 조회 중 오류: {str(e)}"

    def _build_llm_prompt(
        self,
        user_prompt: str,
        intent: str,
        data_summary: str | None,
        context: str | None,
    ) -> str:
        """LLM에 전달할 최종 프롬프트 생성"""
        parts = []

        intent_labels = {
            "payment_analysis": "결제 분석",
            "user_statistics": "사용자 통계",
            "knowledge_query": "지식 조회",
            "general": "일반 질문",
        }
        parts.append(f"[분류된 의도: {intent_labels.get(intent, intent)}]")

        if data_summary:
            parts.append(f"\n--- 데이터베이스 조회 결과 ---\n{data_summary}\n---")

        if context:
            parts.append(f"\n[추가 컨텍스트]\n{context}")

        parts.append(f"\n[관리자 질문]\n{user_prompt}")
        parts.append("\n위 데이터를 기반으로 분석하고, 반드시 한국어로만 답변해주세요. 제공된 링크가 있으면 [법령명](URL) 형태로 활용하세요.")

        return "\n".join(parts)
