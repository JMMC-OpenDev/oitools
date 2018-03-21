#!/bin/bash
#
# Update the REFERENCE xml & csv files (from TEST files)
#

cp -v ./src/test/resources/test/*.xml ./src/test/resources/ref/
cp -v ./src/test/resources/test/*.csv ./src/test/resources/ref/

