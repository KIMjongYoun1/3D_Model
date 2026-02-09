"""add ai models

Revision ID: 002
Revises: 001
Create Date: 2026-01-24

NOTE: DB 분리(2026-02-09)로 인해 users.id FK 제거.
      user_id는 plain UUID 컬럼으로 유지 (application 레벨 참조).
"""
from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects import postgresql

# revision identifiers, used by Alembic.
revision = '002'
down_revision = '001'
branch_labels = None
depends_on = None

def upgrade() -> None:
    # 1. 아바타 테이블 생성
    op.create_table(
        'avatars',
        sa.Column('id', postgresql.UUID(as_uuid=True), nullable=False),
        sa.Column('user_id', postgresql.UUID(as_uuid=True), nullable=False),
        sa.Column('face_image_url', sa.String(length=500), nullable=False),
        sa.Column('mesh_data_url', sa.String(length=500), nullable=True),
        sa.Column('height', sa.Float(), nullable=True),
        sa.Column('weight', sa.Float(), nullable=True),
        sa.Column('body_type', sa.String(length=50), nullable=True),
        sa.Column('is_default', sa.Boolean(), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=True),
        sa.Column('updated_at', sa.DateTime(timezone=True), nullable=True),
        # FK 제거: user_id는 quantum_service DB의 users 테이블 참조 (application 레벨)
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index(op.f('ix_avatars_user_id'), 'avatars', ['user_id'], unique=False)

    # 2. 의류 테이블 생성
    op.create_table(
        'garments',
        sa.Column('id', postgresql.UUID(as_uuid=True), nullable=False),
        sa.Column('user_id', postgresql.UUID(as_uuid=True), nullable=False),
        sa.Column('name', sa.String(length=100), nullable=True),
        sa.Column('category', sa.String(length=50), nullable=True),
        sa.Column('image_url', sa.String(length=500), nullable=False),
        sa.Column('processed_image_url', sa.String(length=500), nullable=True),
        sa.Column('features', sa.JSON(), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=True),
        sa.Column('updated_at', sa.DateTime(timezone=True), nullable=True),
        # FK 제거: user_id는 quantum_service DB의 users 테이블 참조 (application 레벨)
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index(op.f('ix_garments_category'), 'garments', ['category'], unique=False)
    op.create_index(op.f('ix_garments_user_id'), 'garments', ['user_id'], unique=False)

    # 3. 가상 피팅 결과 테이블 생성
    op.create_table(
        'tryon_results',
        sa.Column('id', postgresql.UUID(as_uuid=True), nullable=False),
        sa.Column('user_id', postgresql.UUID(as_uuid=True), nullable=False),
        sa.Column('avatar_id', postgresql.UUID(as_uuid=True), nullable=False),
        sa.Column('garment_id', postgresql.UUID(as_uuid=True), nullable=False),
        sa.Column('result_image_url', sa.String(length=500), nullable=False),
        sa.Column('ai_metadata', sa.JSON(), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=True),
        # FK: avatar_id, garment_id는 같은 quantum_ai DB 내 테이블 참조
        sa.ForeignKeyConstraint(['avatar_id'], ['avatars.id'], ),
        sa.ForeignKeyConstraint(['garment_id'], ['garments.id'], ),
        # FK 제거: user_id는 quantum_service DB의 users 테이블 참조 (application 레벨)
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index(op.f('ix_tryon_results_user_id'), 'tryon_results', ['user_id'], unique=False)

def downgrade() -> None:
    op.drop_table('tryon_results')
    op.drop_table('garments')
    op.drop_table('avatars')
