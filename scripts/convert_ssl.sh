#!/bin/bash

openssl pkcs12 -in $1 -out $2 -clcerts -nokeys -password pass:password
openssl pkcs12 -in $1 -out $3 -nocerts -nodes -password pass:password