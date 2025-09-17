CREATE TABLE IF NOT EXISTS Collection (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    salary BIGINT NOT NULL,
    start_date VARCHAR(255) NOT NULL,
    coordinate_x INTEGER NOT NULL,
    coordinate_y FLOAT NOT NULL,
    status VARCHAR(50) NULL,
    position VARCHAR(50) NULL,
    organization_annual_turnover VARCHAR(255) NULL,
    organization_organization_type VARCHAR(50) NULL,
    address_street VARCHAR(255) NULL,
    location_x FLOAT NULL,
    location_y DOUBLE PRECISION NULL,
    location_z BIGINT NULL,
    location_name VARCHAR(255) NULL,
    creation_date DATE NOT NULL,
    who_created VARCHAR(255) NOT NULL
);
CREATE TABLE IF NOT EXISTS Users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);
