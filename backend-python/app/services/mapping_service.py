"""
매핑 서비스 (고도화 버전)
- 카테고리 감지 및 AI 에이전트 라우팅 연동
- TSV/CSV 테이블 데이터 자동 파싱 (AI 우회로 즉시 시각화)
- 한국어 주석 및 설명 포함
"""
from typing import Any, Dict, List, Optional
import math
import json
import re
import time
from sqlalchemy.orm import Session
from app.services.ai_agent_service import ai_agent_service
from app.models.correlation import CorrelationRule
from app.models.mapping import MappingData
from app.core.url_sanitizer import sanitize_mapping_result

class MappingOrchestrator:
    """
    AI 매핑 오케스트레이터
    - 데이터의 성격을 분석하여 최적의 3D 시각화 전략과 AI 모델을 선택합니다.
    """

    async def process_data_to_3d(self, data_type: str, raw_data: Any, db: Optional[Session] = None, service_db: Optional[Session] = None, options: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
        """
        데이터 처리 및 3D 매핑 메인 메소드
        
        Args:
            data_type: 데이터 유형
            raw_data: 원본 데이터
            db: AI DB 세션 (quantum_ai) - 상관관계 규칙, 시각화 데이터
            service_db: Service DB 세션 (quantum_service) - knowledge_base RAG 조회용 (READ ONLY)
            options: 처리 옵션
        """
        start_time = time.time()
        options = options or {}
        
        # 1. 기본 시각화 데이터 생성 (AI 분석 포함)
        result = await self._generate_base_mapping(data_type, raw_data, db, service_db, options)
        
        # 2. DB 기반 로컬 상관관계 분석 (AI DB에서 규칙 조회)
        if db and "nodes" in result:
            local_links = self._analyze_local_correlations(db, result["nodes"])
            result.setdefault("links", []).extend(local_links)
            
        # 처리 시간 기록
        processing_time = int((time.time() - start_time) * 1000)
        result["processing_time_ms"] = processing_time

        # references URL 검증 (MITM/조작 방지)
        result = sanitize_mapping_result(result)

        return result

    async def _generate_base_mapping(self, data_type: str, raw_data: Any, db: Optional[Session], service_db: Optional[Session], options: Dict[str, Any]) -> Dict[str, Any]:
        """데이터 소스에 따른 시각화 처리 분기"""
        render_type = options.get("render_type", "auto")
        
        # 이미 구조화된 리스트 데이터 처리 (정산 등)
        if render_type == "settlement" or isinstance(raw_data, list):
            return self._handle_settlement_visualization(raw_data)
        
        # 비구조화 텍스트 또는 문서 분석
        if data_type == "document_analysis" or data_type == "file_analysis" or isinstance(raw_data, str):
            text_content = raw_data if isinstance(raw_data, str) else str(raw_data)
            
            # 탭/쉼표 구분 테이블: 파싱 → AI 분석 → 시각화 (파서로 구조 추출, AI로 인사이트 보강)
            parsed_table = self._try_parse_table_text(text_content)
            if parsed_table is not None:
                try:
                    ai_result = await ai_agent_service.analyze_structured_table(
                        parsed_table, db=db, service_db=service_db, options=options
                    )
                except Exception:
                    ai_result = {"summary": "", "detected_category": "GENERAL_DOC", "model_tier": "none"}
                return self._handle_settlement_with_ai(parsed_table, ai_result)
            
            # 고도화된 AI 에이전트 호출 (비구조화 텍스트, 카테고리 감지 및 티어링 포함)
            # service_db를 통해 knowledge_base RAG를 조회합니다.
            ai_result = await ai_agent_service.analyze_document(text_content, db=db, service_db=service_db, options=options)
            
            # AI가 제안한 렌더링 방식이 정산형인 경우
            if render_type == "settlement" or ai_result.get("suggested_render") == "settlement":
                return self._handle_settlement_visualization(ai_result.get("table_data", []))
            
            # 일반적인 다이어그램 형태로 변환
            return self._handle_ai_result_to_diagram(ai_result)

        # 구조화된 JSON 데이터 처리
        if isinstance(raw_data, dict):
            return self._handle_json_diagram_visualization(raw_data)
        
        # 기타 일반 데이터 처리
        return self._handle_generic_visualization(raw_data)

    def _try_parse_table_text(self, text: str) -> Optional[List[Dict[str, Any]]]:
        """
        탭/쉼표 구분 테이블 텍스트를 파싱하여 [{col:val}, ...] 형태로 반환.
        엑셀·CSV 복사 등 사용자가 아무 테이블이나 붙여넣은 경우 AI 없이 즉시 시각화.
        파싱 실패 시 None. 헤더가 여러 줄로 나뉜 경우도 처리.
        """
        text = text.strip()
        if not text or len(text) < 10:
            return None
        sep = "\t" if "\t" in text[:500] else ("," if "," in text[:500] else None)
        if not sep:
            return None
        lines = [ln.strip() for ln in text.splitlines() if ln.strip()]
        if len(lines) < 2:
            return None
        # 각 줄의 컬럼 수 계산
        def col_count(ln: str) -> int:
            return len([p for p in ln.split(sep) if p.strip() or True])
        # 2개 이상 컬럼을 가진 줄 (엑셀/CSV 복사 등 다양한 형식 지원)
        counts = [(i, col_count(ln)) for i, ln in enumerate(lines) if col_count(ln) >= 2]
        if not counts:
            return None
        max_cols = max(c[1] for c in counts)
        # 동일 컬럼 수를 가진 줄들 (데이터/헤더 후보)
        same_col_lines = [(i, ln) for i, ln in enumerate(lines) if col_count(ln) == max_cols]
        if not same_col_lines:
            return None
        first_idx, first_ln = same_col_lines[0]
        parts = [p.strip() for p in first_ln.split(sep)][:max_cols]
        # 첫 컬럼이 숫자면 데이터 행 → col_0, col_1... 사용
        def looks_numeric(s: str) -> bool:
            s = str(s).strip()
            if not s:
                return False
            try:
                float(s.replace(",", ""))
                return True
            except ValueError:
                return False
        if looks_numeric(parts[0]) if parts else False:
            headers = [f"col_{i}" for i in range(max_cols)]
            data_start = 0
        else:
            headers = [re.sub(r"\s+", " ", p).strip() or f"col_{i}" for i, p in enumerate(parts)]
            data_start = 1
        result = []
        for idx, ln in same_col_lines[data_start:]:
            vals = [p.strip() for p in ln.split(sep)][:max_cols]
            while len(vals) < max_cols:
                vals.append("")
            row = {headers[i]: vals[i] for i in range(max_cols)}
            result.append(row)
        return result if len(result) >= 1 else None

    def _analyze_local_correlations(self, db: Session, nodes: List[Dict]) -> List[Dict]:
        """DB에 정의된 규칙을 바탕으로 노드 간 상관관계를 분석합니다."""
        new_links = []
        rules = db.query(CorrelationRule).filter(CorrelationRule.is_active == True).all()
        
        for i, node_a in enumerate(nodes):
            for node_b in nodes[i+1:]:
                a_text = str(node_a.get('label', '') + " " + str(node_a.get('value', ''))).lower()
                b_text = str(node_b.get('label', '') + " " + str(node_b.get('value', ''))).lower()

                for rule in rules:
                    if any(k.lower() in a_text for k in rule.keywords) and \
                       any(k.lower() in b_text for k in rule.keywords):
                        new_links.append({
                            "source": node_a['id'],
                            "target": node_b['id'],
                            "label": rule.label or f"Inferred:{rule.category}",
                            "strength": rule.strength
                        })
                        break 
        return new_links

    def _handle_settlement_with_ai(self, data: List[Dict[str, Any]], ai_result: Dict[str, Any]) -> Dict[str, Any]:
        """파싱된 테이블 + AI 분석 결과를 합쳐 시각화 (루트에 AI 요약, 노드에 테이블 데이터)"""
        base = self._handle_settlement_visualization(data)
        summary = ai_result.get("summary", "")
        if summary:
            # 루트 노드에 AI 요약 반영
            for n in base.get("nodes", []):
                if n.get("type") == "root":
                    n["label"] = "AI 요약"
                    n["value"] = summary
                    break
        base["detected_category"] = ai_result.get("detected_category")
        base["model_tier"] = ai_result.get("model_tier")
        # AI 키워드/관계가 있으면 링크에 반영 (선택적)
        return base

    def _handle_settlement_visualization(self, data: List[Dict[str, Any]]) -> Dict[str, Any]:
        """테이블 데이터를 3D 막대 그래프 형태로 변환 (금액·기온·점수 등 다양한 형식 지원)"""
        nodes = []
        links = []
        total_amount = 0
        
        # 수치 합산용 컬럼명 키워드 (금액, 기온, 점수 등)
        amount_keys = ['금액', 'amount', 'price', '매출', '매입', '합계', 'value', '수량', '기온', '온도', 'temp', '평균', '최고', '최저', '점수', '점', '합', '계', 'total', 'sum']
        # 라벨용 컬럼명 키워드 (이름, 항목, 일시 등)
        label_keys = ['명', '항목', 'name', '구분', '항목명', '제목', '일시', '지점', '날짜', 'date', '이름', '품목', '항목']
        
        def try_float(v: Any) -> Optional[float]:
            if v is None: return None
            try: return float(str(v).replace(",", "").strip())
            except ValueError: return None
        
        for row in data:
            row_sum = 0
            matched = False
            for k, v in row.items():
                if any(key in k.lower() for key in amount_keys):
                    fv = try_float(v)
                    if fv is not None: row_sum += fv; matched = True
            if not matched:
                for v in row.values():
                    fv = try_float(v)
                    if fv is not None: row_sum += fv; break
            total_amount += row_sum

        # 중앙 총계 노드
        nodes.append({
            "id": "total",
            "label": "TOTAL",
            "value": f"{total_amount:,.0f}" if total_amount != 0 else str(len(data)),
            "pos": [0, 0, 0],
            "type": "root",
            "color": "#10b981"
        })

        # 개별 항목 노드 배치 (라벨: 매칭 컬럼 우선, 없으면 첫 번째 비어있지 않은 값)
        for i, row in enumerate(data[:50]):
            node_id = f"item_{i}"
            label = next((str(v) for k, v in row.items() if any(key in k.lower() for key in label_keys) and v is not None and str(v).strip()), None)
            if label is None:
                label = next((str(v) for v in row.values() if v is not None and str(v).strip()), f"Row {i+1}")
            
            angle = (i / len(data[:50])) * math.pi * 2 if len(data) > 0 else 0
            radius = 15
            nodes.append({
                "id": node_id,
                "label": label,
                "value": row,
                "pos": [math.cos(angle) * radius, 2, math.sin(angle) * radius],
                "type": "data",
                "color": "#38bdf8"
            })
            links.append({"source": "total", "target": node_id})

        return {"render_type": "settlement", "nodes": nodes, "links": links}

    def _handle_ai_result_to_diagram(self, ai_result: Dict[str, Any]) -> Dict[str, Any]:
        """AI 분석 결과를 3D 다이어그램 구조로 변환"""
        nodes = []
        links = []
        
        # 중앙 요약 노드
        nodes.append({
            "id": "root_summary",
            "label": "AI SUMMARY",
            "value": ai_result.get("summary", ""),
            "pos": [0, 0, 0],
            "type": "root",
            "color": "#fbbf24"
        })

        # 키워드 노드 구형 배치
        keywords = ai_result.get("keywords", [])
        node_count = len(keywords)
        phi = math.pi * (3. - math.sqrt(5.))
        radius = 15

        for i, kw in enumerate(keywords):
            term = kw.get("term", "Unknown")
            y = 1 - (i / float(node_count - 1)) * 2 if node_count > 1 else 0
            rad_at_y = math.sqrt(1 - y * y)
            theta = phi * i
            
            nodes.append({
                "id": f"kw_{i}",
                "label": term,
                "value": kw.get("definition", ""),
                "pos": [math.cos(theta) * rad_at_y * radius, y * radius, math.sin(theta) * rad_at_y * radius],
                "type": "data",
                "color": "#00f2ff"
            })
            links.append({"source": "root_summary", "target": f"kw_{i}"})

        # 관계 연결
        for rel in ai_result.get("relations", []):
            source_id = next((n["id"] for n in nodes if n["label"] == rel["source"]), None)
            target_id = next((n["id"] for n in nodes if n["label"] == rel["target"]), None)
            if source_id and target_id:
                links.append({"source": source_id, "target": target_id, "label": rel.get("label", "")})
            
        # 메타데이터 전파
        return {
            "render_type": "ai_analysis",
            "nodes": nodes, 
            "links": links,
            "detected_category": ai_result.get("detected_category"),
            "model_tier": ai_result.get("model_tier")
        }

    def _handle_json_diagram_visualization(self, data: Dict) -> Dict[str, Any]:
        """JSON 데이터를 노드-링크 구조로 변환"""
        nodes = [{"id": "root", "label": "JSON Root", "pos": [0, 0, 0], "type": "root", "color": "#ffffff"}]
        links = []
        # ... (기존 로직 유지)
        return {"render_type": "diagram", "nodes": nodes, "links": links}

    def _handle_generic_visualization(self, data: Any) -> Dict[str, Any]:
        """일반 텍스트 데이터 처리"""
        return {"render_type": "monolith", "content": str(data)}

mapping_orchestrator = MappingOrchestrator()
