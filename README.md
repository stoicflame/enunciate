# Enunciate #

Enunciate is a build-time Web service enhancement tool that can
be applied to Java-based projects for generating a lot of cool
artifacts from the source code of your Web service endpoints.

For more information, see the project site at http://enunciate.webcohesion.com.

## Building Enunciate ###

You need at least Java JDK 8 to build. Make sure JAVA_HOME is set to >=JDK 8 before running Maven:

    export JAVA_HOME=/PATH/TO/JDK/8
    mvn clean install

For the "full" build (required for deploy), Enunciate runs tests for the generated client-side code that it 
develops. So in order to run these tests, you're going to need to install some "unusual" libraries for 
things like C/C++ (libxml2), Objective-C (GNUStep), and C# (Mono).

### Ubuntu ###

Here are the packages you'll need to install to run the full build on Ubuntu:

```sudo apt-get install libxml2-dev mono-gmcs gnustep gnustep-devel ruby rubygems ruby-dev php5 openjdk-7-jdk```

And then install the ruby json gem:

```sudo gem install json```
