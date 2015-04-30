package org.codehaus.enunciate;

/**
 * A project artifact.
 *
 * @author Ryan Heaton
 */
public class Artifact {

  private String enunciateArtifactId;
  private String classifier;
  private String artifactType;

  /**
   * The id of the enunciate artifact that is to be a project artifact.
   *
   * @return The id of the enunciate artifact that is to be a project artifact.
   */
  public String getEnunciateArtifactId() {
    return enunciateArtifactId;
  }

  /**
   * The id of the enunciate artifact that is to be a project artifact.
   *
   * @param enunciateArtifactId The id of the enunciate artifact that is to be a project artifact.
   */
  public void setEnunciateArtifactId(String enunciateArtifactId) {
    this.enunciateArtifactId = enunciateArtifactId;
  }

  /**
   * The artifact id.
   *
   * @return The artifact id.
   */
  public String getClassifier() {
    return classifier;
  }

  /**
   * The artifact id.
   *
   * @param classifier The artifact id.
   */
  public void setClassifier(String classifier) {
    this.classifier = classifier;
  }

  /**
   * The packaging of the artifact.
   *
   * @return The packaging of the artifact.
   */
  public String getArtifactType() {
    return artifactType;
  }

  /**
   * The packaging of the artifact.
   *
   * @param artifactType The packaging of the artifact.
   */
  public void setArtifactType(String artifactType) {
    this.artifactType = artifactType;
  }
}
