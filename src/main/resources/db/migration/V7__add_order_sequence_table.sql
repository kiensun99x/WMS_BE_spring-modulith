-- File: V7__add_order_sequence_table.sql

USE order_db;

CREATE TABLE order_sequences (
    sequence_date DATE primary key ,
    current_value BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME
);