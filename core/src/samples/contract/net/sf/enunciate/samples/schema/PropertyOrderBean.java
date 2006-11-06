package net.sf.enunciate.samples.schema;

import javax.xml.namespace.QName;
import java.util.UUID;

/**
 * @author Ryan Heaton
 */
public class PropertyOrderBean {

  private int propertyA;
  private QName propertyB;
  private UUID propertyC;
  private String propertyD;
  private Boolean propertyE;

  public QName getPropertyB() {
    return propertyB;
  }

  public void setPropertyB(QName propertyB) {
    this.propertyB = propertyB;
  }

  public int getPropertyA() {
    return propertyA;
  }

  public void setPropertyA(int propertyA) {
    this.propertyA = propertyA;
  }

  public String getPropertyD() {
    return propertyD;
  }

  public void setPropertyD(String propertyD) {
    this.propertyD = propertyD;
  }

  public Boolean getPropertyE() {
    return propertyE;
  }

  public void setPropertyE(Boolean propertyE) {
    this.propertyE = propertyE;
  }

  public UUID getPropertyC() {
    return propertyC;
  }

  public void setPropertyC(UUID propertyC) {
    this.propertyC = propertyC;
  }

}
