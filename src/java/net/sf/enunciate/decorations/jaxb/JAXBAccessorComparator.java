package net.sf.enunciate.decorations.jaxb;

import javax.xml.bind.annotation.AccessorOrder;
import java.util.Comparator;

/**
 * A comparator for JAXB accessors.
 *
 * @author Ryan Heaton
 */
public class JAXBAccessorComparator implements Comparator<JAXBAccessorDeclaration> {

  private final AccessorOrder accessorOrder;
  private final String[] propOrder;

  /**
   * Instantiate a new comparator, given the sorting parameters.
   *
   * @param propOrder The property order, or null if none is specified.
   * @param order The accessor order.
   */
  public JAXBAccessorComparator(String[] propOrder, AccessorOrder order) {
    this.accessorOrder = order;
    this.propOrder = propOrder;
  }

  public int compare(JAXBAccessorDeclaration accessor1, JAXBAccessorDeclaration accessor2) {
    String propertyName1 = accessor1.getPropertyName();
    String propertyName2 = accessor2.getPropertyName();

    if (this.propOrder != null) {
      //apply the specified property order
      int propertyIndex1 = find(this.propOrder, propertyName1);
      int propertyIndex2 = find(this.propOrder, propertyName2);

      if (propertyIndex1 < 0) {
        throw new IllegalStateException(accessor1.getPosition() + ": property '" + propertyName1 + "' isn't included in the specified property order.");
      }
      if (propertyIndex2 < 0) {
        throw new IllegalStateException(accessor1.getPosition() + ": property '" + propertyName2 + "' isn't included in the specified property order.");
      }

      return propertyIndex1 - propertyIndex2;
    }
    else if (accessorOrder == AccessorOrder.ALPHABETICAL) {
      return propertyName1.compareTo(propertyName2);
    }

    //if no order is specified, it's undefined.
    return propertyName1.hashCode() - propertyName2.hashCode();
  }

  /**
   * Finds the order index of the specified property.
   * @param propOrder The property order.
   * @param propertyName The property name.
   * @return The order index of the specified property, or -1 if not found.
   */
  protected int find(String[] propOrder, String propertyName) {
    for (int i = 0; i < propOrder.length; i++) {
      if (propOrder[i].equals(propertyName)) {
        return i;
      }
    }

    return -1;
  }
}
