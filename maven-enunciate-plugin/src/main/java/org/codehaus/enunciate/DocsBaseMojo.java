package org.codehaus.enunciate;

/**
 * Simple relocation of the {@link DocsMojo}.
 *
 * @goal docs
 * @phase process-sources
 * @requiresDependencyResolution test
 * @extendsPlugin maven-enunciate-slim-plugin
 *
 * @author Ryan Heaton
 */
public class DocsBaseMojo extends DocsMojo {

}