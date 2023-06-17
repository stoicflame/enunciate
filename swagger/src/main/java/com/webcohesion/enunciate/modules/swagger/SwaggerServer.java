/*
 * © 2023 by Intellectual Reserve, Inc. All rights reserved.
 */
package com.webcohesion.enunciate.modules.swagger;

public class SwaggerServer {
  
  private String url;
  private String description;

  public SwaggerServer() {
  }

  public SwaggerServer(String url, String description) {
    this.url = url;
    this.description = description;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
