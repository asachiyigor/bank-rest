#!/bin/bash
set -e

# Modify pg_hba.conf to use md5 authentication
echo "host all all all md5" >> /var/lib/postgresql/data/pg_hba.conf

# Set password for postgres user
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    ALTER USER postgres WITH PASSWORD 'postgres';
EOSQL

echo "PostgreSQL authentication configured successfully"
