This is another sample application that demonstrates an AJAX app with an embedded Flash component.
It was used to illustrate my tutorials on java.net.

If you'd like to try this example out on your local box, make sure to edit the "enunciate.xml" file
to point Enunciate to the right directories for the GWT and Flex SDK.

If you're using a version of GWT newer than 1.5, you'll have to edit the pom file and update the GWT
dependencies as described in the comments.

Once you're configured correctly, you should be able to run "mvn jetty:run-war" from the command line
and see the addressbook sample at http://localhost:8080/addressbook/addressbook/index.html.