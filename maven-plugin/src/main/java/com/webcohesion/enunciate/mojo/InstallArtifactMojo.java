package com.webcohesion.enunciate.mojo;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Install an Enunciate-generated artifact as if it were in its own project.
 *
 * @author Ryan Heaton
 */
@Mojo ( name = "install-artifact", defaultPhase = LifecyclePhase.INSTALL, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME )
public class InstallArtifactMojo extends InstallArtifactBaseMojo {
}
