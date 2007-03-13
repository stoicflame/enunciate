package org.codehaus.enunciate.samples.genealogy.cite;

import org.codehaus.enunciate.samples.genealogy.data.Assertion;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * A set of information.
 *
 * @author Ryan Heaton
 */
public class InfoSet {

  private String id;
  private List<Assertion> inferences;
  private Contributor submitter;
  private Source source;
  private String sourceReference;

  /**
   * The id of the infoset.
   *
   * @return The id of the infoset.
   */
  @XmlID
  @XmlAttribute
  public String getId() {
    return id;
  }

  /**
   * The id of the infoset.
   *
   * @param id The id of the infoset.
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * The inferences that make up this infoset.
   *
   * @return The inferences that make up this infoset.
   */
  @XmlIDREF
  public List<Assertion> getInferences() {
    return inferences;
  }

  /**
   * The inferences that make up this infoset.
   *
   * @param inferences The inferences that make up this infoset.
   */
  public void setInferences(List<Assertion> inferences) {
    this.inferences = inferences;
  }

  /**
   * The submitter of the information.
   *
   * @return The submitter of the information.
   */
  @XmlElementRef
  public Contributor getSubmitter() {
    return submitter;
  }

  /**
   * The submitter of the information.
   *
   * @param submitter The submitter of the information.
   */
  public void setSubmitter(Contributor submitter) {
    this.submitter = submitter;
  }

  /**
   * The source of the information.
   *
   * @return The source of the information.
   */
  @XmlIDREF
  public Source getSource() {
    return source;
  }

  /**
   * The source of the information.
   *
   * @param source The source of the information.
   */
  public void setSource(Source source) {
    this.source = source;
  }

  /**
   * A reference within the source of this information.
   *
   * @return A reference within the source of this information.
   */
  public String getSourceReference() {
    return sourceReference;
  }

  /**
   * A reference within the source of this information.
   *
   * @param sourceReference A reference within the source of this information.
   */
  public void setSourceReference(String sourceReference) {
    this.sourceReference = sourceReference;
  }
}
