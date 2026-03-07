CREATE TABLE managers (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(30) NOT NULL,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    address VARCHAR(255),
    birth_date DATE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);