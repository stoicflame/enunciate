package net.sf.enunciate.samples.genealogy.cite;

import net.sf.enunciate.samples.genealogy.data.Assertion;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class InfoSet {

  private String id;
  private List<Assertion> inferences;
  private Contributor submitter;
  private Source source;
  private String sourceReference;

  @XmlID
  @XmlAttribute
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @XmlIDREF
  public List<Assertion> getInferences() {
    return inferences;
  }

  public void setInferences(List<Assertion> inferences) {
    this.inferences = inferences;
  }

  @XmlElementRef
  public Contributor getSubmitter() {
    return submitter;
  }

  public void setSubmitter(Contributor submitter) {
    this.submitter = submitter;
  }

  @XmlIDREF
  public Source getSource() {
    return source;
  }

  public void setSource(Source source) {
    this.source = source;
  }

  public String getSourceReference() {
    return sourceReference;
  }

  public void setSourceReference(String sourceReference) {
    this.sourceReference = sourceReference;
  }
}
