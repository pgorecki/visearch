#!/bin/bash

baseDir=$PWD
projects=(crawler analyzer clustering)

rm "$baseDir/*.jar"

for p in ${projects[*]}
do
  printf "    %s\n" $p
  cd "projects/$p"
  mvn clean compile assembly:single
  #printf "code %s\n" "$result"
  if [[ $? != 0 ]]
  then
    printf "Error!!\n"
    exit 1
  else
    printf "Success!!!\n"
    cp target/*with-dependencies.jar "$baseDir/build"
  fi
  cd $baseDir
done

scp build/*.jar grant@213.184.8.84:~/visearch

#cd projects/crawler
#result=$(mvn clean compile assembly:single)
#printf "XX%dXX\n" "$result"
#cp target/*with-dependencies.jar ../../build
