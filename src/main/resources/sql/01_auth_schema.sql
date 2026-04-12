-- USERS
CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       username VARCHAR(100) UNIQUE NOT NULL,
                       email VARCHAR(150) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ROLES
CREATE TABLE roles (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(50) UNIQUE NOT NULL
);

-- USER_ROLES (many-to-many)
CREATE TABLE user_roles (
                            user_id INT,
                            role_id INT,
                            PRIMARY KEY (user_id, role_id),
                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                            FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- REFRESH TOKENS
CREATE TABLE refresh_tokens (
                                id SERIAL PRIMARY KEY,
                                user_id INT,
                                token VARCHAR(255) NOT NULL,
                                expiry_date TIMESTAMP,
                                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);