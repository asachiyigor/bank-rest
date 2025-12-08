#!/bin/bash
set -e

# Modify pg_hba.conf to use trust authentication for all connections
echo "Configuring PostgreSQL authentication..."
echo "host all all all trust" >> "$PGDATA/pg_hba.conf"

echo "PostgreSQL initialization complete"
