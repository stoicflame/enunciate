package org.codehaus.enunciate.samples.schema;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.Date;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Ryan Heaton
 */
@XmlRootElement (
  name = "element-bean-one",
  namespace = "urn:element-bean-one"
)
@XmlType (
  name=""
)
public class ElementBeanOne {

  private EnumBeanOne property1;
  private BeanTwo property2;
  private int property3;
  private String property4;
  private Object property5;
  private byte[] property6;
  private ElementBeanOne loopingProperty1;
  private Collection<Object> elementsProperty1;
  private Collection<Date> elementsProperty2;
  private Collection<Object> elementsProperty3;
  private Collection<BigDecimal> wrappedElementsProperty1;
  private Collection<BigInteger> wrappedElementsProperty2;

  public EnumBeanOne getProperty1() {
    return property1;
  }

  public void setProperty1(EnumBeanOne property1) {
    this.property1 = property1;
  }

  public BeanTwo getProperty2() {
    return property2;
  }

  public void setProperty2(BeanTwo property2) {
    this.property2 = property2;
  }

  @XmlElement (
    name = "changedname",
    nillable = true,
    required = true,
    namespace = "urn:changedname",
    defaultValue = "6"
  )
  public int getProperty3() {
    return property3;
  }

  public void setProperty3(int property3) {
    this.property3 = property3;
  }

  public String getProperty4() {
    return property4;
  }

  public void setProperty4(String property4) {
    this.property4 = property4;
  }

  @XmlElement (
    type = EnumBeanOne.class
  )
  public Object getProperty5() {
    return property5;
  }

  public void setProperty5(Object property5) {
    this.property5 = property5;
  }

  public byte[] getProperty6() {
    return property6;
  }

  public void setProperty6(byte[] property6) {
    this.property6 = property6;
  }

  public ElementBeanOne getLoopingProperty1() {
    return loopingProperty1;
  }

  public void setLoopingProperty1(ElementBeanOne loopingProperty1) {
    this.loopingProperty1 = loopingProperty1;
  }

  public Collection<Object> getElementsProperty1() {
    return elementsProperty1;
  }

  @XmlElements (
    value = {@XmlElement ( name = "item", namespace = "urn:item" )}
  )
  public void setElementsProperty1(Collection<Object> elementsProperty1) {
    this.elementsProperty1 = elementsProperty1;
  }

  public Collection<Date> getElementsProperty2() {
    return elementsProperty2;
  }

  public void setElementsProperty2(Collection<Date> elementsProperty2) {
    this.elementsProperty2 = elementsProperty2;
  }

  @XmlElements (
    value = {
      @XmlElement(
        name="durationItem",
        namespace="urn:durationItem",
        type=javax.xml.datatype.Duration.class
      ),
      @XmlElement(
        name="imageItem",
        namespace="urn:imageItem",
        type=java.awt.Image.class
      )
    }
  )
  public Collection<Object> getElementsProperty3() {
    return elementsProperty3;
  }

  public void setElementsProperty3(Collection<Object> elementsProperty3) {
    this.elementsProperty3 = elementsProperty3;
  }

  @XmlElementWrapper (
    name = "wrapper1",
    namespace = "urn:wrapper1",
    nillable = true
  )
  public Collection<BigDecimal> getWrappedElementsProperty1() {
    return wrappedElementsProperty1;
  }

  public void setWrappedElementsProperty1(Collection<BigDecimal> wrappedElementsProperty1) {
    this.wrappedElementsProperty1 = wrappedElementsProperty1;
  }

  @XmlElementWrapper
  public Collection<BigInteger> getWrappedElementsProperty2() {
    return wrappedElementsProperty2;
  }

  public void setWrappedElementsProperty2(Collection<BigInteger> wrappedElementsProperty2) {
    this.wrappedElementsProperty2 = wrappedElementsProperty2;
  }
}
