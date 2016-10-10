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
package com.webcohesion.enunciate.javac.decorations.element;

import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.ElementDecorator;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class DecoratedTypeParameterElement extends DecoratedElement<TypeParameterElement> implements TypeParameterElement {

  private Element genericElement;
  private List<? extends TypeMirror> bounds;

  public DecoratedTypeParameterElement(TypeParameterElement delegate, DecoratedProcessingEnvironment env) {
    super(delegate, env);
  }

  @Override
  public Element getGenericElement() {
    if (this.genericElement == null) {
      this.genericElement = ElementDecorator.decorate(this.delegate.getGenericElement(), this.env);
    }

    return this.genericElement;
  }

  @Override
  public List<? extends TypeMirror> getBounds() {
    if (this.bounds == null) {
      this.bounds = TypeMirrorDecorator.decorate(this.delegate.getBounds(), env);
    }

    return this.bounds;
  }

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitTypeParameter(this, p);
  }
}
