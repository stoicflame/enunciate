/*
 * Copyright 2006-2008 Web Cohesion
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

package org.codehaus.enunciate.modules.spring_app;

import javax.servlet.*;
import java.io.IOException;

/**
 * A no-op filter that just invokes the next filter chain.
 *
 * @author Ryan Heaton
 */
public class NoOpFilter implements Filter {

  public void init(FilterConfig filterConfig) throws ServletException {
    //no-op.
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
    filterChain.doFilter(request, response);
  }

  public void destroy() {
    //no-op.
  }
}
