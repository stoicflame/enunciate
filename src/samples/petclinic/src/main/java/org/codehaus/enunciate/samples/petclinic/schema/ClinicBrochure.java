package org.codehaus.enunciate.samples.petclinic.schema;

import javax.xml.bind.annotation.XmlRootElement;
import javax.activation.DataHandler;
import java.util.Map;

/**
 * A clinic brochure.
 *
 * @author Ryan Heaton
 */
@XmlRootElement
public class ClinicBrochure {

  private Map<String, String> metaData;
  private DataHandler data;

  /**
   * Metadata about the brochure.  When using this as a REST payload, the metadata will be reflected in the headers.
   *
   * @return Metadata about the brochure.  
   */
  public Map<String, String> getMetaData() {
    return metaData;
  }

  /**
   * Metadata about the brochure.  When using this as a REST payload, the metadata will be reflected in the headers.
   *
   * @param metaData Metadata about the brochure.
   */
  public void setMetaData(Map<String, String> metaData) {
    this.metaData = metaData;
  }

  /**
   * The data that handles the brochure.
   *
   * @return The data that handles the brochure.
   */
  public DataHandler getData() {
    return data;
  }

  /**
   * The data that handles the brochure.
   *
   * @param data The data that handles the brochure.
   */
  public void setData(DataHandler data) {
    this.data = data;
  }
}
