package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.declaration.MemberDeclaration;

/**
 * A method or field decorated so as to be able to describe how it will be bound to XML by JAXB.
 *
 * @author Ryan Heaton
 */
public interface Accessor extends MemberDeclaration {

  /**
   * The property name.
   *
   * @return The property name.
   */
  public abstract String getPropertyName();


}
