"""
매핑 서비스 (고도화 버전)
- 카테고리 감지 및 AI 에이전트 라우팅 연동
- 한국어 주석 및 설명 포함
"""
from typing import Any, Dict, List, Optional
import math
import json
import time
from sqlalchemy.orm import Session
from app.services.ai_agent_service import ai_agent_service
from app.models.correlation import CorrelationRule
from app.models.mapping import MappingData

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
            
            # 고도화된 AI 에이전트 호출 (카테고리 감지 및 티어링 포함)
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

    def _handle_settlement_visualization(self, data: List[Dict[str, Any]]) -> Dict[str, Any]:
        """정산 데이터를 3D 막대 그래프 형태로 변환"""
        nodes = []
        links = []
        total_amount = 0
        
        for row in data:
            for k, v in row.items():
                if any(key in k.lower() for key in ['금액', 'amount', 'price']):
                    try: total_amount += float(v)
                    except: pass

        # 중앙 총계 노드
        nodes.append({
            "id": "total",
            "label": "TOTAL",
            "value": f"{total_amount:,.0f}",
            "pos": [0, 0, 0],
            "type": "root",
            "color": "#10b981"
        })

        # 개별 항목 노드 배치
        for i, row in enumerate(data[:50]):
            node_id = f"item_{i}"
            label = next((str(v) for k, v in row.items() if any(key in k.lower() for key in ['명', '항목', 'name'])), f"Item {i}")
            
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
