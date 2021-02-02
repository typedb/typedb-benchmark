#!/usr/bin/env bash

set -ex

bash -c 'echo $BENCHMARK_PRIVATE_KEY | base64 -d > ~/.ssh/id_rsa'
chmod 400 ~/.ssh/id_rsa
bash -c 'echo $BENCHMARK_PUBLIC_KEY | base64 -d >> ~/.ssh/authorized_keys'
