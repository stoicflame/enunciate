package org.codehaus.enunciate.samples.schema;

import java.util.HashMap;

/**
 * @author Ryan Heaton
 */
public class MapExtended extends HashMap<String, Object> {

  private String customField;

  public String getCustomField() {
    return customField;
  }

  public void setCustomField(String customField) {
    this.customField = customField;
  }
}
