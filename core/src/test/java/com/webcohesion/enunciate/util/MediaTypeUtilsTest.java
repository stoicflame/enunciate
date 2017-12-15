package com.webcohesion.enunciate.util;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class MediaTypeUtilsTest {
  private static final TypeSafeMatcher<List<String>> IS_JSON_COMPATIBLE = new TypeSafeMatcher<List<String>>() {
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
}
