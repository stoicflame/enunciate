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

package org.codehaus.enunciate.modules.rest;

import org.codehaus.enunciate.rest.annotations.VerbType;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.multipart.MultipartResolver;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 * Single-instance factory for an instance of org.springframework.web.multipart.commons.CommonsMultipartResolver if Commons Fileupload isn't on the classpath,
 * then no resolver will be returned by this factory.
 *
 * @author Ryan Heaton
 */
public class CommonsMultipartResolverFactory implements MultipartResolverFactory, ServletContextAware {

  private final MultipartResolver resolver;

  public CommonsMultipartResolverFactory() {
    MultipartResolver resolver;
    try {
      resolver = (MultipartResolver) Class.forName("org.springframework.web.multipart.commons.CommonsMultipartResolver").newInstance();
    }
    catch (Throwable e) {
      resolver = null;
    }

    this.resolver = resolver;
  }

  public void setServletContext(ServletContext servletContext) {
    if (resolver != null) {
      ((ServletContextAware) resolver).setServletContext(servletContext);
    }
  }

  public boolean isMultipart(HttpServletRequest request) {
    return resolver != null && resolver.isMultipart(request);
  }

  public MultipartResolver getMultipartResolver(String nounContext, String noun, VerbType verb) {
    return resolver;
  }
}
