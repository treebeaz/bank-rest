--liquibase formatted sql

--changeset treebeaz:1
ALTER TABLE cards
ADD COLUMN cardholder_name VARCHAR(255);


--changeset treebeaz:2
UPDATE cards
SET cardholder_name = firstname || ' ' || lastname;

--changeset treebeaz:3
ALTER TABLE cards
DROP COLUMN firstname,
DROP COLUMN lastname;

--changeset treebeaz:4
ALTER TABLE cards
ALTER COLUMN cardholder_name SET NOT NULL;