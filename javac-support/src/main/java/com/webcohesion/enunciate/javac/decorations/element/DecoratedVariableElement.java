/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.javac.decorations.element;

import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.javadoc.DocComment;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.javac.javadoc.JavaDocTagHandler;

import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.VariableElement;

/**
 * @author Ryan Heaton
 */
public class DecoratedVariableElement extends DecoratedElement<VariableElement> implements VariableElement {

  private DocComment docComment;

  public DecoratedVariableElement(VariableElement delegate, DecoratedProcessingEnvironment env) {
    super(delegate, env);
    if (delegate instanceof DecoratedVariableElement) {
      this.docComment = ((DecoratedVariableElement)delegate).docComment;
    }
  }

  @Override
  public Object getConstantValue() {
    return this.delegate.getConstantValue();
  }

  protected void setDocComment(DocComment docComment) {
    this.docComment = docComment;
  }

  //Inherited.
  public String getDocComment() {
    return this.docComment == null ? super.getDocComment() : this.docComment.get();
  }

  @Override
  protected JavaDoc getJavaDoc(JavaDocTagHandler tagHandler, boolean useDelegate) {
    return super.getJavaDoc(tagHandler, this.docComment == null);
  }

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitVariable(this, p);
  }
}
