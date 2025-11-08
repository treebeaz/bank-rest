--liquibase formatted sql

--changeset treebeaz:1
CREATE TABLE cards (
    id BIGSERIAL PRIMARY KEY,
    card_number VARCHAR(255) NOT NULL,  -- зашифрованный номер карты
    last_digits VARCHAR(4) NOT NULL,
    user_id BIGINT NOT NULL,
    balance DECIMAL(15,2) DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK ( status in ('ACTIVE','BLOCKED', 'EXPIRED') ),
    expiry_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_cards_user_id FOREIGN  KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);