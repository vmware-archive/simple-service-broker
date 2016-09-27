#!/bin/sh -ex

cd hello-tile-repo

mvn -e install

cp target/*.jar ../app-jar