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

package org.codehaus.enunciate.modules.jersey;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.spi.MessageBodyWorkers;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseWriter;
import com.sun.jersey.spi.container.WebApplication;

import java.io.IOException;

/**
 * An enunciate-supported web application.  For now, it just handles the request with a
 * {@link org.codehaus.enunciate.modules.jersey.StatusMessageResponseWriter}.
 *
 * @author Ryan Heaton
 */
public class EnunciateWebApplication implements WebApplication {

  private final WebApplication delegate;

  public EnunciateWebApplication(WebApplication delegate) {
    this.delegate = delegate;
  }

  public void initiate(ResourceConfig resourceConfig) throws IllegalArgumentException, ContainerException {
    delegate.initiate(resourceConfig);
  }

  public boolean isInitiated() {
    return delegate.isInitiated();
  }

  public void initiate(ResourceConfig resourceConfig, IoCComponentProviderFactory provider) throws IllegalArgumentException, ContainerException {
    delegate.initiate(resourceConfig, provider);
  }

  public void destroy() {
    delegate.destroy();
  }

  public WebApplication clone() {
    return new EnunciateWebApplication(delegate.clone());
  }

  public MessageBodyWorkers getMessageBodyWorkers() {
    return delegate.getMessageBodyWorkers();
  }

  public HttpContext getThreadLocalHttpContext() {
    return delegate.getThreadLocalHttpContext();
  }

  public void handleRequest(ContainerRequest request, ContainerResponseWriter responseWriter) throws IOException {
    delegate.handleRequest(request, new StatusMessageResponseWriter(responseWriter));
  }

  public void handleRequest(ContainerRequest request, ContainerResponse response) throws IOException {
    delegate.handleRequest(request, response);
  }
}
