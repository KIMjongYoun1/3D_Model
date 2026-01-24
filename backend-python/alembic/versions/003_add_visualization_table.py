"""add visualization table

Revision ID: 003
Revises: 002
Create Date: 2026-01-24

"""
from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects import postgresql

# revision identifiers, used by Alembic.
revision = '003'
down_revision = '002'
branch_labels = None
depends_on = None

def upgrade() -> None:
    op.create_table(
        'visualization_data',
        sa.Column('id', postgresql.UUID(as_uuid=True), nullable=False),
        sa.Column('user_id', postgresql.UUID(as_uuid=True), nullable=False),
        sa.Column('data_type', sa.String(length=50), nullable=False),
        sa.Column('raw_data', sa.JSON(), nullable=True),
        sa.Column('mapping_data', sa.JSON(), nullable=True),
        sa.Column('model_url', sa.String(length=500), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=True),
        sa.Column('updated_at', sa.DateTime(timezone=True), nullable=True),
        sa.ForeignKeyConstraint(['user_id'], ['users.id'], ),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index(op.f('ix_visualization_data_data_type'), 'visualization_data', ['data_type'], unique=False)
    op.create_index(op.f('ix_visualization_data_user_id'), 'visualization_data', ['user_id'], unique=False)

def downgrade() -> None:
    op.drop_table('visualization_data')

