package net.sf.enunciate.main;

/**
 * An artifact that supports a name. 
 *
 * @author Ryan Heaton
 */
public interface NamedArtifact extends Artifact {

  /**
   * The name of the artifact.
   *
   * @return The name of the artifact.
   */
  String getName();
  
}
