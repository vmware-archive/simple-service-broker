#!/bin/sh -ex

cd tile-repo/sample-broker

mkdir target
cp ../../broker-jar/*.jar target/
tile build

cp product/*.pivotal ../../broker-tile