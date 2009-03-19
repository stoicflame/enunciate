package net.java.ws.bookstore.domain;

/**
 * Utility class for equals and hashcode functions.
 * 
 * @author Ross McDonald
 * 
 */
public final class ObjectIdentifier {
  
  public ObjectIdentifier() {
    
  }

  /**
   * Compares two object by using lhs.equals(rhs). Safe to use with
   * null objects.
   * 
   * @param lhs
   * @param rhs
   * @return true if lhs equals rhs
   */
  public static boolean equal(Object lhs, Object rhs) {
    if (lhs == rhs) {
      return true;
    }
    if (lhs == null) {
      return false;
    }
    return lhs.equals(rhs);
  }

  /**
   * Returns hashcode of the object by calling obj.hashcode(). Safe
   * to use when obj is null.
   * 
   * @param obj
   * @return hashcode of the object or 0 if obj is null
   */
  public static int hashCode(final Object... obj) {
    int result = 37;
    for (Object o : obj) {
      result = 37 * result + (o != null ? o.hashCode() : 0);
    }
    return result;
  }
}
