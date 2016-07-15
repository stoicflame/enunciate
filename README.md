[![Build Status](https://travis-ci.org/CIResearchGroup/enunciate.svg?branch=master)](https://travis-ci.org/CIResearchGroup/enunciate)
# Enunciate #

Enunciate is a build-time Web service enhancement tool that can
be applied to Java-based projects for generating a lot of cool
artifacts from the source code of your Web service endpoints.

For more information, see the project site at http://enunciate.webcohesion.com.

## Building Enunciate ###

Enunciate runs tests for the generated client-side code that it develops. So in order to run these tests,
you're going to need to install some "unusual" libraries for things like C/C++ (libxml2), Objective-C
(GNUStep), and C# (Mono).

You need Java JDK 7 to build Enunciate. Currently, it doesn't build with Java JDK 8. Make sure Maven is
using Java JDK 7 by setting JAVA_HOME before running Maven:

    export JAVA_HOME=/PATH/TO/JDK/7
    mvn clean install

### Ubuntu ###

Here are the packages you'll need to install to run the full build on Ubuntu:

```sudo apt-get install libxml2-dev mono-gmcs gnustep gnustep-devel ruby rubygems ruby-dev php5 openjdk-7-jdk```

And then install the ruby json gem:

```sudo gem install json```
