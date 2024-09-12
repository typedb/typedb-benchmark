#!/usr/bin/env bash

set -eu
set -o pipefail

cd "$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd ..

# BENCHMARK_PROPERTIES_FILE=${1:-driver/benchmark-sf0.01.properties}
BENCHMARK_PROPERTIES_FILE=${1:-driver/benchmark.properties}

# java -cp target/typeql-2.0.0-SNAPSHOT.jar org.ldbcouncil.snb.driver.Client -P ${BENCHMARK_PROPERTIES_FILE}
java -cp target/dependency/*:target/typeql-2.0.0-SNAPSHOT.jar org.ldbcouncil.snb.driver.Client -P ${BENCHMARK_PROPERTIES_FILE}
