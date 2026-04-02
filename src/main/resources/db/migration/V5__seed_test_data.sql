-- ═══════════════════════════════════════════════════════════
-- V5: Seed test users, accounts, and transactions
-- Passwords are bcrypt-hashed: "Admin@123456" for all test users
-- bcrypt hash (12 rounds) of "Admin@123456"
-- ═══════════════════════════════════════════════════════════

-- Test users (password: Admin@123456)
-- Hash generated with BCrypt 12 rounds
INSERT INTO users (id, email, password_hash, first_name, last_name, role_id) VALUES
    ('b0000000-0000-0000-0000-000000000001', 'admin@zorvyn.com',
     '$2a$12$LJ3m4ys3LzTLnEFBhm6GOeTwaONoJTLJ5RRCqFhwAZNzHEh.jg1Mq',
     'System', 'Admin',
     'a0000000-0000-0000-0000-000000000001'),

    ('b0000000-0000-0000-0000-000000000002', 'manager@zorvyn.com',
     '$2a$12$LJ3m4ys3LzTLnEFBhm6GOeTwaONoJTLJ5RRCqFhwAZNzHEh.jg1Mq',
     'Finance', 'Manager',
     'a0000000-0000-0000-0000-000000000002'),

    ('b0000000-0000-0000-0000-000000000003', 'analyst@zorvyn.com',
     '$2a$12$LJ3m4ys3LzTLnEFBhm6GOeTwaONoJTLJ5RRCqFhwAZNzHEh.jg1Mq',
     'Data', 'Analyst',
     'a0000000-0000-0000-0000-000000000003'),

    ('b0000000-0000-0000-0000-000000000004', 'user@zorvyn.com',
     '$2a$12$LJ3m4ys3LzTLnEFBhm6GOeTwaONoJTLJ5RRCqFhwAZNzHEh.jg1Mq',
     'Regular', 'User',
     'a0000000-0000-0000-0000-000000000004');

-- Test accounts (account_number values are placeholders — will be encrypted by the app)
INSERT INTO accounts (id, user_id, account_number, balance, currency, account_type) VALUES
    ('c0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001',
     'ACCT-1001-0001', 100000.00, 'USD', 'checking'),

    ('c0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000002',
     'ACCT-2001-0001', 75000.00, 'USD', 'checking'),

    ('c0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000002',
     'ACCT-2001-0002', 25000.00, 'USD', 'savings'),

    ('c0000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000003',
     'ACCT-3001-0001', 50000.00, 'USD', 'checking'),

    ('c0000000-0000-0000-0000-000000000005', 'b0000000-0000-0000-0000-000000000004',
     'ACCT-4001-0001', 10000.00, 'USD', 'checking'),

    ('c0000000-0000-0000-0000-000000000006', 'b0000000-0000-0000-0000-000000000004',
     'ACCT-4001-0002', 5000.00, 'USD', 'savings');

-- Sample transactions
INSERT INTO transactions (id, user_id, from_account_id, to_account_id, amount, type, status, reference_number) VALUES
    ('d0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000004',
     'c0000000-0000-0000-0000-000000000005', 'c0000000-0000-0000-0000-000000000006',
     500.00, 'transfer', 'completed', 'TXN-20240101-000001'),

    ('d0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000004',
     NULL, 'c0000000-0000-0000-0000-000000000005',
     1000.00, 'deposit', 'completed', 'TXN-20240101-000002'),

    ('d0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000002',
     'c0000000-0000-0000-0000-000000000002', NULL,
     250.00, 'withdrawal', 'pending', 'TXN-20240102-000001');
