#!/usr/bin/env bash

set -e
set -o pipefail

SCRIPT=$1; shift
LOG=$1; shift

${SCRIPT} "$@" 2>&1 | tee ${LOG}

echo "Log output script complete"
