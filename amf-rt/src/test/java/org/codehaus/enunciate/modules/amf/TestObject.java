package org.codehaus.enunciate.modules.amf;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Map;

@XmlRootElement
public class TestObject {
  private Map<String, String> prop;
  private Map.Entry[] entries;

  @XmlJavaTypeAdapter ( MapXmlAdapter.class )
  public Map<String, String> getProp() {
    return prop;
  }

  public void setProp(Map<String, String> prop) {
    this.prop = prop;
  }

  @XmlJavaTypeAdapter( EntryAdapter.class )
  public Map.Entry[] getEntries() {
    return entries;
  }

  public void setEntries(Map.Entry[] entries) {
    this.entries = entries;
  }
}
