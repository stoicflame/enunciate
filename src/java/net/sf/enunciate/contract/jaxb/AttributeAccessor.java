package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.declaration.FieldDeclaration;
import net.sf.jelly.apt.decorations.declaration.PropertyDeclaration;

/**
 * An accessor that is marshalled in xml to an xml attribute.
 *
 * @author Ryan Heaton
 */
public class AttributeAccessor extends Accessor {

  public AttributeAccessor(FieldDeclaration delegate) {
    super(delegate);
  }

  public AttributeAccessor(PropertyDeclaration delegate) {
    super(delegate);
  }

}
