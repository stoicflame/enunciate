package org.codehaus.enunciate;

/**
 * Simple relocation of the {@link AssembleMojo}.
 *
 * @goal assemble
 * @phase process-sources
 * @requiresDependencyResolution test
 * @extendsPlugin maven-enunciate-slim-plugin
 *
 * @author Ryan Heaton
 */
public class AssembleBaseMojo extends AssembleMojo {

}
