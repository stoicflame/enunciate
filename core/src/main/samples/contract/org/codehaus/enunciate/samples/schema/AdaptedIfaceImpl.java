package org.codehaus.enunciate.samples.schema;

import javax.xml.bind.annotation.XmlType;

/**
 * @author Ryan Heaton
 */
@XmlType( name = "adaptediface", namespace = "urn:adaptediface" )
public class AdaptedIfaceImpl implements AdaptedIface {

  private String member;

  @Override
  public String getMember() {
    return member;
  }

  public void setMember(String member) {
    this.member = member;
  }
}
