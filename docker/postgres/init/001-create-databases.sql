SELECT 'CREATE DATABASE auth_db'
WHERE NOT EXISTS (
    SELECT FROM pg_database WHERE datname = 'auth_db'
)\gexec

SELECT 'CREATE DATABASE user_db'
WHERE NOT EXISTS (
    SELECT FROM pg_database WHERE datname = 'user_db'
)\gexec

SELECT 'CREATE DATABASE product_db'
WHERE NOT EXISTS (
    SELECT FROM pg_database WHERE datname = 'product_db'
)\gexec

SELECT 'CREATE DATABASE inventory_db'
WHERE NOT EXISTS (
    SELECT FROM pg_database WHERE datname = 'inventory_db'
)\gexec

SELECT 'CREATE DATABASE order_db'
WHERE NOT EXISTS (
    SELECT FROM pg_database WHERE datname = 'order_db'
)\gexec

SELECT 'CREATE DATABASE notification_db'
WHERE NOT EXISTS (
    SELECT FROM pg_database WHERE datname = 'notification_db'
)\gexec