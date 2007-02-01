package net.sf.enunciate.samples.genealogy.data;

import javax.xml.bind.annotation.XmlType;

/**
 * A type of relationship.
 *
 * @author Ryan Heaton
 */
@XmlType (
  name=""
)
public enum RelationshipType {

  /**
   * indicates a spouse relationship.
   */
  spouse,

  /**
   * indicates a parent relationship.
   */
  parent,

  /**
   * indicates a child relationship.
   */
  child
}
