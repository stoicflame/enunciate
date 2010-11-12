Introduction
============
This is the sample pet clinic application for Enunciate.  It exists to
demonstrate Enunciate's GWT and AMF (Flex) development features. Maven
is used to build this example application.

Maven invocation leverages the gwt-maven-plugin and the flexmojos to compile
the application. To build and deploy the entire application:

mvn jetty:run-war

Then you can hit http://localhost:8080/petclinic/petclinic/petclinic.html
to see the application.