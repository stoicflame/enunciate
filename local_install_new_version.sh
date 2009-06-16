#!/bin/sh

set -e;

BUILD_HOME=`pwd`;

mvn -N -DskipTests=true clean install

for i in `cat pom.xml | grep module\> | sed 's/<module>//' | sed 's/<\/module>//'`; do
  echo "building $BUILD_HOME/$i";
  cd $BUILD_HOME/$i;
  mvn -DskipTests=true clean install;
done
