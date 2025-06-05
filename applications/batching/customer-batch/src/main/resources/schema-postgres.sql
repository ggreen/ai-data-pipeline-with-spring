create schema if not exists customer;


CREATE TABLE if not exists customer.customers (
    customer_id SERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    address VARCHAR(20),
    city VARCHAR(20),
    state VARCHAR(20),
    zip VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);