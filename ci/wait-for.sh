#!/usr/bin/env bash

set -ex

until [ -f "${1}" ]; do echo "Waiting for file ${1} to be created."; sleep 1; done
