package net.sf.enunciate.samples.genealogy.data;

import net.sf.enunciate.samples.genealogy.cite.InfoSet;

import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * An assertion of a piece of information, usually associated with a source.
 *
 * @author Ryan Heaton
 */
public abstract class Assertion {

  private String id;
  private String note;
  private InfoSet infoSet;

  /**
   * The id of the assertion.
   *
   * @return The id of the assertion.
   */
  @XmlID
  @XmlAttribute
  public String getId() {
    return id;
  }

  /**
   * The id of the assertion.
   *
   * @param id The id of the assertion.
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * A note associated with this assertion.
   *
   * @return A note associated with this assertion.
   */
  public String getNote() {
    return note;
  }

  /**
   * A note associated with this assertion.
   *
   * @param note A note associated with this assertion.
   */
  public void setNote(String note) {
    this.note = note;
  }

  /**
   * The infoset from which this assertion was made.
   *
   * @return The infoset from which this assertion was made.
   */
  @XmlIDREF
  public InfoSet getInfoSet() {
    return infoSet;
  }

  /**
   * The infoset from which this assertion was made.
   *
   * @param infoSet The infoset from which this assertion was made.
   */
  public void setInfoSet(InfoSet infoSet) {
    this.infoSet = infoSet;
  }
}
