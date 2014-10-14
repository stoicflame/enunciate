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

package org.codehaus.enunciate.contract.jaxrs;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.type.ClassType;

import com.sun.mirror.type.TypeMirror;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxb.ElementDeclaration;
import org.codehaus.enunciate.contract.json.JsonType;

/**
 * Metadata about the representations of a resource, as provided by a resource methods.
 *
 * @author Ryan Heaton
 */
public class ResourceRepresentationMetadata {

  private final TypeMirror delegate;
  private final String docValue;

  public ResourceRepresentationMetadata(DecoratedTypeMirror delegate) {
    this.delegate = delegate;
    this.docValue = delegate.getDocValue();
  }

  public ResourceRepresentationMetadata(TypeMirror delegate, String docValue) {
    this.delegate = delegate;
    this.docValue = docValue;
  }

  /**
   * The documentation.
   *
   * @return The documentation.
   */
  public String getDocValue() {
    return this.docValue;
  }

  /**
   * The element for the XML representation, if any.
   *
   * @return The element for the XML representation, if any.
   */
  public ElementDeclaration getXmlElement() {
    if (delegate instanceof ClassType) {
      ClassDeclaration declaration = ((ClassType) delegate).getDeclaration();
      if (declaration != null) {
        return ((EnunciateFreemarkerModel) FreemarkerModel.get()).findElementDeclaration(declaration);
      }
    }
    return null;
  }

  /**
   * The type for the JSON representation, if any.
   *
   * @return The type for the JSON representation, if any.
   */
  public JsonType getJsonType() {
    if (delegate instanceof ClassType) {
      ClassDeclaration declaration = ((ClassType) delegate).getDeclaration();
      if (declaration != null) {
        return ((EnunciateFreemarkerModel) FreemarkerModel.get()).findJsonTypeDefinition(declaration);
      }
    }
    return null;
  }

  public TypeMirror getDelegate() {
    return delegate;
  }
}
