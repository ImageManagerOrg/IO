#!/bin/sh

mv prod-docker-compose.yml docker-compose.yml

sudo docker-compose stop
sudo docker-compose down
sudo docker-compose pull
sudo docker-compose up -d