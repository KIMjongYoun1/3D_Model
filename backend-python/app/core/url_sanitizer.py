"""
외부 링크 URL 검증 (References, Evidence 등)
- http/https만 허용
- MITM으로 조작된 응답에서 악성 URL 차단
"""
import re
from typing import Any, Dict, List

MAX_URL_LENGTH = 2048


def is_safe_external_url(url: Any) -> bool:
    """http/https URL만 허용"""
    if not url or not isinstance(url, str):
        return False
    trimmed = url.strip()
    if not trimmed or len(trimmed) > MAX_URL_LENGTH:
        return False
    if re.search(r"javascript\s*:", trimmed, re.I):
        return False
    if re.search(r"data\s*:", trimmed, re.I):
        return False
    try:
        from urllib.parse import urlparse

        parsed = urlparse(trimmed)
        return parsed.scheme in ("http", "https") and bool(parsed.netloc)
    except Exception:
        return False


def sanitize_reference(ref: Dict[str, Any]) -> Dict[str, Any]:
    """단일 reference의 url 검증. 유효하지 않으면 url 제거"""
    if not isinstance(ref, dict):
        return {}
    result = dict(ref)
    url = ref.get("url")
    if not is_safe_external_url(url):
        result.pop("url", None)
    return result


def sanitize_node_references(node: Dict[str, Any]) -> Dict[str, Any]:
    """노드의 references 배열 내 url 검증"""
    if not isinstance(node, dict):
        return node
    refs = node.get("references")
    if not isinstance(refs, list):
        return node
    result = dict(node)
    sanitized = [sanitize_reference(r) for r in refs]
    result["references"] = [r for r in sanitized if r]
    return result


def sanitize_mapping_result(data: Dict[str, Any]) -> Dict[str, Any]:
    """매핑 결과 내 모든 노드의 references 검증"""
    if not isinstance(data, dict):
        return data
    result = dict(data)
    nodes = result.get("nodes")
    if isinstance(nodes, list):
        result["nodes"] = [sanitize_node_references(n) for n in nodes]
    return result
