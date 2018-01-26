#!/bin/bash

./cleanTestProperties.sh

for FILE in ./src/test/resources/test/*.properties 
do
  echo "sorting $FILE"
  sort $FILE > $FILE.tmp
  mv $FILE.tmp $FILE
done

