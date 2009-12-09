package org.codehaus.enunciate;

/**
 * @goal deploy-artifact
 * @phase deploy
 * @executionStrategy once-per-session
 * @extendsPlugin maven-enunciate-slim-plugin
 * 
 * @author Ryan Heaton
 */
public class DeployArtifactBaseMojo extends DeployArtifactMojo {
}
