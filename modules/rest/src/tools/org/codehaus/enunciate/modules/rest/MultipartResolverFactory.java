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
import org.springframework.web.multipart.MultipartResolver;

import javax.servlet.http.HttpServletRequest;

/**
 * A factory for a multipart resolver.
 * 
 * @author Ryan Heaton
 */
public interface MultipartResolverFactory {

  /**
   * Whether the request represents a multipart request.
   *
   * @param request The request.
   * @return Whether the request represents a multipart request.
   */
  boolean isMultipart(HttpServletRequest request);

  /**
   * Get the multipart resolver for the given REST resource.
   *
   * @param nounContext The noun context of the rest resource.
   * @param noun The noun of the rest resource.
   * @param verb The verb for the operation for which the multipart resolver will be applied.
   * @return The multipart resolver.
   */
  MultipartResolver getMultipartResolver(String nounContext, String noun, VerbType verb);

}
