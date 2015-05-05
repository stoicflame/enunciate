package com.webcohesion.enunciate.mojo;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Generates the Enunciate documentation, including any client-side libraries.
 *
 * @author Ryan Heaton
 */
@Mojo ( name = "docs", defaultPhase = LifecyclePhase.PROCESS_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME )
public class DocsMojo extends DocsBaseMojo {

}