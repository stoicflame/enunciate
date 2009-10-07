package org.codehaus.enunciate;

/**
 * Simple relocation of the {@link AssembleMojo}.
 *
 * @goal assemble
 * @extendsPlugin maven-enunciate-slim-plugin
 * @phase process-sources
 * @requiresDependencyResolution compile
 * @executionStrategy once-per-session
 *
 * @author Ryan Heaton
 */
public class AssembleBaseMojo extends AssembleMojo {

}
