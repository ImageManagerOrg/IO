#!/bin/sh

docker-compose down
docker-compose pull
docker-compose -f prod-docker-compose.yml up -d