#!/bin/bash

mkdir /home/ubuntu/deployment/nginx_certs
/home/ubuntu/deployment/scripts/convert_ssl.sh /home/ubuntu/secrets/origin_a_com.p12 /home/ubuntu/deployment/nginx_certs/origin_a_com.crt.pem /home/ubuntu/deployment/nginx_certs/origin_a_com.key.pem
/home/ubuntu/deployment/scripts/convert_ssl.sh /home/ubuntu/secrets/origin_b_com.p12 /home/ubuntu/deployment/nginx_certs/origin_b_com.crt.pem /home/ubuntu/deployment/nginx_certs/origin_b_com.key.pem