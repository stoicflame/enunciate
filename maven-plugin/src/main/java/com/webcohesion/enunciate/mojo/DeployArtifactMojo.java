package com.webcohesion.enunciate.mojo;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Deploy an Enunciate-generated artifact as if it were in its own project.
 * 
 * @author Ryan Heaton
 */
@Mojo ( name = "deploy-artifact", defaultPhase = LifecyclePhase.DEPLOY, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME )
public class DeployArtifactMojo extends DeployArtifactBaseMojo {
}
