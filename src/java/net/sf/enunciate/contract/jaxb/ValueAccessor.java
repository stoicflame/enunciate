package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.declaration.FieldDeclaration;
import net.sf.jelly.apt.decorations.declaration.PropertyDeclaration;

/**
 * An accessor that is marshalled in xml to an xml value.
 *
 * @author Ryan Heaton
 */
public class ValueAccessor extends Accessor {

  public ValueAccessor(FieldDeclaration delegate) {
    super(delegate);
  }

  public ValueAccessor(PropertyDeclaration delegate) {
    super(delegate);
  }

  /**
   * There's no name of a value accessor
   *
   * @return null.
   */
  public String getAccessorName() {
    return null;
  }

}
