package org.codehaus.enunciate.samples.genealogy.exceptions;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author Ryan Heaton
 */
@XmlRootElement
public class EisFault implements Serializable {


  private static final long serialVersionUID = 2499375180709729151L;


  private String faultDetail;


  public void setFaultDetail(String faultDetail) {

    this.faultDetail = faultDetail;

  }


  public String getFaultDetail() {

    return faultDetail;

  }

}