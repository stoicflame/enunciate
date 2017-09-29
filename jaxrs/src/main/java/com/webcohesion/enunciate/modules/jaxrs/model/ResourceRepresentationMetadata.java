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
package com.webcohesion.enunciate.modules.jaxrs.model;

import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.javadoc.DocComment;
import com.webcohesion.enunciate.javac.javadoc.JavaDocTagHandler;

import javax.lang.model.type.TypeMirror;

/**
 * Metadata about the representations of a resource, as provided by a resource methods.
 *
 * @author Ryan Heaton
 */
public class ResourceRepresentationMetadata {

  private final TypeMirror delegate;
  private final DocComment docComment;

  public ResourceRepresentationMetadata(DecoratedTypeMirror delegate) {
    this.delegate = delegate;
    this.docComment = delegate.getDeferredDocComment();
  }

  public ResourceRepresentationMetadata(TypeMirror delegate, DocComment docComment) {
    this.delegate = delegate;
    this.docComment = docComment;
  }

  /**
   * The documentation.
   *
   * @return The documentation.
   * @param tagHandler The tag handler.
   */
  public String getDocValue(JavaDocTagHandler tagHandler) {
    return this.docComment.get(tagHandler);
  }

  public TypeMirror getDelegate() {
    return delegate;
  }
}
