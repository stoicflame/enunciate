# Enunciate #

Enunciate is a build-time Web service enhancement tool that can
be applied to Java-based projects for generating a lot of cool
artifacts from the source code of your Web service endpoints.

For more information, see the project site at http://enunciate.codehaus.org.

## Building Enunciate ###

Enunciate runs tests for the generated client-side code that it develops. So in order to run these tests,
you're going to need to install some "unusual" libraries for things like C/C++ (libxml2), Objective-C
(GNUStep), and C# (Mono).

### Ubuntu ###

Here are the packages you'll need to install to run the full build on Ubuntu:

```sudo apt-get install libxml2-dev mono-gmcs gnustep gnustep-devel ruby rubgems ruby-dev```

And then install the ruby json gem:

```sudo gem install json```
