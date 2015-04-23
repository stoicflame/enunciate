package com.webcohesion.enunciate.javac.decorations;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Locale;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class DecoratedProcessingEnvironment implements ProcessingEnvironment {

  private final ProcessingEnvironment delegate;

  public DecoratedProcessingEnvironment(ProcessingEnvironment delegate) {
    this.delegate = delegate;
  }

  @Override
  public Map<String, String> getOptions() {
    return delegate.getOptions();
  }

  @Override
  public Messager getMessager() {
    return delegate.getMessager();
  }

  @Override
  public Filer getFiler() {
    return delegate.getFiler();
  }

  @Override
  public Elements getElementUtils() {
    return new DecoratedElements(delegate.getElementUtils());
  }

  @Override
  public Types getTypeUtils() {
    return delegate.getTypeUtils();
  }

  @Override
  public SourceVersion getSourceVersion() {
    return delegate.getSourceVersion();
  }

  @Override
  public Locale getLocale() {
    return delegate.getLocale();
  }
}
