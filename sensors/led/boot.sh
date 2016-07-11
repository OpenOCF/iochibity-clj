#!/usr/bin/env bash

#set -x

#IOTIVITY_LIBPATH=$IOTIVITY_HOME/out/darwin/x86_64/release

#IOTIVITY_JNI=/usr/local/lib/iotivity/1.1

JLP=

if [ -d "$IOTIVITY_LIBPATH" ];
then
        if [ -d "$IOTIVITY_JNIPATH" ]
	then
	        JLP=$IOTIVITY_JNIPATH:$IOTIVITY_LIBPATH
	        echo "java.library.path = $JLP"
	else
	        echo "IOTIVITY_JNIPATH ($IOTIVITY_JNIPATH) not found.";
	        echo "Assuming JNI lib is in IOTIVITY_LIBPATH ($IOTIVITY_LIBPATH).";
	        JLP=$IOTIVITY_LIBPATH
	        echo "java.library.path = $JLP"
	fi
else
	echo "IOTIVITY_LIBPATH ($IOTIVITY_LIBPATH) not found.  Exiting."
	exit
fi

if [ -f .boot-jvm-options ]; then
  OPTS=`cat .boot-jvm-options`
fi

export DYLD_LIBRARY_PATH=$JLP

BOOT_JVM_OPTIONS="$OPTS -Djava.library.path=$JLP" boot "$@"
