--liquibase formatted sql

--changeset treebeaz:1
ALTER TABLE cards ADD COLUMN firstname VARCHAR(128) NOT NULL;
ALTER TABLE cards ADD COLUMN lastname VARCHAR(128) NOT NULL;