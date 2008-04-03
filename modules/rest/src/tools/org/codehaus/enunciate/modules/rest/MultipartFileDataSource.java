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

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Ryan Heaton
 */
public class MultipartFileDataSource extends RESTRequestDataSource {

  private final MultipartFile multipartFile;

  public MultipartFileDataSource(MultipartFile multipartFile) {
    super(null, multipartFile.getName());
    this.multipartFile = multipartFile;
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

  public String getOriginalFilename() {
    return multipartFile.getOriginalFilename();
  }
}
