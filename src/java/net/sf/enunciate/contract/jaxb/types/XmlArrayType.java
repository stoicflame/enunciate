package net.sf.enunciate.contract.jaxb.types;

import com.sun.mirror.type.ArrayType;
import net.sf.jelly.apt.decorations.type.DecoratedArrayType;

/**
 * An xml array type.
 *
 * @author Ryan Heaton
 */
public class XmlArrayType extends DecoratedArrayType implements XmlTypeMirror {

  private final XmlTypeMirror componentType;

  public XmlArrayType(ArrayType delegate) throws XmlTypeException {
    super(delegate);

    try {
      componentType = XmlTypeDecorator.decorate(super.getComponentType());
    }
    catch (XmlTypeException e) {
      throw new XmlTypeException("Problem with the array component type: " + e.getMessage());
    }
  }

  /**
   * The name of the array type is the name of its component type.
   *
   * @return The name of the array type is the name of its component type.
   */
  public String getName() {
    return getComponentType().getName();
  }

  /**
   * The name of the array type is the name of its component type.
   *
   * @return The name of the array type is the name of its component type.
   */
  public String getNamespace() {
    return getComponentType().getNamespace();
  }

  /**
   * Whether the array type is anonymous depends on whether its component type is anonymous.
   *
   * @return Whether the array type is anonymous.
   */
  public boolean isAnonymous() {
    return getComponentType().isAnonymous();
  }

  // Inherited.
  @Override
  public XmlTypeMirror getComponentType() {
    return componentType;
  }

}
