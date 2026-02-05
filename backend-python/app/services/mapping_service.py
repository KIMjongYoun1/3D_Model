"""
매핑 서비스 (고도화 버전)
- 데이터의 성격(구조화 vs 비구조화)을 파악하여 
  각기 다른 3D 매핑 전략을 적용합니다.
"""
from typing import Any, Dict, List, Optional
import math
import json
from sqlalchemy.orm import Session
from app.services.ai_agent_service import ai_agent_service
from app.models.correlation import CorrelationRule

class MappingOrchestrator:
    """
    AI 매핑 오케스트레이터
    - 입력 데이터가 JSON(구조화)인지 일반 텍스트(비구조화)인지 판별하여 전략을 결정합니다.
    """

    async def process_data_to_3d(self, data_type: str, raw_data: Any, db: Optional[Session] = None, options: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
        options = options or {}
        render_type = options.get("render_type", "auto")

        # 1. 기본 시각화 데이터 생성
        result = await self._generate_base_mapping(data_type, raw_data, options)
        
        # 2. DB 기반 로컬 상관관계 분석 (확대 해석)
        if db and "nodes" in result:
            local_links = self._analyze_local_correlations(db, result["nodes"])
            result["links"].extend(local_links)
            
        return result

    async def _generate_base_mapping(self, data_type: str, raw_data: Any, options: Dict[str, Any]) -> Dict[str, Any]:
        render_type = options.get("render_type", "auto")
        
        if render_type == "settlement" or isinstance(raw_data, list):
            return self._handle_settlement_visualization(raw_data)
        
        if render_type == "diagram" and isinstance(raw_data, dict):
            return self._handle_json_diagram_visualization(raw_data)

        if data_type == "document_analysis" or isinstance(raw_data, str):
            text_content = raw_data if isinstance(raw_data, str) else str(raw_data)
            ai_result = await ai_agent_service.analyze_document(text_content, options)
            
            if render_type == "settlement" or ai_result.get("suggested_render") == "settlement":
                return self._handle_settlement_visualization(ai_result.get("table_data", []))
            
            return self._handle_ai_result_to_diagram(ai_result)

        if isinstance(raw_data, dict):
            return self._handle_json_diagram_visualization(raw_data)
        
        return self._handle_generic_visualization(raw_data)

    def _analyze_local_correlations(self, db: Session, nodes: List[Dict]) -> List[Dict]:
        """DB에 정의된 규칙을 바탕으로 노드 간 상관관계를 확대 해석 (지능형 강도 판단 및 로깅 추가)"""
        new_links = []
        rules = db.query(CorrelationRule).filter(CorrelationRule.is_active == True).all()
        
        print(f"\n🔍 [Correlation Engine] 로컬 상관관계 분석 시작 (노드 수: {len(nodes)}, 규칙 수: {len(rules)})")
        
        for i, node_a in enumerate(nodes):
            for node_b in nodes[i+1:]:
                a_label = node_a.get('label', '')
                b_label = node_b.get('label', '')
                
                a_text = str(a_label + " " + str(node_a.get('value', ''))).lower()
                b_text = str(b_label + " " + str(node_b.get('value', ''))).lower()

                for rule in rules:
                    keywords = rule.keywords
                    # 매칭된 키워드 추출
                    matches_a = [k for k in keywords if k.lower() in a_text]
                    matches_b = [k for k in keywords if k.lower() in b_text]
                    
                    if matches_a and matches_b:
                        # --- [지능형 강도 판단 로직] ---
                        base_strength = rule.strength
                        
                        # 보너스 1: 키워드 중복 매칭 (다양한 키워드가 겹칠수록 강함)
                        keyword_bonus = min(3, (len(matches_a) + len(matches_b)) // 2)
                        
                        # 보너스 2: 레이블 직접 일치 (이름 자체가 키워드를 포함하면 매우 강함)
                        label_bonus = 0
                        if any(k.lower() in a_label.lower() for k in keywords) and \
                           any(k.lower() in b_label.lower() for k in keywords):
                            label_bonus = 2
                            
                        final_strength = min(10, base_strength + keyword_bonus + label_bonus)
                        
                        print(f"  └─ ✨ 매칭 발견: [{a_label}] ↔ [{b_label}]")
                        print(f"     - 카테고리: {rule.category}, 매칭키워드: {list(set(matches_a + matches_b))}")
                        print(f"     - 강도계산: 기본({base_strength}) + 키워드({keyword_bonus}) + 레이블({label_bonus}) = 최종({final_strength})")

                        new_links.append({
                            "source": node_a['id'],
                            "target": node_b['id'],
                            "label": rule.label or f"Inferred:{rule.category}",
                            "strength": final_strength
                        })
                        break 
        
        print(f"✅ [Correlation Engine] 분석 완료: 총 {len(new_links)}개의 새로운 관계 생성\n")
        return new_links

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

        for i, kw_data in enumerate(keywords):
            # kw_data가 딕셔너리인지 문자열인지 체크 (하위 호환성)
            term = kw_data.get("term", "Unknown") if isinstance(kw_data, dict) else kw_data
            raw_value = kw_data.get("value", "") if isinstance(kw_data, dict) else ""
            definition = kw_data.get("definition", "") if isinstance(kw_data, dict) else term
            importance = kw_data.get("importance", 5) if isinstance(kw_data, dict) else 5

            # 팝업에 표시될 최종 텍스트 구성 (키워드 : 벨류 : 해석)
            display_value = f"[{term}]\n● 데이터: {raw_value}\n● 해석: {definition}" if raw_value else definition

            y_base = 1 - (i / float(node_count - 1)) * 2 if node_count > 1 else 0
            rad_at_y = math.sqrt(1 - y_base * y_base)
            theta = phi * i
            pos = [math.cos(theta) * rad_at_y * radius, y_base * radius, math.sin(theta) * rad_at_y * radius]

            nodes.append({
                "id": f"kw_{i}",
                "label": term,
                "value": display_value, 
                "pos": pos,
                "type": "data",
                "importance": importance,
                "references": kw_data.get("references", []), # 참고 자료 링크 추가
                "color": "#00f2ff"
            })
            # 요약 노드와 모든 키워드 연결
            links.append({"source": "root_summary", "target": f"kw_{i}"})

        # 3. 키워드 간 관계 연결
        for rel in ai_result.get("relations", []):
            # 관계 텍스트를 기반으로 노드 매칭
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
        """구조화된 JSON 데이터를 3D 노드로 변환 (지능형 배열 감지 추가)"""
        nodes = []
        links = []
        
        # [개선] 만약 JSON 내부에 'nodes' 배열이 있다면, 해당 배열의 아이템들을 개별 노드로 처리
        target_items = []
        if "nodes" in data and isinstance(data["nodes"], list):
            target_items = data["nodes"]
            # 링크 정보도 있으면 가져옴
            if "links" in data and isinstance(data["links"], list):
                links = data["links"]
        else:
            # 기존 방식: 최상위 키-벨류를 노드로 변환
            for k, v in data.items():
                target_items.append({"id": k, "label": k, "value": v})

        node_count = len(target_items)
        phi = math.pi * (3. - math.sqrt(5.))
        radius = max(15, math.sqrt(node_count) * 8)

        for i, item in enumerate(target_items):
            # 딕셔너리 형태면 값 추출, 아니면 기본값 생성
            if isinstance(item, dict):
                node_id = str(item.get("id", f"node_{i}"))
                label = str(item.get("label") or item.get("name") or node_id)
                value = item.get("value") or item
            else:
                node_id = f"node_{i}"
                label = f"Item_{i}"
                value = item

            val_str = str(value)
            is_image = val_str.startswith("http") and any(val_str.lower().endswith(ext) for ext in [".jpg", ".png", ".webp", ".jpeg"])
            
            y_base = 1 - (i / float(node_count - 1)) * 2 if node_count > 1 else 0
            rad_at_y = math.sqrt(1 - y_base * y_base)
            theta = phi * i
            pos = [math.cos(theta) * rad_at_y * radius, y_base * radius, math.sin(theta) * rad_at_y * radius]

            nodes.append({
                "id": node_id, 
                "label": label, 
                "value": value, 
                "pos": pos, 
                "type": "image" if is_image else "data", 
                "color": "#00f2ff"
            })
            
            # nodes 배열 모드가 아닐 때만 root와 연결
            if not ("nodes" in data and isinstance(data["nodes"], list)):
                if not any(n["id"] == "root" for n in nodes):
                    nodes.append({"id": "root", "label": "Data Root", "pos": [0, 0, 0], "type": "root", "color": "#ffffff"})
                links.append({"source": "root", "target": node_id})
            
        return {"render_type": "diagram", "nodes": nodes, "links": links}

    def _handle_generic_visualization(self, data: Any) -> Dict[str, Any]:
        return {"render_type": "monolith", "content": str(data), "stats": {"length": len(str(data)), "lines": 1}}

mapping_orchestrator = MappingOrchestrator()
