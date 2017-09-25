package com.webcohesion.enunciate.api.datatype;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.List;

import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;

public class CustomSyntax implements Syntax {

  private final CustomMediaTypeDescriptor mediaType;

  public CustomSyntax(CustomMediaTypeDescriptor mediaType) {
    this.mediaType = mediaType;
  }

  @Override
  public String getId() {
    return mediaType.getMediaType().replace('/', '_').replace('+', '_');
  }

  @Override
  public String getSlug() {
    return getId();
  }

  @Override
  public String getLabel() {
    return this.mediaType.getMediaType();
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public List<Namespace> getNamespaces() {
    return Collections.emptyList();
  }

  @Override
  public boolean isAssignableToMediaType(String mediaType) {
    return this.mediaType.getMediaType().equals(mediaType);
  }

  @Override
  public MediaTypeDescriptor findMediaTypeDescriptor(String mediaType, DecoratedTypeMirror typeMirror) {
    return this.mediaType;
  }

  @Override
  public List<DataType> findDataTypes(String name) {
    return Collections.emptyList();
  }

  @Override
  public Example parseExample(Reader example) throws Exception {
    return new RawExample(example);
  }

  @Override
  public int compareTo(Syntax o) {
    return this.getSlug().compareTo(o.getSlug());
  }

  private class RawExample implements Example {

    private final String body;

    RawExample(Reader example) throws IOException {
      String body = "";
      int charVal;
      while ((charVal = example.read()) != -1) {
        body += (char) charVal;
      }
      example.close();
      this.body = body;
    }

    @Override
    public String getLang() {
      return "plain";
    }

    @Override
    public String getBody() {
      return this.body;
    }
  }
}
