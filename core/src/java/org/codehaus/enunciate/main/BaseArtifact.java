package org.codehaus.enunciate.main;

/**
 * Base implementation for an artifact.
 * 
 * @author Ryan Heaton
 */
public abstract class BaseArtifact implements Artifact {

  private final String module;
  private final String id;

  /**
   * @param module The name of the module.
   * @param id The module id.
   */
  protected BaseArtifact(String module, String id) {
    this.module = module;
    this.id = id;
  }

  /**
   * The module.
   *
   * @return The module.
   */
  public String getModule() {
    return module;
  }

  /**
   * The id.
   *
   * @return The id.
   */
  public String getId() {
    return id;
  }

  /**
   * Compares artifacts by module then by id.
   *
   * @param artifact The artifact to compare.
   * @return The comparison.
   */
  public int compareTo(Artifact artifact) {
    String thisId = this.id == null ? "" : this.id;
    String otherId = artifact.getId();
    if (otherId == null) {
      otherId = "";
    }

    return thisId.compareTo(otherId);
  }
}
