package org.codehaus.enunciate;

/**
 * Assembles the whole Enunciate app, with Spring support, without compilation or packaging of the war.
 * For use with the "war" packaging.
 *
 * @goal assemble
 * @extendsPlugin enunciate
 * @phase process-sources
 * @requiresDependencyResolution compile
 * @executionStrategy once-per-session
 *
 * @author Ryan Heaton
 */
public class AssembleSpringMojo extends AssembleMojo {

}
