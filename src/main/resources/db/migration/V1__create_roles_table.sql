-- ═══════════════════════════════════════════════════════════
-- V1: Create roles table
-- ═══════════════════════════════════════════════════════════

CREATE TABLE roles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(50) UNIQUE NOT NULL,
    permissions JSONB NOT NULL DEFAULT '[]'::jsonb,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Seed default roles with permissions
INSERT INTO roles (id, name, permissions, description) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'admin',
     '["read", "write", "delete", "approve", "manage_users", "manage_roles", "view_audit"]'::jsonb,
     'Full system access — can manage users, roles, and view audit logs'),

    ('a0000000-0000-0000-0000-000000000002', 'finance_manager',
     '["read", "write", "approve"]'::jsonb,
     'Can read, write, and approve financial transactions'),

    ('a0000000-0000-0000-0000-000000000003', 'analyst',
     '["read"]'::jsonb,
     'Read-only access to transactions and analytics'),

    ('a0000000-0000-0000-0000-000000000004', 'user',
     '["read", "write"]'::jsonb,
     'Basic user — can read and create own transactions');
