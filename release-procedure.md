1. Make sure any new modules are included
  1. Make sure the parent pom includes any new modules.
  2. Make sure the top pom includes any new modules.
  3. Make sure the rt pom includes any new modules.
  4. Make sure the build-site.xml file includes any new modules (either optional or in core.libs.fileset)
2. Replace all instances of old_version with new_version (do a replaceAll operation, note that maven-release-plugin
    isn't viable until ENUNCIATE-501 is fixed, which depends on MRELEASE-669)
3. `mvn clean install` (to run all the tests)
4. Make sure the samples are working.
5. `mvn clean deploy -P release` (your new version should now be deployed)
6. Build the distribution from the LAST RELEASE TAG (e.g. `ant -f build-site.xml dist`)
7. [Create the release](https://github.com/stoicflame/enunciate/releases).
8. Make any announcements.
    * webcohesion blog
    * javalobby?
9. Do a "replace all" of old_version to new_version in the wiki.
