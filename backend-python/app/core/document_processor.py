"""
문서 처리기 (고도화 버전)
- PDF 및 TXT 파일에서 텍스트를 추출하고, AI 분석에 최적화된 크기로 쪼갭니다(Chunking).
"""
import PyPDF2
import io
import pandas as pd
from fastapi import UploadFile
from typing import List, Dict, Any

class DocumentProcessor:
    """
    문서 전처리 엔진
    - PDF, TXT, Excel 등 다양한 형식에서 데이터를 추출합니다.
    """

    def extract_data(self, file_content: bytes, filename: str) -> Any:
        """파일 형식에 따라 데이터 추출 (텍스트 또는 구조화된 데이터)"""
        fn = filename.lower()
        
        # 1. Excel 처리 (정산 데이터 등)
        if fn.endswith(('.xlsx', '.xls')):
            df = pd.read_excel(io.BytesIO(file_content))
            return df.to_dict(orient='records') # 리스트 객체로 반환

        # 2. CSV 처리
        elif fn.endswith('.csv'):
            df = pd.read_csv(io.BytesIO(file_content))
            return df.to_dict(orient='records')

        # 3. PDF 처리
        elif fn.endswith('.pdf'):
            return self._extract_from_pdf(file_content)

        # 4. TXT 처리
        return file_content.decode('utf-8', errors='ignore')

    def _extract_from_pdf(self, content: bytes) -> str:
        """PDF 바이너리에서 텍스트 추출"""
        pdf_reader = PyPDF2.PdfReader(io.BytesIO(content))
        text = ""
        for page in pdf_reader.pages:
            text += page.extract_text() + "\n"
        return text

    def split_text(self, text: str, chunk_size: int = 3000, overlap: int = 200) -> List[str]:
        """
        [알고리즘: 슬라이딩 윈도우 청킹]
        - 긴 문서를 chunk_size 단위로 쪼개되, 맥락 유지를 위해 overlap을 둡니다.
        - 이유: AI 모델의 토큰 제한(Context Window) 문제를 해결하기 위함입니다.
        """
        chunks = []
        start = 0
        while start < len(text):
            end = start + chunk_size
            chunks.append(text[start:end])
            start += chunk_size - overlap # 맥락 연결을 위한 오버랩 적용
        return chunks

document_processor = DocumentProcessor()
