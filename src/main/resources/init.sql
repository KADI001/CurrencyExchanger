CREATE TABLE currencies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code CHAR(3) UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    sign CHAR(5) NOT NULL
);
--QUERY
CREATE TABLE exchangerates (
    id INT AUTO_INCREMENT PRIMARY KEY,
    base_currency_id INT NOT NULL,
    target_currency_id INT NOT NULL,
    rate DECIMAL(65,2) NOT NULL,

    FOREIGN KEY (base_currency_id) REFERENCES currencies(id),
    FOREIGN KEY (target_currency_id) REFERENCES currencies(id)
);