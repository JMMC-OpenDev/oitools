#!/bin/bash
#
# Update all REFERENCE files from existing TEST files
#
# First, run a complete build & test:
#   mvn clean install
#

./sortTestProperties.sh

meld ./src/test/resources/test/ ./src/test/resources/ref

./copyTestOIFits.sh
./copyTestXmlCsv.sh
./copyTestProperties.sh

cd rules
./toHtml.sh

