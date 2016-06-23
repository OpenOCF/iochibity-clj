#!/usr/bin/env bash

set -x
JLP=${IOTIVITY_HOME}/out/darwin/x86_64/release


if [ -f .boot-jvm-options ]; then
  OPTS=`cat .boot-jvm-options`
fi

BOOT_JVM_OPTIONS="$OPTS -Djava.library.path=$JLP" boot "$@"
