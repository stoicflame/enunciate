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

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Ryan Heaton
 */
public class MultipartFileDataSource extends RESTRequestDataSource {

  private final ContentTypeSupport contentTypeSupport;
  private final MultipartFile multipartFile;
  private final HttpServletRequest request;

  public MultipartFileDataSource(MultipartFile multipartFile) {
    super(null, multipartFile.getOriginalFilename());
    this.multipartFile = multipartFile;
    this.contentTypeSupport = null;
    this.request = null;
  }

  public MultipartFileDataSource(MultipartFile multipartFile, ContentTypeSupport contentTypeSupport, HttpServletRequest request) {
    super(null, multipartFile.getOriginalFilename());
    this.multipartFile = multipartFile;
    this.contentTypeSupport = contentTypeSupport;
    this.request = request;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return multipartFile.getInputStream();
  }

  @Override
  public String getContentType() {
    return multipartFile.getContentType();
  }

  @Override
  public long getSize() {
    return multipartFile.getSize();
  }

  /**
   * Unmarshal this data source based on its content type.
   *
   * @return The unmarshalled data source.
   */
  public Object unmarshal() throws Exception {
    return unmarshalAs(getContentType());
  }

  /**
   * Unmarshal this data source as the specified content type.
   *
   * @param contentType The content type.
   * @return The unmarshalled object.
   */
  public Object unmarshalAs(String contentType) throws Exception {
    if (getContentTypeSupport() == null) {
      throw new UnsupportedOperationException("Cannot unmarshal this data source: no support for content type " + contentType + ".");
    }

    if (getRequest() == null) {
      throw new UnsupportedOperationException("Cannot unmarshal this data source: no reference to the original request.");
    }

    RESTRequestContentTypeHandler handler = getContentTypeSupport().lookupHandlerByContentType(contentType);
    if (handler == null) {
      throw new UnsupportedOperationException("Cannot unmarshal this data source: no content type handler found for content type " + contentType + ".");
    }
    return handler.read(getRequest());
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
   * The request from which this file was parsed.
   *
   * @return The request from which this file was parsed.
   */
  public HttpServletRequest getRequest() {
    return request;
  }
}
