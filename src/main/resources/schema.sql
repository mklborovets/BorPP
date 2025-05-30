-- Таблиця користувачів
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL DEFAULT 'user',
    balance DECIMAL(10, 2) NOT NULL DEFAULT 0.0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Таблиця транспортних засобів
CREATE TABLE IF NOT EXISTS vehicles (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    year INTEGER NOT NULL,
    type VARCHAR(50) NOT NULL,
    condition VARCHAR(50) NOT NULL,
    description TEXT,
    vin VARCHAR(17),
    mileage INTEGER,
    engine_type VARCHAR(50),
    photo_url TEXT,
    video_url TEXT,
    engine VARCHAR(100),
    engine_volume DECIMAL(3,1),
    power INTEGER,
    transmission VARCHAR(50),
    documents TEXT,
    registration_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Таблиця аукціонів
CREATE TABLE IF NOT EXISTS auctions (
    id SERIAL PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    start_price DECIMAL(10, 2) NOT NULL,
    price_step DECIMAL(10, 2) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CHECK (end_time > start_time),
    CHECK (price_step > 0),
    CHECK (start_price > 0),
    CHECK (status IN ('PENDING', 'ACTIVE', 'FINISHED', 'CANCELLED'))
);

-- Таблиця ставок
CREATE TABLE IF NOT EXISTS bids (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    auction_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    bid_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE,
    CHECK (amount > 0)
);

-- Таблиця логів
CREATE TABLE IF NOT EXISTS logs (
    id SERIAL PRIMARY KEY,
    user_id BIGINT,
    action TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
); 