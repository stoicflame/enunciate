/*
 * Copyright 2006 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
