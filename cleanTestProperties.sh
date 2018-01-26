#!/bin/bash

for FILE in ./src/test/resources/test/*.properties 
do
  CHECK=`head -1 $FILE | grep "#" | wc -l`
#  echo "CHECK = $CHECK"

  if [ $CHECK -eq "1" ]; then
    echo "updating $FILE"
    tail -n +3 $FILE > $FILE.tmp
    mv $FILE.tmp $FILE
  fi
done

