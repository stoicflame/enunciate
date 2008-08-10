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

import org.springframework.web.context.ServletContextAware;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.MultipartException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.activation.DataHandler;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Default multipart request handler, uses an instance of Spring's MultipartResolver.
 *
 * @author Ryan Heaton
 */
public class DefaultMultipartRequestHandler implements MultipartRequestHandler, ServletContextAware {

  private final MultipartResolver resolver;
  private ContentTypeSupport contentTypeSupport;

  public DefaultMultipartRequestHandler() {
    this("org.springframework.web.multipart.commons.CommonsMultipartResolver");
  }

  public DefaultMultipartRequestHandler(String multipartResolverClass) {
    MultipartResolver resolver;
    try {
      resolver = (MultipartResolver) Class.forName(multipartResolverClass).newInstance();
    }
    catch (Throwable e) {
      resolver = null;
    }

    this.resolver = resolver;
  }

  public DefaultMultipartRequestHandler(MultipartResolver resolver) {
    this.resolver = resolver;
  }

  @Autowired
  public void setServletContext(ServletContext servletContext) {
    if ((resolver != null) && (resolver instanceof ServletContextAware)) {
      ((ServletContextAware) resolver).setServletContext(servletContext);
    }
  }

  /**
   * The resolver for this multipart request handler.
   *
   * @return The resolver for this multipart request handler.
   */
  public MultipartResolver getResolver() {
    return resolver;
  }

  /**
   * Whether the request is multipart, according to {@link #getResolver() the resolver}.
   *
   * @param request The request.
   * @return Whether the request is multipart.
   */
  public boolean isMultipart(HttpServletRequest request) {
    return resolver != null && resolver.isMultipart(request);
  }

  /**
   * Uses the resolver to resolve the multipart request.
   *
   * @param request The request.
   * @return The resolved request.
   */
  public HttpServletRequest handleMultipartRequest(HttpServletRequest request) throws MultipartException {
    return resolver != null ? resolver.resolveMultipart(request) : request;
  }

  /**
   * The content type support.
   *
   * @return The content type support.
   */
  public ContentTypeSupport getContentTypeSupport() {
    return contentTypeSupport;
  }

  /**
   * The content type support.
   *
   * @param contentTypeSupport The content type support.
   */
  @Autowired (required = false)
  public void setContentTypeSupport(ContentTypeSupport contentTypeSupport) {
    this.contentTypeSupport = contentTypeSupport;
  }

  /**
   * Parses the parts. Assuming the request is multipart, the parts will have a {@link javax.activation.DataHandler#getDataSource() DataSource}
   * that is an instance of {@link org.codehaus.enunciate.modules.rest.MultipartFileDataSource MultipartFileDataSource}.
   *
   * @param request The request.
   * @return The parts.
   */
  public Collection<DataHandler> parseParts(HttpServletRequest request) {
    ArrayList<DataHandler> dataHandlers = new ArrayList<DataHandler>();
    if (request instanceof MultipartHttpServletRequest) {
      Collection<MultipartFile> multipartFiles = (Collection<MultipartFile>) ((MultipartHttpServletRequest) request).getFileMap().values();
      for (MultipartFile multipartFile : multipartFiles) {
        MultipartFileDataSource dataSource = new MultipartFileDataSource(multipartFile, getContentTypeSupport(), request);

        dataHandlers.add(new DataHandler(dataSource));
      }
    }
    else {
      dataHandlers.add(new DataHandler(new RESTRequestDataSource(request, request.getRequestURL().toString())));
    }
    return dataHandlers;
  }
}
