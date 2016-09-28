#!/bin/sh -e

TILE_GEN_DIR=$1
SOURCE_DIR=$2
BROKER_JAR=$3
TILE_HISTORY_OLD=$4

BIN_DIR="$( cd "${TILE_GEN_DIR}/bin" && pwd )"

TILE="${BIN_DIR}/tile"

HISTORY=`ls ${TILE_HISTORY_OLD}/tile-history-*.yml`
if [ -n "${HISTORY}" ]; then
	cp ${HISTORY} ${SOURCE_DIR}/tile-history.yml
fi

(cd ${SOURCE_DIR}; cp ${BROKER_JAR} resources/; ${TILE} build)

VERSION=`grep '^version:' ${SOURCE_DIR}/tile-history.yml | sed 's/^version: //'`
HISTORY="tile-history-${VERSION}.yml"

cp ${SOURCE_DIR}/product/*.pivotal ../broker-jar
cp ${SOURCE_DIR}/tile-history.yml ../broker-jar/tile-history-${VERSION}.yml