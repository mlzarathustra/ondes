#!/bin/bash

. ondes-args

D=build/classes/java/main
R=build/resources/main
J=../ondes/build/libs/ondes-all.jar

java -cp $J \
    ondes.tools.WaveEditor \
    "${ONDES_ARGS[@]}"

