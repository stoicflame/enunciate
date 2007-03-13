package org.codehaus.enunciate.modules.xfire_client;

import javax.xml.namespace.QName;
import java.util.TreeSet;

/**
 * @author Ryan Heaton
 */
public class GeneratedWrapperBeanExample implements GeneratedWrapperBean {

  private double simple;
  private String[] strings;
  private TreeSet integers;

  public double getSimple() {
    return simple;
  }

  public void setSimple(double simple) {
    this.simple = simple;
  }

  public String[] getStrings() {
    return strings;
  }

  public void setStrings(String[] strings) {
    this.strings = strings;
  }

  public void addToStrings(String item) {
    if (this.strings == null) {
      this.strings = new String[1];
    }
    else {
      Object[] oldArray = this.strings;
      this.strings = new String[this.strings.length + 1];
      System.arraycopy(oldArray, 0, this.strings, 0, oldArray.length);
    }

    this.strings[this.strings.length - 1] = item;
  }

  public TreeSet getIntegers() {
    return integers;
  }

  public void setIntegers(TreeSet integers) {
    this.integers = integers;
  }

  public void addToIntegers(Integer i) {
    if (this.integers == null) {
      this.integers = (TreeSet) ListParser.newCollectionInstance(TreeSet.class);
    }

    this.integers.add(i);
  }

  public QName getWrapperQName() {
    return new QName("urn:generated-wrapper-bean-example", "bean");
  }
}
