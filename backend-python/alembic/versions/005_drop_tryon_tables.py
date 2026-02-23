"""drop tryon tables (avatars, garments, tryon_results)

Revision ID: 005
Revises: 004
Create Date: 2026-02-19

데이터 시각화에 집중하기 위해 피팅(Try-On) 관련 테이블 제거.
"""
from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects import postgresql

revision = '005'
down_revision = '004'
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.execute("DROP TABLE IF EXISTS tryon_results")
    op.execute("DROP TABLE IF EXISTS garments")
    op.execute("DROP TABLE IF EXISTS avatars")


def downgrade() -> None:
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
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index(op.f('ix_avatars_user_id'), 'avatars', ['user_id'], unique=False)

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
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index(op.f('ix_garments_category'), 'garments', ['category'], unique=False)
    op.create_index(op.f('ix_garments_user_id'), 'garments', ['user_id'], unique=False)

    op.create_table(
        'tryon_results',
        sa.Column('id', postgresql.UUID(as_uuid=True), nullable=False),
        sa.Column('user_id', postgresql.UUID(as_uuid=True), nullable=False),
        sa.Column('avatar_id', postgresql.UUID(as_uuid=True), nullable=False),
        sa.Column('garment_id', postgresql.UUID(as_uuid=True), nullable=False),
        sa.Column('result_image_url', sa.String(length=500), nullable=False),
        sa.Column('ai_metadata', sa.JSON(), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=True),
        sa.ForeignKeyConstraint(['avatar_id'], ['avatars.id'], ),
        sa.ForeignKeyConstraint(['garment_id'], ['garments.id'], ),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index(op.f('ix_tryon_results_user_id'), 'tryon_results', ['user_id'], unique=False)
