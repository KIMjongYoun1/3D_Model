"""create users table - REMOVED (Java Flyway 담당)

Revision ID: 001
Revises: 
Create Date: 2025-12-26

NOTE: users 테이블은 quantum_service DB에서 Java Flyway가 관리합니다.
      이 마이그레이션은 quantum_ai DB에서는 실행하지 않습니다.
      DB 분리(2026-02-09)로 인해 빈 마이그레이션으로 변경되었습니다.
"""
from alembic import op
import sqlalchemy as sa

# revision identifiers, used by Alembic.
revision = '001'
down_revision = None
branch_labels = None
depends_on = None


def upgrade() -> None:
    # users 테이블은 quantum_service DB (Java Flyway)에서 관리
    # quantum_ai DB에서는 생성하지 않음
    op.execute('CREATE EXTENSION IF NOT EXISTS "uuid-ossp"')


def downgrade() -> None:
    pass
