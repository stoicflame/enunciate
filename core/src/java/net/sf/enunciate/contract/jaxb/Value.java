package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.declaration.MemberDeclaration;

/**
 * An accessor that is marshalled in xml to an xml value.
 *
 * @author Ryan Heaton
 */
public class Value extends Accessor {

  public Value(MemberDeclaration delegate, TypeDefinition typedef) {
    super(delegate, typedef);
  }

  /**
   * There's no name of a value accessor
   *
   * @return null.
   */
  public String getName() {
    return null;
  }

  /**
   * The target namespace of the value.
   *
   * @return The target namespace of the value.
   */
  public String getNamespace() {
    return getTypeDefinition().getTargetNamespace();
  }

}
