"""
매핑 서비스 (고도화 버전)
- 데이터의 성격(구조화 vs 비구조화)을 파악하여 
  각기 다른 3D 매핑 전략을 적용합니다.
"""
from typing import Any, Dict, List, Optional
import math
import json
from app.services.ai_agent_service import ai_agent_service

class MappingOrchestrator:
    """
    AI 매핑 오케스트레이터
    - 입력 데이터가 JSON(구조화)인지 일반 텍스트(비구조화)인지 판별하여 전략을 결정합니다.
    """

    async def process_data_to_3d(self, data_type: str, raw_data: Any) -> Dict[str, Any]:
        # 1. 엑셀/CSV 데이터 처리 (정산 등 리스트 형태)
        if isinstance(raw_data, list):
            return self._handle_settlement_visualization(raw_data)

        # 2. 문서 분석 요청인 경우 (AI Agent 가동)
        if data_type == "document_analysis" or isinstance(raw_data, str):
            text_content = raw_data if isinstance(raw_data, str) else str(raw_data)
            ai_result = await ai_agent_service.analyze_document(text_content)
            return self._handle_ai_result_to_diagram(ai_result)

        # 2. 구조화된 데이터(Dict)인 경우 기존 다이어그램 로직 실행
        if isinstance(raw_data, dict):
            return self._handle_json_diagram_visualization(raw_data)
        
        return self._handle_generic_visualization(raw_data)

    def _handle_settlement_visualization(self, data: List[Dict[str, Any]]) -> Dict[str, Any]:
        """엑셀 정산 데이터를 3D 막대 그래프 및 관계도로 변환"""
        nodes = []
        links = []
        
        # 합계 계산
        total_amount = 0
        for row in data:
            # '금액', 'amount', 'price' 등 키워드 탐색
            for k, v in row.items():
                if any(key in k.lower() for key in ['금액', 'amount', 'price', '수량']):
                    try: total_amount += float(v)
                    except: pass

        # 1. 중앙 총계 노드 (거대 큐브)
        nodes.append({
            "id": "total",
            "label": "TOTAL_SETTLEMENT",
            "value": f"{total_amount:,.0f}",
            "pos": [0, 0, 0],
            "type": "root",
            "color": "#10b981" # 성공/정산의 초록색
        })

        # 2. 개별 항목 노드 (데이터 양에 따라 배치)
        for i, row in enumerate(data[:50]): # 최대 50개까지만 표시 (성능)
            node_id = f"item_{i}"
            # 행 데이터의 대표값 찾기 (이름, 항목 등)
            label = next((str(v) for k, v in row.items() if any(key in k.lower() for key in ['명', '항목', 'item', 'name'])), f"Item {i}")
            
            # 값에 따른 높이(Y) 설정 (막대 그래프 효과)
            val = 0
            for v in row.values():
                try: 
                    val = float(v)
                    break
                except: pass

            angle = (i / len(data[:50])) * math.pi * 2 if len(data[:50]) > 0 else 0
            radius = 15
            nodes.append({
                "id": node_id,
                "label": label,
                "value": row,
                "pos": [math.cos(angle) * radius, val / 1000 if val > 0 else 2, math.sin(angle) * radius],
                "type": "data",
                "color": "#38bdf8"
            })
            links.append({"source": "total", "target": node_id})

        return {
            "render_type": "settlement",
            "nodes": nodes,
            "links": links,
            "summary": {
                "total_count": len(data),
                "total_sum": total_amount
            }
        }

    def _handle_ai_result_to_diagram(self, ai_result: Dict[str, Any]) -> Dict[str, Any]:
        """AI 분석 결과(요약, 키워드, 관계)를 3D 다이어그램 구조로 변환"""
        nodes = []
        links = []
        
        # 1. 중앙 요약 노드 (Root)
        nodes.append({
            "id": "root_summary",
            "label": "AI SUMMARY",
            "value": ai_result.get("summary", "No summary available"),
            "pos": [0, 0, 0],
            "type": "root",
            "color": "#fbbf24" # 노란색 (중요)
        })

        # 2. 키워드 노드 배치 (구형 분산)
        keywords = ai_result.get("keywords", [])
        node_count = len(keywords)
        phi = math.pi * (3. - math.sqrt(5.))
        radius = 15

        for i, kw in enumerate(keywords):
            y_base = 1 - (i / float(node_count - 1)) * 2 if node_count > 1 else 0
            rad_at_y = math.sqrt(1 - y_base * y_base)
            theta = phi * i
            pos = [math.cos(theta) * rad_at_y * radius, y_base * radius, math.sin(theta) * rad_at_y * radius]

            nodes.append({
                "id": f"kw_{i}",
                "label": kw,
                "value": kw,
                "pos": pos,
                "type": "data",
                "color": "#00f2ff"
            })
            # 요약 노드와 모든 키워드 연결
            links.append({"source": "root_summary", "target": f"kw_{i}"})

        # 3. 키워드 간 관계 연결
        for rel in ai_result.get("relations", []):
            # 관계 텍스트를 기반으로 노드 매칭 (간단한 매칭 로직)
            source_node = next((n["id"] for n in nodes if n["label"] in rel["source"]), None)
            target_node = next((n["id"] for n in nodes if n["label"] in rel["target"]), None)
            if source_node and target_node:
                links.append({
                    "source": source_node, 
                    "target": target_node, 
                    "label": rel.get("label", "related")
                })
            
        return {
            "render_type": "ai_analysis",
            "nodes": nodes,
            "links": links
        }

    def _handle_json_diagram_visualization(self, data: Dict) -> Dict[str, Any]:
        """기존 JSON 다이어그램 로직"""
        nodes = []
        links = []
        node_count = len(data)
        phi = math.pi * (3. - math.sqrt(5.))
        radius = max(15, math.sqrt(node_count) * 8)

        nodes.append({"id": "root", "label": "Data Root", "pos": [0, 0, 0], "type": "root", "color": "#ffffff"})

        for i, (key, value) in enumerate(data.items()):
            node_id = f"node_{i}"
            val_str = str(value)
            is_image = val_str.startswith("http") and any(val_str.lower().endswith(ext) for ext in [".jpg", ".png", ".webp", ".jpeg"])
            
            y_base = 1 - (i / float(node_count - 1)) * 2 if node_count > 1 else 0
            rad_at_y = math.sqrt(1 - y_base * y_base)
            theta = phi * i
            pos = [math.cos(theta) * rad_at_y * radius, y_base * radius, math.sin(theta) * rad_at_y * radius]

            nodes.append({"id": node_id, "label": key, "value": val_str, "pos": pos, "type": "image" if is_image else "data", "color": "#00f2ff"})
            links.append({"source": "root", "target": node_id})
            
        return {"render_type": "diagram", "nodes": nodes, "links": links}

    def _handle_generic_visualization(self, data: Any) -> Dict[str, Any]:
        return {"render_type": "monolith", "content": str(data), "stats": {"length": len(str(data)), "lines": 1}}

mapping_orchestrator = MappingOrchestrator()
