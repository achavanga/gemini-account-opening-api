-- Create Address table
CREATE TABLE address (
                         id SERIAL PRIMARY KEY,
                         house_number VARCHAR(6) NOT NULL,
                         street_name VARCHAR(100) NOT NULL,
                         city VARCHAR(100) NOT NULL,
                         postal_code VARCHAR(10) NOT NULL CHECK (postal_code ~ '^\d{4} [A-Z]{2}$')
);

-- Create AccountType enum (if your database supports enums)
CREATE TYPE account_type AS ENUM ('SAVINGS', 'CHECKING', 'BUSINESS');

-- Create RequestStatus enum (if your database supports enums)
CREATE TYPE request_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED');

-- Create Customer table
CREATE TABLE customer (
                          id SERIAL PRIMARY KEY,
                          name VARCHAR(100) NOT NULL CHECK (LENGTH(name) >= 2),
                          address_id INTEGER REFERENCES address(id) ON DELETE SET NULL,
                          date_of_birth DATE NOT NULL CHECK (date_of_birth < CURRENT_DATE),
                          id_document VARCHAR(20) ,
                          account_type account_type,
                          starting_balance NUMERIC(15,2) CHECK (starting_balance > 0),
                          monthly_salary NUMERIC(15,2) CHECK (monthly_salary > 0),
                          interested_in_other_products BOOLEAN,
                          email VARCHAR(100) CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    status request_status ,
    paused_at TIMESTAMP WITH TIME ZONE,
    request_id  VARCHAR(100) NOT NULL,
    CONSTRAINT fk_address FOREIGN KEY (address_id) REFERENCES address(id)
);

CREATE INDEX idx_address_postal_code ON address(postal_code);
CREATE INDEX idx_address_street_name ON address(street_name);

CREATE INDEX idx_customer_request_id ON customer(request_id);
CREATE UNIQUE INDEX idx_customer_email ON customer(email);
CREATE INDEX idx_customer_date_of_birth ON customer(date_of_birth);