#!/bin/sh -ex

cd source

mvn -e install

cp target/*.jar ../app-jar