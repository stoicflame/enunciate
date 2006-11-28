package net.sf.enunciate.samples.genealogy.data;

import javax.xml.bind.annotation.XmlType;

/**
 * @author Ryan Heaton
 */
@XmlType (
  name=""
)
public enum RelationshipType {

  spouse,

  parent,

  child
}
