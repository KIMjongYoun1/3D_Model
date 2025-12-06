#!/usr/bin/env python3
"""
Notion에 프로젝트 문서를 업로드하는 스크립트

사용법:
    python upload_to_notion.py

필수:
    - .env 파일에 NOTION_API_KEY 설정
    - pip install notion-client
"""

import os
import json
from pathlib import Path
from notion_client import Client
from dotenv import load_dotenv

# 환경 변수 로드
load_dotenv()

# Notion 클라이언트 초기화
notion = Client(auth=os.getenv("NOTION_API_KEY"))

# 메인 페이지 ID
MAIN_PAGE_ID = "2c1ccb77-bf7d-8105-9c1d-ee187cad3c53"

# 업로드할 문서 목록 (포트폴리오용 - 제외 문서 제외)
# 스크립트는 docs/ 디렉토리에서 실행되므로 상대 경로 사용
DOCUMENTS = {
    "기획 문서": {
        "planning/README.md": "프로젝트 개요",
        "planning/ROADMAP.md": "개발 로드맵"
    },
    "기술 문서": {
        "technical/ARCHITECTURE.md": "시스템 아키텍처",
        "technical/AI_MODELS.md": "AI 모델 선정",
        "technical/AI_INTEGRATION.md": "AI 모듈 연동",
        "technical/SECURITY.md": "보안 가이드"
    },
    "개발 가이드": {
        "guides/MCP_GUIDE.md": "MCP 가이드",
        "guides/NOTION_GUIDE.md": "Notion 가이드",
        "guides/WORKFLOW_DESIGN_TO_DOCS.md": "워크플로우",
        "guides/CURSOR_GUIDE.md": "Cursor 가이드",
        "guides/CURSOR_TOOLS_INTEGRATION.md": "도구 통합"
    },
    "도구": {
        "tools/TOOLS.md": "사용 도구"
    },
    "설계 문서": {
        "design/ERD.md": "데이터베이스 설계",
        "design/FIGMA_GUIDE.md": "Figma 가이드",
        "design/COMPONENT_SPECS.md": "컴포넌트 스펙"
    }
}

# 제외할 문서
EXCLUDED_DOCUMENTS = [
    "PROJECT_REALITY_CHECK.md",
    "PROJECT_ASSESSMENT.md",
    "COST.md",
    "UserThink.md"
]


def markdown_to_notion_blocks(markdown_content: str) -> list:
    """
    마크다운을 Notion 블록 형식으로 변환
    간단한 변환 (제목, 문단, 코드 블록)
    """
    blocks = []
    lines = markdown_content.split('\n')
    
    i = 0
    while i < len(lines):
        line = lines[i].strip()
        
        if not line:
            i += 1
            continue
            
        # 제목 처리
        if line.startswith('# '):
            blocks.append({
                "object": "block",
                "type": "heading_1",
                "heading_1": {
                    "rich_text": [{"type": "text", "text": {"content": line[2:]}}]
                }
            })
        elif line.startswith('## '):
            blocks.append({
                "object": "block",
                "type": "heading_2",
                "heading_2": {
                    "rich_text": [{"type": "text", "text": {"content": line[3:]}}]
                }
            })
        elif line.startswith('### '):
            blocks.append({
                "object": "block",
                "type": "heading_3",
                "heading_3": {
                    "rich_text": [{"type": "text", "text": {"content": line[4:]}}]
                }
            })
        # 코드 블록 처리
        elif line.startswith('```'):
            code_language = line[3:].strip() if len(line) > 3 else ""
            code_lines = []
            i += 1
            while i < len(lines) and not lines[i].strip().startswith('```'):
                code_lines.append(lines[i])
                i += 1
            blocks.append({
                "object": "block",
                "type": "code",
                "code": {
                    "rich_text": [{"type": "text", "text": {"content": "\n".join(code_lines)}}],
                    "language": code_language if code_language else "plain text"
                }
            })
        # 일반 문단
        else:
            blocks.append({
                "object": "block",
                "type": "paragraph",
                "paragraph": {
                    "rich_text": [{"type": "text", "text": {"content": line}}]
                }
            })
        
        i += 1
    
    return blocks


def create_notion_page(parent_id: str, title: str, content: str) -> str:
    """
    Notion 페이지 생성
    """
    try:
        # 마크다운을 Notion 블록으로 변환
        blocks = markdown_to_notion_blocks(content)
        
        # 페이지 생성
        response = notion.pages.create(
            parent={"page_id": parent_id},
            properties={
                "title": {
                    "title": [
                        {
                            "text": {
                                "content": title
                            }
                        }
                    ]
                }
            },
            children=blocks[:100]  # Notion API는 한 번에 최대 100개 블록
        )
        
        page_id = response["id"]
        print(f"✅ 페이지 생성 성공: {title} (ID: {page_id})")
        
        # 나머지 블록이 있으면 추가
        if len(blocks) > 100:
            remaining_blocks = blocks[100:]
            for i in range(0, len(remaining_blocks), 100):
                chunk = remaining_blocks[i:i+100]
                notion.blocks.children.append(
                    block_id=page_id,
                    children=chunk
                )
                print(f"  추가 블록 업로드: {len(chunk)}개")
        
        return page_id
        
    except Exception as e:
        print(f"❌ 페이지 생성 실패: {title}")
        print(f"   에러: {str(e)}")
        return None


def create_category_page(parent_id: str, category_name: str) -> str:
    """
    카테고리 페이지 생성
    """
    try:
        response = notion.pages.create(
            parent={"page_id": parent_id},
            properties={
                "title": {
                    "title": [
                        {
                            "text": {
                                "content": category_name
                            }
                        }
                    ]
                }
            }
        )
        print(f"✅ 카테고리 생성: {category_name}")
        return response["id"]
    except Exception as e:
        print(f"❌ 카테고리 생성 실패: {category_name}")
        print(f"   에러: {str(e)}")
        return None


def main():
    """
    메인 실행 함수
    """
    print("🚀 Notion 문서 업로드 시작...")
    print(f"📄 메인 페이지 ID: {MAIN_PAGE_ID}\n")
    
    # 각 카테고리별로 처리
    for category, documents in DOCUMENTS.items():
        print(f"\n📁 카테고리: {category}")
        
        # 카테고리 페이지 생성
        category_page_id = create_category_page(MAIN_PAGE_ID, category)
        if not category_page_id:
            print(f"   ⚠️ 카테고리 생성 실패, 건너뜀")
            continue
        
        # 각 문서 업로드
        for filename, title in documents.items():
            filepath = Path(filename)
            
            if not filepath.exists():
                print(f"   ⚠️ 파일 없음: {filename}")
                continue
            
            if filename in EXCLUDED_DOCUMENTS:
                print(f"   ⏭️ 제외 문서: {filename}")
                continue
            
            # 파일 읽기
            try:
                with open(filepath, 'r', encoding='utf-8') as f:
                    content = f.read()
            except Exception as e:
                print(f"   ❌ 파일 읽기 실패: {filename} - {str(e)}")
                continue
            
            # Notion 페이지 생성
            create_notion_page(category_page_id, title, content)
    
    print("\n✅ 업로드 완료!")


if __name__ == "__main__":
    main()

