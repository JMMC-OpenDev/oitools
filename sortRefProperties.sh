#!/bin/bash

for FILE in ./src/test/resources/ref/*.properties 
do
  echo "sorting $FILE"
  sort $FILE > $FILE.tmp
  mv $FILE.tmp $FILE
done

