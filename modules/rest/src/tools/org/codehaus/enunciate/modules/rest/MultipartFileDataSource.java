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
