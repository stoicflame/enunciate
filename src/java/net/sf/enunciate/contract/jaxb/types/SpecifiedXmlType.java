package net.sf.enunciate.contract.jaxb.types;

import com.sun.mirror.util.TypeVisitor;

import javax.xml.bind.annotation.XmlSchemaType;

/**
 * An xml type that has been specified.
 *
 * @author Ryan Heaton
 */
public class SpecifiedXmlType implements XmlTypeMirror {

  private final XmlSchemaType annotation;

  public SpecifiedXmlType(XmlSchemaType annotation) {
    this.annotation = annotation;
  }

  /**
   * The specified name.
   *
   * @return The specified name.
   */
  public String getName() {
    return annotation.name();
  }

  /**
   * The specified namespace.
   *
   * @return The specified namespace.
   */
  public String getNamespace() {
    return annotation.namespace();
  }

  /**
   * A specified type is never anonymous.
   *
   * @return A specified type is never anonymous.
   */
  public boolean isAnonymous() {
    return false;
  }

  public void accept(TypeVisitor typeVisitor) {
    typeVisitor.visitTypeMirror(this);
  }
}
