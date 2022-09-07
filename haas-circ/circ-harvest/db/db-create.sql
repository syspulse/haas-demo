CREATE DATABASE IF NOT EXISTS haas_db;
CREATE USER IF NOT EXISTS 'haas_user'@'%' IDENTIFIED BY 'haas_pass';
GRANT ALL PRIVILEGES ON haas_db.* TO 'haas_user'@'%' WITH GRANT OPTION;

USE haas_db;

CREATE TABLE HOLDERS (
    addr varchar(255),
    value decimal(15,5)
);

