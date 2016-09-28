#!/bin/sh -ex

cd tile-repo

mvn -e install

cp target/*.jar ../broker-jar