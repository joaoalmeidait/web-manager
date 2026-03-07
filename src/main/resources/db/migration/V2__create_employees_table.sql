CREATE TABLE employees (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(255),
    created_at TIMESTAMP,
    manager_id UUID,
    CONSTRAINT fk_manager
        FOREIGN KEY (manager_id)
        REFERENCES managers(id)
);