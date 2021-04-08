#!/bin/sh

mkdir -p ~/.prometheus-cfg/

sed \
    -e "s/GRAFANA_USERNAME/$GRAFANA_USERNAME/;s/GRAFANA_PASSWORD/$GRAFANA_PASSWORD/" \
    ../prometheus/prod-prometheus.yml > prometheus.yml
