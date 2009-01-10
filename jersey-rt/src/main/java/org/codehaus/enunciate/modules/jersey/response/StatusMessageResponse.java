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

package org.codehaus.enunciate.modules.jersey.response;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.MultivaluedMap;

/**
 * A response that includes a status message.
 *
 * @author Ryan Heaton
 */
public class StatusMessageResponse extends Response implements HasStatusMessage {

  private final String statusMessage;
  private final Response delegate;

  public StatusMessageResponse(Response delegate, String statusMessage) {
    this.delegate = delegate;
    this.statusMessage = statusMessage;
  }

  public Object getEntity() {
    return delegate.getEntity();
  }

  public int getStatus() {
    return delegate.getStatus();
  }

  public MultivaluedMap<String, Object> getMetadata() {
    return delegate.getMetadata();
  }

  /**
   * The status message.
   *
   * @return The status message.
   */
  public String getStatusMessage() {
    return this.statusMessage;
  }

}
