package net.sf.enunciate.samples.genealogy.services;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Information about an unknown source.
 * @author Ryan Heaton
 */
@XmlRootElement (
  namespace = "http://enunciate.sf.net/samples/full"
)
public class UnknownSourceBean {

  private String sourceId;
  private int errorCode;

  /**
   * The id of the source.
   *
   * @return The id of the source.
   */
  public String getSourceId() {
    return sourceId;
  }

  /**
   * The id of the source.
   *
   * @param sourceId The id of the source.
   */
  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }

  /**
   * The error code.
   *
   * @return The error code.
   */
  public int getErrorCode() {
    return errorCode;
  }

  /**
   * The error code.
   *
   * @param errorCode The error code.
   */
  public void setErrorCode(int errorCode) {
    this.errorCode = errorCode;
  }
}
