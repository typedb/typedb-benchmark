#!/usr/bin/env bash

## Generate data sets, update streams, and parameters

set -eu
set -o pipefail

cd "$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd ..


echo "==============================================================================="
echo "Generating data for SNB Interactive v2"
echo "-------------------------------------------------------------------------------"
echo "SF: ${SF}"
echo "LDBC_SNB_DATAGEN_DIR: ${LDBC_SNB_DATAGEN_DIR}"
echo "LDBC_SNB_DATAGEN_MAX_MEM: ${LDBC_SNB_DATAGEN_MAX_MEM}"
echo "LDBC_SNB_DRIVER_DIR: ${LDBC_SNB_DRIVER_DIR}"
echo "==============================================================================="


export LDBC_SNB_IMPLS_DIR=`pwd`

USE_DATAGEN_DOCKER=${USE_DATAGEN_DOCKER:-false}
# set DATAGEN_COMMAND
if ${USE_DATAGEN_DOCKER}; then
    echo "Using Datagen Docker image"
    DATAGEN_COMMAND="docker run --volume ${LDBC_SNB_DATAGEN_DIR}/out-sf${SF}:/out ldbc/datagen-standalone:0.5.1-2.12_spark3.2 --cores 4 --parallelism 4 --memory ${LDBC_SNB_DATAGEN_MAX_MEM} --"
else
    echo "Using non-containerized Datagen"
    cd ${LDBC_SNB_DATAGEN_DIR}
    export LDBC_SNB_DATAGEN_JAR=$(sbt -batch -error 'print assembly / assemblyOutputPath')
    DATAGEN_COMMAND="tools/run.py --cores 4 --parallelism 4 --memory ${LDBC_SNB_DATAGEN_MAX_MEM} -- --output-dir ${LDBC_SNB_DATAGEN_DIR}/out-sf${SF}"
fi

cd ${LDBC_SNB_IMPLS_DIR}


echo "==================== Cleanup existing directories ===================="
mkdir -p update-streams-sf${SF}/
mkdir -p parameters-sf${SF}/
rm -rf ${LDBC_SNB_IMPLS_DIR}/update-streams-sf${SF}/*
rm -rf ${LDBC_SNB_IMPLS_DIR}/parameters-sf${SF}/*


echo "==================== Generate data for update streams and factors ===================="
cd ${LDBC_SNB_DATAGEN_DIR}
if ${USE_DATAGEN_DOCKER} && [ -d out-sf${SF} ] && [ ${OSTYPE} != "darwin"* ]; then
    sudo chown -R $(id -u):$(id -g) out-sf${SF}
fi
rm -rf out-sf${SF}

if ${USE_DATAGEN_DOCKER} && [ -d out-sf${SF} ] && [ ${OSTYPE} != "darwin"* ]; then
    sudo chown -R $(id -u):$(id -g) out-sf${SF}
fi
rm -rf out-sf${SF}/graphs/parquet/raw
${DATAGEN_COMMAND} \
    --mode bi \
    --format parquet \
    --scale-factor ${SF} \
    --generate-factors

if ${USE_DATAGEN_DOCKER} && [ ${OSTYPE} != "darwin"* ]; then
    sudo chown -R $(id -u):$(id -g) out-sf${SF}
fi


echo "==================== Generate parameters ===================="
cd ${LDBC_SNB_DRIVER_DIR}/paramgen
export LDBC_SNB_DATA_ROOT_DIRECTORY=${LDBC_SNB_DATAGEN_DIR}/out-sf${SF}/
export LDBC_SNB_FACTOR_TABLES_DIR=${LDBC_SNB_DATA_ROOT_DIRECTORY}/factors/parquet/raw/composite-merged-fk/

if [[ $SF < 1 ]]
then
    export LDBC_THRESHOLD_VALUE_FILE='paramgen_window_values_test.json'
else
    export LDBC_THRESHOLD_VALUE_FILE='paramgen_window_values.json'
fi

# Instead of using the scripts/paramgen.sh script, we call paramgen manually
# and parameterize it with the ${LDBC_THRESHOLD_VALUE_FILE} file
# See the rationale for this at https://github.com/ldbc/ldbc_snb_datagen_spark/issues/429
python3 paramgen.py \
    --raw_parquet_dir "${LDBC_SNB_DATA_ROOT_DIRECTORY}/graphs/parquet/raw/" \
    --factor_tables_dir "${LDBC_SNB_FACTOR_TABLES_DIR}/" \
    --time_bucket_size_in_days 1 \
    --generate_short_query_parameters True \
    --threshold_values_path ${LDBC_THRESHOLD_VALUE_FILE}

cd ..

mv parameters/*.parquet ${LDBC_SNB_IMPLS_DIR}/parameters-sf${SF}/


echo "==================== Generate update streams ===================="
cd ${LDBC_SNB_DRIVER_DIR}
cd scripts
export LDBC_SNB_DATA_ROOT_DIRECTORY=${LDBC_SNB_DATAGEN_DIR}/out-sf${SF}/

./convert.sh
mv inserts/ ${LDBC_SNB_IMPLS_DIR}/update-streams-sf${SF}/
mv deletes/ ${LDBC_SNB_IMPLS_DIR}/update-streams-sf${SF}/


echo "==================== Generate data for Cypher ===================="
cd ${LDBC_SNB_DATAGEN_DIR}
if ${USE_DATAGEN_DOCKER} && [ -d out-sf${SF} ] && [ ${OSTYPE} != "darwin"* ]; then
    sudo chown -R $(id -u):$(id -g) out-sf${SF}
fi
rm -rf out-sf${SF}/graphs/parquet/raw
${DATAGEN_COMMAND} \
    --mode bi \
    --format csv \
    --scale-factor ${SF} \
    --explode-edges \
    --epoch-millis \
    --format-options header=false,quoteAll=true,compression=gzip


echo "==================== Generate data for Postgres =================="
cd ${LDBC_SNB_DATAGEN_DIR}
if ${USE_DATAGEN_DOCKER} && [ -d out-sf${SF} ] && [ ${OSTYPE} != "darwin"* ]; then
    sudo chown -R $(id -u):$(id -g) out-sf${SF}
fi
rm -rf out-sf${SF}/graphs/parquet/raw
${DATAGEN_COMMAND} \
    --mode bi \
    --format csv \
    --scale-factor ${SF}
