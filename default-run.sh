#!/bin/sh

if [ ! -f .env ]; then
    echo "The .env file was not found. Please rename .env.template to .env and fill in the values."
    exit 1
fi

# Execute Docker Compose command
docker compose -f docker-compose.yml up -d --build