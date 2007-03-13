package org.codehaus.enunciate.modules.xfire;

import java.util.Comparator;

/**
 * Compares two handler interceptors.
 *
 * @author Ryan Heaton
 */
public class EnunciateHandlerInterceptorComparator implements Comparator<EnunciateHandlerInterceptor> {

  public static final EnunciateHandlerInterceptorComparator INSTANCE = new EnunciateHandlerInterceptorComparator();

  /**
   * Compares the two interceptors by {@link org.springframework.core.Ordered#getOrder() order}.
   *
   * @param o1 The first interceptor.
   * @param o2 The second interceptor.
   * @return The comparison.
   */
  public int compare(EnunciateHandlerInterceptor o1, EnunciateHandlerInterceptor o2) {
    return o1.getOrder() - o2.getOrder();
  }
}
