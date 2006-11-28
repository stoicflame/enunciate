package net.sf.enunciate.samples.genealogy.data;

/**
 * @author Ryan Heaton
 */
public class Relationship extends Assertion {

  private RelationshipType type;
  private Name sourcePersonName;
  private Name targetPersonName;

  public RelationshipType getType() {
    return type;
  }

  public void setType(RelationshipType type) {
    this.type = type;
  }

  public Name getSourcePersonName() {
    return sourcePersonName;
  }

  public void setSourcePersonName(Name sourcePersonName) {
    this.sourcePersonName = sourcePersonName;
  }

  public Name getTargetPersonName() {
    return targetPersonName;
  }

  public void setTargetPersonName(Name targetPersonName) {
    this.targetPersonName = targetPersonName;
  }
}
