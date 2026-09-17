#!/bin/sh
rm -f rsc/openapi.yaml
wget http://localhost:8080/q/openapi.yaml
mv openapi.yaml rsc/openapi.yaml