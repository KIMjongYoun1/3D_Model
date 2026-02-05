"""add correlation rules table

Revision ID: 004
Revises: 003
Create Date: 2026-02-05

"""
from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects import postgresql

# revision identifiers, used by Alembic.
revision = '004'
down_revision = '003'
branch_labels = None
depends_on = None

def upgrade() -> None:
    # 1. 테이블 생성
    op.create_table(
        'correlation_rules',
        sa.Column('id', sa.Integer(), autoincrement=True, nullable=False),
        sa.Column('category', sa.String(length=50), nullable=False),
        sa.Column('keywords', sa.JSON(), nullable=False),
        sa.Column('strength', sa.Integer(), server_default='5', nullable=True),
        sa.Column('label', sa.String(length=100), nullable=True),
        sa.Column('is_active', sa.Boolean(), server_default='true', nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=True),
        sa.Column('updated_at', sa.DateTime(timezone=True), nullable=True),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index(op.f('ix_correlation_rules_category'), 'correlation_rules', ['category'], unique=False)

    # 2. 기본 데이터 인서트 (1회성)
    # PostgreSQL의 JSON 형식을 맞추기 위해 문자열로 입력
    op.execute("""
        INSERT INTO correlation_rules (category, keywords, strength, label) VALUES 
        ('FINANCE', '["payment", "billing", "price", "amount", "settlement", "tax", "money", "cost", "결제", "정산", "금액", "매출"]', 8, 'Financial Connection'),
        ('IDENTITY', '["user", "account", "login", "member", "id", "auth", "profile", "유저", "계정", "로그인", "회원"]', 7, 'User Identity Flow'),
        ('INFRA', '["server", "db", "database", "network", "cloud", "aws", "redis", "cache", "storage", "서버", "데이터베이스", "인프라"]', 6, 'Infrastructure Link'),
        ('DEVOPS', '["ci", "cd", "pipeline", "git", "repo", "docker", "k8s", "jenkins", "배포", "파이프라인"]', 5, 'DevOps Workflow')
    """)

def downgrade() -> None:
    op.drop_index(op.f('ix_correlation_rules_category'), table_name='correlation_rules')
    op.drop_table('correlation_rules')
