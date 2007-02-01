package net.sf.enunciate.samples.genealogy.data;

/**
 * A relationship between two people.
 *
 * @author Ryan Heaton
 */
public class Relationship extends Assertion {

  private RelationshipType type;
  private Name sourcePersonName;
  private Name targetPersonName;

  /**
   * The relationship type.
   *
   * @return The relationship type.
   */
  public RelationshipType getType() {
    return type;
  }

  /**
   * The relationship type.
   *
   * @param type The relationship type.
   */
  public void setType(RelationshipType type) {
    this.type = type;
  }

  /**
   * The name of the source person.
   *
   * @return The name of the source person.
   */
  public Name getSourcePersonName() {
    return sourcePersonName;
  }

  /**
   * The name of the source person.
   *
   * @param sourcePersonName The name of the source person.
   */
  public void setSourcePersonName(Name sourcePersonName) {
    this.sourcePersonName = sourcePersonName;
  }

  /**
   * The name of the target person.
   *
   * @return The name of the target person.
   */
  public Name getTargetPersonName() {
    return targetPersonName;
  }

  /**
   * The name of the target person.
   *
   * @param targetPersonName The name of the target person.
   */
  public void setTargetPersonName(Name targetPersonName) {
    this.targetPersonName = targetPersonName;
  }

}
