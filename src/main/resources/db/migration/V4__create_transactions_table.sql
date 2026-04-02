-- ═══════════════════════════════════════════════════════════
-- V4: Create transactions table
-- ═══════════════════════════════════════════════════════════

CREATE TABLE transactions (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID NOT NULL REFERENCES users(id),
    from_account_id   UUID REFERENCES accounts(id),
    to_account_id     UUID REFERENCES accounts(id),
    amount            NUMERIC(18, 2) NOT NULL,
    type              VARCHAR(20) NOT NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'pending',
    encrypted_details TEXT,
    reference_number  VARCHAR(50) UNIQUE NOT NULL,
    approved_by       UUID REFERENCES users(id),
    approved_at       TIMESTAMPTZ,
    rejection_reason  TEXT,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_transaction_type CHECK (type IN ('transfer', 'deposit', 'withdrawal')),
    CONSTRAINT chk_transaction_status CHECK (status IN ('pending', 'approved', 'rejected', 'completed')),
    CONSTRAINT chk_positive_amount CHECK (amount > 0)
);

CREATE INDEX idx_txn_user ON transactions(user_id);
CREATE INDEX idx_txn_status ON transactions(status);
CREATE INDEX idx_txn_created ON transactions(created_at DESC);
CREATE INDEX idx_txn_reference ON transactions(reference_number);
CREATE INDEX idx_txn_from_account ON transactions(from_account_id);
CREATE INDEX idx_txn_to_account ON transactions(to_account_id);
