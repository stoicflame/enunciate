package org.codehaus.enunciate.jboss;

import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpRequestPreprocessor;

import javax.ws.rs.core.MediaType;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class PathBasedConnegHttpPreprocessor implements HttpRequestPreprocessor {

  private Map<String,MediaType> mediaTypeMappings;

  public PathBasedConnegHttpPreprocessor(Map<String,MediaType> mediaTypeMappings) {
    this.mediaTypeMappings = mediaTypeMappings;
  }

  public void preProcess(HttpRequest request) {
    String preprocessedPath = request.getPreprocessedPath();
    for (Map.Entry<String, MediaType> mediaType : mediaTypeMappings.entrySet()) {
      int mediaTypeMappingIndex = preprocessedPath.indexOf("/" + mediaType.getKey() + "/");
      if (mediaTypeMappingIndex >= 0) {
        preprocessedPath = preprocessedPath.substring(mediaTypeMappingIndex + mediaType.getKey().length() + 1);
        request.setPreprocessedPath(preprocessedPath);
        request.getHttpHeaders().getAcceptableMediaTypes().add(0, mediaType.getValue());
        break;
      }
    }
  }
}
