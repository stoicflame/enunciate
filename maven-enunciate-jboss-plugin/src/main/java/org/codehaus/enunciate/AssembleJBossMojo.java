package org.codehaus.enunciate;

/**
 * Assembles the whole Enunciate app, with CXF and Spring support, without compilation or packaging of the war.
 * For use with the "war" packaging.
 *
 * @goal assemble
 * @extendsPlugin enunciate
 * @phase process-sources
 * @requiresDependencyResolution test
 *
 * @author Ryan Heaton
 */
public class AssembleJBossMojo extends AssembleMojo {

}
