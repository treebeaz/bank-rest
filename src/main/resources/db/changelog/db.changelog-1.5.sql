--liquibase formatted sql

--changeset treebeaz:1
ALTER TABLE cards
DROP CONSTRAINT IF EXISTS cards_status_check;

--changeset treebeaz:2
ALTER TABLE cards
ADD CONSTRAINT cards_status_check
CHECK(status IN ('ACTIVE', 'BLOCKED', 'EXPIRED', 'PENDING_BLOCK'));