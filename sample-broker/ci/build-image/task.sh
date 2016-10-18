#!/bin/sh -ex

cd tile-repo/sample-broker

mkdir target
cp ../../broker-jar/*.jar target/hello-broker.jar
tile build

file=`ls product/*.pivotal`
filename=$(basename "${file}")
filename="${filename%-*}"
ver=`more ../number`

cp ${file} ../../broker-tile/${filename}-${ver}.pivotal