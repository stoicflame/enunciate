package net.sf.enunciate.samples.genealogy.services;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Ryan Heaton
 */
@XmlRootElement (
  namespace = "http://enunciate.sf.net/samples/full"
)
public class UnknownSourceBean {

  private String sourceId;
  private int errorCode;

  public String getSourceId() {
    return sourceId;
  }

  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }

  public int getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(int errorCode) {
    this.errorCode = errorCode;
  }
}
