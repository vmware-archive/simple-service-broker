#!/bin/sh -e

set -e

# echo "### Skipping remove"
# exit 0

TILE_GEN_DIR="$( cd "$1" && pwd )"
TILE_DIR="$( cd "$2" && pwd )"
POOL_DIR="$( cd "$3" && pwd )"

TILE_FILE=`cd "${TILE_DIR}"; ls *.pivotal`
if [ -z "${TILE_FILE}" ]; then
	echo "No files matching ${TILE_DIR}/*.pivotal"
	ls -lR "${TILE_DIR}"
	exit 1
fi

PRODUCT=`echo "${TILE_FILE}" | sed "s/-[^-]*$//"`

BIN_DIR="$( cd "${TILE_GEN_DIR}/bin" && pwd )"

PCF="${BIN_DIR}/pcf"

cd "${POOL_DIR}"

echo "Available products:"
$PCF products
echo

if ! $PCF is-installed "${PRODUCT}" ; then
	echo "${PRODUCT} not installed - skipping removal"
	exit 0
fi

echo "Uninstalling ${PRODUCT}"
$PCF uninstall "${PRODUCT}"
echo

echo "Applying Changes"
$PCF apply-changes
echo

echo "Available products:"
$PCF products
echo

if $PCF is-installed "${PRODUCT}" ; then
	echo "${PRODUCT} remains installed - remove failed"
	exit 1
fi
