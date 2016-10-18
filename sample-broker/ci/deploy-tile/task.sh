#!/bin/sh -ex

REPO_DIR="$( cd tile-repo  && pwd )"
POOL_DIR="$( cd pcf-environment && pwd )"
TILE_FILE=`cd broker-tile; ls *.pivotal`
if [ -z "${TILE_FILE}" ]; then
	echo "No files matching broker-tile/*.pivotal"
	ls -lR broker-tile
	exit 1
fi

PRODUCT=`echo "${TILE_FILE}" | sed "s/-[^-]*$//"`
VERSION=`more version/number`

cd pcf-environment

echo "Available products:"
PCF products
echo

echo "Uploading ${TILE_FILE}"
PCF import broker-tile/${TILE_FILE}
echo

echo "Available products:"
PCF products
PCF is-available "${PRODUCT}" "${VERSION}"
echo

echo "Installing product ${PRODUCT} version ${VERSION}"
PCF install "${PRODUCT}" "${VERSION}"
echo

echo "Available products:"
PCF products
PCF is-installed "${PRODUCT}" "${VERSION}"
echo

echo "Configuring product ${PRODUCT}"
PCF configure "${PRODUCT}" "../tile-repo/sample-broker/ci/missing-properties.yml"
echo

echo "Applying Changes"
PCF apply-changes
echo