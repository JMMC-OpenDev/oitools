#!/bin/bash
#
# CLI OIFitsProcessor
#
# list:
#   ./oifitsProcessor.sh list  /home/bourgesl/oidata/2007-06-29.fits
#

java -cp ./target/oitools-TRUNK.jar fr.jmmc.oitools.OIFitsProcessor $*;
