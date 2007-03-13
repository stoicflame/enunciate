package org.codehaus.enunciate.main;

import java.util.Collection;

/**
 * A bundle of artifacts that are logically grouped together.  An artifact bundle is
 * itself an artifact.
 *
 * @author Ryan Heaton
 */
public interface ArtifactBundle extends Artifact {

  /**
   * Get the artifacts that are associated with this bundle.
   *
   * @return The artifacts that are associated with this bundle.
   */
  Collection<? extends Artifact> getArtifacts();
}
