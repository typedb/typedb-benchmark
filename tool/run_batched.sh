#!/bin/bash

# usage: for ...; do echo cmd; done | run_batched.sh

MAX_JOBS=10

while true; do
    CUR_JOBS=$(jobs | wc -l)
    if ((CUR_JOBS < MAX_JOBS)); then
        read cmd || break
        echo "$cmd"
        bash -c "$cmd" &
    else
        sleep 60
    fi
done

echo "scheduled all jobs"

wait $(jobs -p)
