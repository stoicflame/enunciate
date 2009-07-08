/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.contract.common.rest;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.util.TypeVisitor;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxb.RootElementDeclaration;
import org.codehaus.enunciate.contract.jaxb.ElementDeclaration;

/**
 * @author Ryan Heaton
 */
public class ResourcePayloadTypeAdapter extends DecoratedTypeMirror implements RESTResourcePayload {

  private final DecoratedTypeMirror delegate;

  public ResourcePayloadTypeAdapter(DecoratedTypeMirror delegate) {
    super(delegate);
    this.delegate = delegate;
  }

  public void accept(TypeVisitor v) {
    delegate.accept(v);
  }

  public boolean equals(Object obj) {
    return delegate.equals(obj);
  }

  public String toString() {
    return delegate.toString();
  }

  public boolean isInstanceOf(String className) {
    return delegate.isInstanceOf(className);
  }

  public boolean isAnnotation() {
    return delegate.isAnnotation();
  }

  public boolean isArray() {
    return delegate.isArray();
  }

  public boolean isCollection() {
    return delegate.isCollection();
  }

  public boolean isClass() {
    return delegate.isClass();
  }

  public boolean isDeclared() {
    return delegate.isDeclared();
  }

  public boolean isEnum() {
    return delegate.isEnum();
  }

  public boolean isInterface() {
    return delegate.isInterface();
  }

  public boolean isPrimitive() {
    return delegate.isPrimitive();
  }

  public boolean isReferenceType() {
    return delegate.isReferenceType();
  }

  public boolean isTypeVariable() {
    return delegate.isTypeVariable();
  }

  public boolean isVoid() {
    return delegate.isVoid();
  }

  public boolean isWildcard() {
    return delegate.isWildcard();
  }

  public String getDocComment() {
    return delegate.getDocComment();
  }

  public void setDocComment(String docComment) {
    delegate.setDocComment(docComment);
  }

  public String getDocValue() {
    return delegate.getDocValue();
  }

  // Inherited.
  public ElementDeclaration getXmlElement() {
    if (delegate instanceof ClassType) {
      ClassDeclaration declaration = ((ClassType) delegate).getDeclaration();
      if (declaration != null) {
        return ((EnunciateFreemarkerModel) FreemarkerModel.get()).findElementDeclaration(declaration);
      }
    }
    return null;
  }
}
