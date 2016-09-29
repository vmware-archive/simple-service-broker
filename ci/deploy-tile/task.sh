#!/bin/sh -e

TILE_GEN_DIR="$( cd "$1" && pwd )"
REPO_DIR="$( cd "$2" && pwd )"
TILE_DIR="$( cd "$3" && pwd )"
POOL_DIR="$( cd "$4" && pwd )"

BIN_DIR="$( cd "${TILE_GEN_DIR}/bin" && pwd )"

PCF="${BIN_DIR}/pcf"

TILE_FILE=`cd "${TILE_DIR}"; ls *.pivotal`
if [ -z "${TILE_FILE}" ]; then
	echo "No files matching ${TILE_DIR}/*.pivotal"
	ls -lR "${TILE_DIR}"
	exit 1
fi

PRODUCT=`echo "${TILE_FILE}" | sed "s/-[^-]*$//"`
VERSION=`echo "${TILE_FILE}" | sed "s/${PRODUCT}-//" | sed "s/\.pivotal\$//"`

cd "${POOL_DIR}"

echo "Available products:"
$PCF products
echo

echo "Uploading ${TILE_FILE}"
$PCF import "${TILE_DIR}/${TILE_FILE}"
echo

echo "Available products:"
$PCF products
$PCF is-available "${PRODUCT}" "${VERSION}"
echo

echo "Installing product ${PRODUCT} version ${VERSION}"
$PCF install "${PRODUCT}" "${VERSION}"
echo

echo "Available products:"
$PCF products
$PCF is-installed "${PRODUCT}" "${VERSION}"
echo

echo "Configuring product ${PRODUCT}"
$PCF configure "${PRODUCT}" "${REPO_DIR}/ci/missing-properties.yml"
echo

echo "Applying Changes"
$PCF apply-changes
echo