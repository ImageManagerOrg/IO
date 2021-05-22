#!/bin/sh

mv prod-docker-compose.yml docker-compose.yml

/home/ubuntu/deployment/scripts/migrate_all_sll_cert.sh
sudo docker-compose stop
sudo docker-compose down
sudo docker-compose pull
sudo docker-compose up -d