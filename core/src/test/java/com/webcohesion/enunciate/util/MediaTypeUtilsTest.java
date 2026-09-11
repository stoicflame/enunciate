package com.webcohesion.enunciate.util;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MediaTypeUtilsTest {
  private static final TypeSafeMatcher<List<String>> IS_JSON_COMPATIBLE = new TypeSafeMatcher<>() {
    @Override
    protected boolean matchesSafely(List<String> item) {
      return MediaTypeUtils.isJsonCompatible(item);
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("json compatible datatype");
    }
  };

  @Test
  public void empty() {
    assertThat(Collections.<String>emptyList(), not(IS_JSON_COMPATIBLE));
  }

  @Test
  public void wildcard() {
    assertThat(Collections.singletonList("*/*"), IS_JSON_COMPATIBLE);
    assertThat(Collections.singletonList("text/*"), IS_JSON_COMPATIBLE);
    assertThat(Collections.singletonList("application/*"), IS_JSON_COMPATIBLE);
  }

  @Test
  public void json() {
    assertThat(Collections.singletonList("application/json"), IS_JSON_COMPATIBLE);
  }

  @Test
  public void charset() {
    assertThat(Collections.singletonList("application/json;charset=UTF-8"), IS_JSON_COMPATIBLE);
    assertThat(Collections.singletonList("application/json ; charset=UTF-8"), IS_JSON_COMPATIBLE);
  }

  @Test
  public void binaryApplicationSubtypes() {
    assertTrue(MediaTypeUtils.isBinary("application/octet-stream"));
    assertTrue(MediaTypeUtils.isBinary("application/pdf"));
    assertTrue(MediaTypeUtils.isBinary("application/zip"));
    assertTrue(MediaTypeUtils.isBinary("application/wasm"));
    assertTrue(MediaTypeUtils.isBinary("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
    assertFalse(MediaTypeUtils.isBinary("application/json"));
    assertFalse(MediaTypeUtils.isBinary("application/xml"));
    assertFalse(MediaTypeUtils.isBinary("application/x-www-form-urlencoded"));
  }

  @Test
  public void binaryTopLevelTypes() {
    assertTrue(MediaTypeUtils.isBinary("image/png"));
    assertTrue(MediaTypeUtils.isBinary("audio/mpeg"));
    assertTrue(MediaTypeUtils.isBinary("video/mp4"));
    assertTrue(MediaTypeUtils.isBinary("font/woff2"));
    assertFalse(MediaTypeUtils.isBinary("text/plain"));
    assertFalse(MediaTypeUtils.isBinary("text/csv"));
    assertFalse(MediaTypeUtils.isBinary("multipart/form-data"));
  }

  @Test
  public void binaryStructuredSuffixes() {
    //a structured syntax suffix wins over the media type that carries it.
    assertFalse(MediaTypeUtils.isBinary("image/svg+xml"));
    assertFalse(MediaTypeUtils.isBinary("application/vnd.acme.thing+json"));
    assertFalse(MediaTypeUtils.isBinary("application/problem+xml"));
  }

  @Test
  public void binaryUnknownTypes() {
    //nothing is assumed about unrecognized media types; they can be declared binary via configuration.
    assertFalse(MediaTypeUtils.isBinary("application/vnd.acme.thing"));
    assertFalse(MediaTypeUtils.isBinary("application/x-acme"));
  }

  @Test
  public void binaryWildcards() {
    assertFalse(MediaTypeUtils.isBinary("*/*"));
    assertFalse(MediaTypeUtils.isBinary("application/*"));
    assertFalse(MediaTypeUtils.isBinary("image/*"));
  }

  @Test
  public void binaryMalformed() {
    assertFalse(MediaTypeUtils.isBinary(null));
    assertFalse(MediaTypeUtils.isBinary(""));
    assertFalse(MediaTypeUtils.isBinary("  "));
    assertFalse(MediaTypeUtils.isBinary("application"));
    assertFalse(MediaTypeUtils.isBinary("application/"));
  }

  @Test
  public void binaryParamsAndCase() {
    assertTrue(MediaTypeUtils.isBinary("application/octet-stream;charset=UTF-8"));
    assertTrue(MediaTypeUtils.isBinary("Application/Octet-Stream"));
    assertTrue(MediaTypeUtils.isBinary(" application/pdf ; q=0.5 "));
  }

  @Test
  public void normalization() {
    assertEquals("application/pdf", MediaTypeUtils.normalize(" Application/PDF ; q=0.5 "));
  }
}
