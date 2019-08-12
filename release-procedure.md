1. Make sure any new modules are included
  1. Make sure the parent pom includes any new modules.
  2. Make sure the top pom includes any new modules.
2. Replace all instances of old_version with new_version (do a replaceAll operation, note that maven-release-plugin
    isn't viable until ENUNCIATE-501 is fixed, which depends on MRELEASE-669)
3. `mvn clean install` (to run all the tests)
4. Commit and tag (e.g. `git tag -a v2.0.0 -m "Version 2.0.0"`).
5. `mvn clean deploy -P release` (your new version should now be deployed)
6. Build the distribution from the LAST RELEASE TAG (e.g. `ant -f build-site.xml dist`)
7. Push.
8. Reset HEAD to a snapshot version.
9. [Create the release](https://github.com/stoicflame/enunciate/releases).
10. Make any announcements.
11. Do a "replace all" of old_version to new_version in the wiki. (e.g. `find . -name "*.md" | xargs sed -i 's/2.0.0/2.0.1/g'`)
12. Upload any new schemas to the pages.
13. Update the [Getting Started Sample](https://github.com/stoicflame/enunciate-sample).
14. Update and publish the [Gradle Plugin](https://github.com/stoicflame/enunciate-gradle).
  1. Update build.gradle to the new version
  2. ./gradlew publishPlugins
  3. Update the docs.  (e.g. `find . -name "*.md" | xargs sed -i 's/2.0.0/2.0.1/g'`)
