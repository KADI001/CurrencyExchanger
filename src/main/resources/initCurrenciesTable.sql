CREATE TABLE currencies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code CHAR(3) UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    sign CHAR(5) NOT NULL
);