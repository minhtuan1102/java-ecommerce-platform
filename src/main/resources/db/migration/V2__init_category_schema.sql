CREATE TABLE categories (
                             id SERIAL PRIMARY KEY,
                             name VARCHAR(100) UNIQUE NOT NULL,
                             description VARCHAR(500),
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
