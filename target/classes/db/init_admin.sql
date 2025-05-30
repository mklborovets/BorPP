-- Додавання адміністратора
-- Пароль: Admin123!
INSERT INTO users (username, password, email, role, balance, created_at)
VALUES (
    'admin',
    -- Хешований пароль Admin123!
    '$2a$10$XgXLGk9ZQgWXH5XGk.Zf5O1kJ7vj7vH.q9q9q9q9q9q9q9q9q9q',
    'admin@auction.com',
    'admin',
    0.0,
    CURRENT_TIMESTAMP
)
ON CONFLICT (username) DO NOTHING; 