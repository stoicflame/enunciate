Introduction
============
This is the sample pet clinic application for Enunciate.  You'll note
there's an Ant build file and a Maven2 POM file in this directory.

Whether you invoke Enunciate on the sample code via Ant, Maven2, or
the command line, these examples each do the same thing:

1. Invoke Enunciate on all of the java source files in the "src/main/java"
   directory and with the config file enunciate.xml.
2. Export the full-featured war to target/petclinic.war. (Note: the maven
   pom appends the version, i.e. target/petclinic-1.0-SNAPSHOT.war)

The resulting war can be deployed into your favorite application container.
The index page will contain the familiar API documentation, but you can
navigate to "petclinic.html" to see the fully-functional GWT-compiled
AJAX application.

NOTE: Whether invoking Enunciate via Maven or via Ant, you must export the
'gwt.home' system property that should point to the GWT home directory.

Invocation via Ant
==================
Run "ant -Dgwt.home=/path/to/gwt/home" in this directory.


Invocation via Maven
====================
Run "mvn -Dgwt.home=/path/to/gwt/home package" in this directory.  You can
also run "mvn -Dgwt.home=/path/to/gwt/home jetty:run-war" to automatically
run the war.  Then you can hit http://localhost:8080/petclinic/petclinic.html
to see the application.