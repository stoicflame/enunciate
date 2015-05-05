package org.codehaus.enunciate.samples.petclinic.schema;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.InputStream;

/**
 * An animal brochure.
 *
 * @author Ryan Heaton
 */
@XmlRootElement
public class AnimalBrochure {

  private String contentType;
  private InputStream content;

  /**
   * The content type (MIME type) of the brochure.
   *
   * @return The content type (MIME type) of the brochure.
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * The content type (MIME type) of the brochure.
   *
   * @param contentType The content type (MIME type) of the brochure.
   */
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  /**
   * The content of the brochure.
   *
   * @return The content of the brochure.
   */
  @XmlTransient //JAXB doesn't recognize InputStream as a valid type.
  public InputStream getContent() {
    return content;
  }

  /**
   * The content of the brochure.
   *
   * @param content The content of the brochure.
   */
  public void setContent(InputStream content) {
    this.content = content;
  }
}
