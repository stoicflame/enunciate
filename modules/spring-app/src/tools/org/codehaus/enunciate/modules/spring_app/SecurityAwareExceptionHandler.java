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

import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.security.AccessDeniedException;
import org.springframework.security.AuthenticationException;
import org.springframework.core.Ordered;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Exception handler that's aware of how to deal with security exceptions.
 *
 * @author Ryan Heaton
 */
public class SecurityAwareExceptionHandler implements HandlerExceptionResolver, Ordered {

  public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, final Exception ex) {
    if ((ex instanceof AccessDeniedException) || (ex instanceof AuthenticationException)) {
      return new ModelAndView(new View() {
        public String getContentType() {
          return null;
        }

        public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
          throw ex;
        }
      });
    }

    return null;
  }

  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }
}
