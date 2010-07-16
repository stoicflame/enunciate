Welcome.

Enunciate runs tests for the generated client-side code that it develops. So in order to run these tests,
you're going to need to install some "unusual" libraries for things like C/C++ (libxml2), Objective-C
(GNUStep), and C# (Mono).

-----------------
Ubuntu
-----------------

I run Ubuntu, so this is what I need in order to build Enunciate:

sudo apt-get install libxml2-dev mono-gmcs gnustep gnustep-devel ruby rubgems ruby-dev

and then to install the ruby json gem:

sudo gem install json
