package org.codehaus.enunciate;

/**
 * Simple relocation of the {@link DocsMojo}.
 *
 * @goal docs
 * @extendsPlugin maven-enunciate-slim-plugin
 * @phase process-sources
 * @requiresDependencyResolution compile
 * @executionStrategy once-per-session
 *
 * @author Ryan Heaton
 */
public class DocsBaseMojo extends DocsMojo {

}