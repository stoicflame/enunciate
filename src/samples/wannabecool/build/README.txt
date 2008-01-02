Introduction
============
This is the sample code for step 4 of the getting started guide in
the Enunciate documentation.  You'll note there's an Ant build file
and a Maven2 POM file in this directory.  You can also invoke
Enunciate via the command line.

Whether you invoke Enunciate on the sample code via Ant, Maven2, or
the command line, these examples each do the same thing:

1. Invoke Enunciate on all of the java source files in the "src/main/java"
   directory and with the config file src/main/java/enunciate.xml.
2. Export the full-featured war to target/wannabecool.war. (Note: the maven
   pom appends the version, i.e. target/wannabecool-1.6.war)
3. Export the client-side jar for accessing the published remote API via
   JDK 1.5 to the file target/wannabecool-client.jar.


Invocation via Ant
==================
Run "ant" in this directory.


Invocation via Maven
====================
Run "mvn package" in this directory.  "mvn install" will install the war
in your local repo.


Invocation via the command line
===============================
Make sure your path points to the "bin" directory of the dist, then:

enunciate -f src/main/java/enunciate.xml\
 -Exfire.war target/wannabecool.war\
 -Eclient.jdk14.library.binaries target/wannabecool-client.jar\
 src/main/java/com/ifyouwannabecool/api/package-info.java\
 src/main/java/com/ifyouwannabecool/api/ExclusiveGroupException.java\
 src/main/java/com/ifyouwannabecool/api/PermissionDeniedException.java\
 src/main/java/com/ifyouwannabecool/api/LinkageService.java\
 src/main/java/com/ifyouwannabecool/api/PersonaService.java\
 src/main/java/com/ifyouwannabecool/impl/PersonaServiceImpl.java\
 src/main/java/com/ifyouwannabecool/impl/LinkageServiceImpl.java\
 src/main/java/com/ifyouwannabecool/domain/persona/package-info.java\
 src/main/java/com/ifyouwannabecool/domain/persona/Name.java\
 src/main/java/com/ifyouwannabecool/domain/persona/Persona.java\
 src/main/java/com/ifyouwannabecool/domain/link/package-info.java\
 src/main/java/com/ifyouwannabecool/domain/link/Link.java\
 src/main/java/com/ifyouwannabecool/domain/link/SocialGroup.java