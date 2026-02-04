"""create users table

Revision ID: 001
Revises: 
Create Date: 2025-12-26

"""
from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects.postgresql import UUID

# revision identifiers, used by Alembic.
revision = '001'
down_revision = None
branch_labels = None
depends_on = None


def upgrade() -> None:
    """
    users 테이블 생성
    - 사용자 정보 저장
    - 비밀번호는 BCrypt로 해시화되어 저장
    """
    # UUID 확장 활성화
    op.execute('CREATE EXTENSION IF NOT EXISTS "uuid-ossp"')
    
    # users 테이블 생성
    op.create_table(
        'users',
        sa.Column('id', UUID(as_uuid=True), primary_key=True, server_default=sa.text('uuid_generate_v4()')),
        sa.Column('email', sa.String(255), nullable=False, unique=True),
        sa.Column('password_hash', sa.String(255), nullable=False),  # BCrypt 해시
        sa.Column('name', sa.String(100), nullable=True),
        sa.Column('profile_image', sa.String(500), nullable=True),
        sa.Column('subscription', sa.String(20), server_default='free', nullable=False),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
        sa.Column('updated_at', sa.DateTime(timezone=True), nullable=True),
        sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
    )
    
    # 인덱스 생성
    op.create_index('idx_users_email', 'users', ['email'])
    op.create_index('idx_users_subscription', 'users', ['subscription'])
    op.create_index('idx_users_deleted_at', 'users', ['deleted_at'], postgresql_where=sa.text('deleted_at IS NULL'))


def downgrade() -> None:
    """
    users 테이블 삭제 (롤백)
    """
    op.drop_index('idx_users_deleted_at', table_name='users')
    op.drop_index('idx_users_subscription', table_name='users')
    op.drop_index('idx_users_email', table_name='users')
    op.drop_table('users')





