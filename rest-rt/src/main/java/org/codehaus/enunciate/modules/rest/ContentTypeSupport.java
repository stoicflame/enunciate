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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Ryan Heaton
 */
public class ContentTypeSupport {

  private final Map<String, String> contentTypesToIds;
  private final Map<String, RESTRequestContentTypeHandler> contentTypesToHandlers;
  private final Map<String, String> idsToContentTypes;
  private final Map<String, RESTRequestContentTypeHandler> idsToHandlers;

  public ContentTypeSupport(Map<String, String> contentTypesToIds, Map<String, RESTRequestContentTypeHandler> contentTypesToHandlers) {
    if (contentTypesToIds == null) {
      contentTypesToIds = new TreeMap<String, String>();
    }
    
    if (contentTypesToHandlers == null) {
      contentTypesToHandlers = new TreeMap<String, RESTRequestContentTypeHandler>();
    }

    this.contentTypesToIds = Collections.unmodifiableMap(contentTypesToIds);
    this.contentTypesToHandlers = Collections.unmodifiableMap(contentTypesToHandlers);

    Map<String, RESTRequestContentTypeHandler> idsToHandlers = new HashMap<String, RESTRequestContentTypeHandler>();
    Map<String, String> idsToContentTypes = new HashMap<String, String>();
    for (String contentType : contentTypesToIds.keySet()) {
      RESTRequestContentTypeHandler handler;
      try {
        handler = contentTypesToHandlers.get(contentType);
      }
      catch (ClassCastException e) {
        throw new IllegalArgumentException("Illegal content type handler for content type '" + contentType + "'.  (Must implement org.codehaus.enunciate.modules.rest.RESTRequestContentTypeHandler).");
      }

      if (handler == null) {
        throw new IllegalArgumentException("No handler specified for content type '" + contentType + "'");
      }

      idsToContentTypes.put(contentTypesToIds.get(contentType), contentType);
      idsToHandlers.put(contentTypesToIds.get(contentType), handler);
    }

    this.idsToContentTypes = Collections.unmodifiableMap(idsToContentTypes);
    this.idsToHandlers = Collections.unmodifiableMap(idsToHandlers);
  }

  /**
   * Lookup the content type for the specified id.
   *
   * @param id The id.
   * @return The content type.
   */
  public String lookupContentTypeById(String id) {
    return this.idsToContentTypes.get(id);
  }

  /**
   * Lookup the content type id for the specified content type.
   *
   * @param contentType The content type.
   * @return The content type id.
   */
  public String lookupIdByContentType(String contentType) {
    return this.contentTypesToIds.get(contentType);
  }

  /**
   * Lookup a content type handler by content type.
   *
   * @param contentType The content type.
   * @return The handler for the content type.
   */
  public RESTRequestContentTypeHandler lookupHandlerByContentType(String contentType) {

    what about wildcards?
    return this.contentTypesToHandlers.get(contentType);
  }

  /**
   * Lookup a content type handler by content type id.
   *
   * @param id The id of the content type.
   * @return The content type handler.
   */
  public RESTRequestContentTypeHandler lookupHandlerById(String id) {
    return this.idsToHandlers.get(id);
  }

}
