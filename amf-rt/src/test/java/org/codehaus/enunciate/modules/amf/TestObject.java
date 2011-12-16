package org.codehaus.enunciate.modules.amf;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Map;

@XmlRootElement
@XmlSeeAlso({TestEnum.class})
public class TestObject {
  private Map<TestEnum, String> propEnum;
  private Map<String, String> propString;
  private Map.Entry[] entries;

  @XmlJavaTypeAdapter ( MapXmlAdapter.class )
  public Map<TestEnum, String> getPropEnum() {
    return propEnum;
  }

  public void setPropEnum(Map<TestEnum, String> propEnum) {
    this.propEnum = propEnum;
  }

  @XmlJavaTypeAdapter ( MapXmlAdapter.class )
  public Map<String, String> getPropString() {
    return propString;
  }

  public void setPropString(Map<String, String> propString) {
    this.propString = propString;
  }

  @XmlJavaTypeAdapter( EntryAdapter.class )
  public Map.Entry[] getEntries() {
    return entries;
  }

  public void setEntries(Map.Entry[] entries) {
    this.entries = entries;
  }
}
