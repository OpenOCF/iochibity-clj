#!/usr/bin/env bash

#set -x

IOTIVITY_LIBPATH=$IOTIVITY_HOME/out/darwin/x86_64/release

#IOTIVITY_JNI=/usr/local/lib/iotivity/1.1
IOTIVITY_JNI=$HOME/iotivity/kit/java/jni

if [ -d "$IOTIVITY_HOME" ];
then
        if [ -d "$IOTIVITY_JNI" ]
	then
	        echo "Starting repl with java.library.path = $JLP"
	else
	        echo "java.library.path not found: $JLP";
	        # exit
	fi
else
	echo "IOTIVITY_HOME ($IOTIVITY_HOME) not found."
	exit
fi

if [ -f .boot-jvm-options ]; then
  OPTS=`cat .boot-jvm-options`
fi

export DYLD_LIBRARY_PATH=$JLP

BOOT_JVM_OPTIONS="$OPTS -Djava.library.path=$IOTIVITY_JNI:$IOTIVITY_LIBPATH" boot "$@"
