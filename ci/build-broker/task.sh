#!/bin/bash -eux

cd tile-repo

mvn -e install

file=`ls target/*.jar`
filename=$(basename "${file}")
filename=${filename%-*}
ver=`more ../version/number`

cp ${file} ../broker-jar/${filename}-${ver}.jar