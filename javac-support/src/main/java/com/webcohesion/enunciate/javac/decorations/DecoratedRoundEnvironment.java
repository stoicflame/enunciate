/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
