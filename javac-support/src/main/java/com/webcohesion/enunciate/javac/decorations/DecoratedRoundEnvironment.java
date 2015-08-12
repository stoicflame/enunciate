package com.webcohesion.enunciate.javac.decorations;

import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class DecoratedRoundEnvironment implements RoundEnvironment {

  private final RoundEnvironment delegate;
  private final DecoratedProcessingEnvironment env;

  public DecoratedRoundEnvironment(RoundEnvironment delegate, DecoratedProcessingEnvironment env) {
    this.delegate = delegate;
    this.env = env;
  }

  @Override
  public boolean processingOver() {
    return this.delegate.processingOver();
  }

  @Override
  public boolean errorRaised() {
    return this.delegate.errorRaised();
  }

  @Override
  public Set<? extends Element> getRootElements() {
    Set<Element> decorated = new HashSet<Element>();
    for (Element element : this.delegate.getRootElements()) {
      decorated.add(ElementDecorator.decorate(element, this.env));
    }
    return decorated;
  }

  @Override
  public Set<? extends Element> getElementsAnnotatedWith(TypeElement a) {
    while (a instanceof DecoratedTypeElement) {
      a = ((DecoratedTypeElement) a).getDelegate();
    }

    Set<Element> decorated = new HashSet<Element>();
    for (Element element : this.delegate.getElementsAnnotatedWith(a)) {
      decorated.add(ElementDecorator.decorate(element, this.env));
    }
    return decorated;
  }

  @Override
  public Set<? extends Element> getElementsAnnotatedWith(Class<? extends Annotation> a) {
    Set<Element> decorated = new HashSet<Element>();
    for (Element element : this.delegate.getElementsAnnotatedWith(a)) {
      decorated.add(ElementDecorator.decorate(element, this.env));
    }
    return decorated;
  }
}
