1. Update the documentation to reflect any changes.
    1. Make sure you've updated the insert sidebox on the index page.
    2. Make sure you've updated the downloads page to reflect where the new files will be.
    3. Make sure you've also updated the version in the executables.html page.
    4. Make sure you're also updated the version maven plugin in the sample poms.
2. Make sure any new modules are included
    1. Make sure the parent pom includes any new modules.
    2. Make sure the top pom includes any new modules.
    3. Make sure the rt pom includes any new modules.
    4. Make sure the build-site.xml file includes any new modules (either optional or in core.libs.fileset)
3. replace all instances of old_version with new_version (do a replaceAll operation, note that maven-release-plugin
    isn't viable until ENUNCIATE-501 is fixed, which depends on MRELEASE-669)
4. `mvn clean install` (to run all the tests)
5. Make sure the samples are working.
6. `mvn clean deploy` (your new version should now be deployed)
7. Build the distribution from the LAST RELEASE TAG (e.g. `ant -f build-site.xml dist`)
8. `cadaver https://dav.codehaus.org/dist/enunciate/` (`https://dav.codehaus.org/snapshots.dist/enunciate/` for snapshot dist)
    1. `put target/enunciate-VERSION.zip`
    2. `put target/enunciate-VERSION.tar.gz`
9. `sitecopy --update enunciate`
10. Make your announcements.
    * mailing lists
    * webcohesion blog
    * java.net?
    * serverside.com?
    * javalobby?
11. Update the WIKI, close all JIRA issues for last version.  Announce version release in JIRA.
