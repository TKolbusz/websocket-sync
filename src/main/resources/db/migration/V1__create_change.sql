CREATE TABLE IF NOT EXISTS change
(
    id         SERIAL PRIMARY KEY,
    entityId   VARCHAR(36) NOT NULL,
    entityType VARCHAR(40) NOT NULL,
    timestamp  BIGINT      NOT NULL,
    tenantId   CHAR(10)
);
