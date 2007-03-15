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

import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;

/**
 * The EnunciateHandlerMapping is just a SimpleUrlHandlerMapping that extracts its interceptors from
 * the context, rather than sets them explicitly.  It's intended that there should be only one of
 * these per servlet context, as multiple instances will obtain the same list of interceptors.
 * 
 * @author Ryan Heaton
 */
public class EnunciateHandlerMapping extends SimpleUrlHandlerMapping {

  /**
   * Attempts to find any instances of {@link EnunciateHandlerMapping} in the context, orders
   * them, and sets them as interceptors for this handler mapping.
   *
   * @throws BeansException
   */
  @Override
  public void initApplicationContext() throws BeansException {
    loadInterceptors();
    super.initApplicationContext();
  }

  /**
   * Loads the interceptors found in the context.
   */
  protected void loadInterceptors() {
    ApplicationContext ctx = getApplicationContext();
    Map interceptorBeans = ctx.getBeansOfType(EnunciateHandlerInterceptor.class);
    if (interceptorBeans.size() > 0) {
      ArrayList<EnunciateHandlerInterceptor> interceptors = new ArrayList<EnunciateHandlerInterceptor>();
      interceptors.addAll(interceptorBeans.values());
      Collections.sort(interceptors, EnunciateHandlerInterceptorComparator.INSTANCE);
      setInterceptors(interceptors.toArray(new EnunciateHandlerInterceptor[interceptors.size()]));
    }
  }
}
