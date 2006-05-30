package net.sf.enunciate.contract.jaxb;

import net.sf.enunciate.contract.ValidationException;

import javax.xml.bind.annotation.AccessorOrder;
import java.util.Comparator;

/**
 * A comparator for accessors.
 *
 * @author Ryan Heaton
 */
public class AccessorComparator implements Comparator<Accessor> {

  private final AccessorOrder accessorOrder;
  private final String[] propOrder;

  /**
   * Instantiate a new comparator, given the sorting parameters.
   *
   * @param propOrder The property order, or null if none is specified.
   * @param order     The accessor order.
   */
  public AccessorComparator(String[] propOrder, AccessorOrder order) {
    this.accessorOrder = order;
    this.propOrder = propOrder;
  }

  // Inherited.
  public int compare(Accessor accessor1, Accessor accessor2) {
    String propertyName1 = accessor1.getPropertyName();
    String propertyName2 = accessor2.getPropertyName();

    if (this.propOrder != null) {
      //apply the specified property order
      int propertyIndex1 = find(this.propOrder, propertyName1);
      int propertyIndex2 = find(this.propOrder, propertyName2);

      if (propertyIndex1 < 0) {
        throw new ValidationException(accessor1.getPosition() + ": property '" + propertyName1 + "' isn't included in the specified property order.");
      }
      if (propertyIndex2 < 0) {
        throw new ValidationException(accessor2.getPosition() + ": property '" + propertyName2 + "' isn't included in the specified property order.");
      }

      return propertyIndex1 - propertyIndex2;
    }
    else if (accessorOrder == AccessorOrder.ALPHABETICAL) {
      return propertyName1.compareTo(propertyName2);
    }

    //If no order is specified, it's undefined. We'll put it in source order.
    int comparison = accessor1.getPosition().line() - accessor2.getPosition().line();
    if (comparison == 0) {
      comparison = accessor1.getPosition().column() - accessor2.getPosition().column();
    }

    return comparison;
  }

  /**
   * Finds the order index of the specified property.
   *
   * @param propOrder    The property order.
   * @param propertyName The property name.
   * @return The order index of the specified property, or -1 if not found.
   */
  protected int find(String[] propOrder, String propertyName) {
    for (int i = 0; i < propOrder.length; i++) {
      if (propOrder[i].equalsIgnoreCase(propertyName)) {
        return i;
      }
    }

    return -1;
  }
}
