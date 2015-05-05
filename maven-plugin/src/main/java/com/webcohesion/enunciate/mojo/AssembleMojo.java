package com.webcohesion.enunciate.mojo;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Assembles the Enunciate documentation.
 *
 * @author Ryan Heaton
 */
@Mojo ( name = "assemble", defaultPhase = LifecyclePhase.PROCESS_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME )
public class AssembleMojo extends AssembleBaseMojo {

}
