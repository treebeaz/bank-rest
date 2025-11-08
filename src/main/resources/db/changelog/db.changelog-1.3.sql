--liquibase formatted sql

--changeset treebeaz:1
ALTER TABLE cards ADD COLUMN hash_card_number VARCHAR(64) NOT NULL UNIQUE;