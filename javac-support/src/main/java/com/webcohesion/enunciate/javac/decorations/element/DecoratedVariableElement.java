/*
 * Copyright 2006 Ryan Heaton
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
package com.webcohesion.enunciate.javac.decorations.element;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.VariableElement;

/**
 * @author Ryan Heaton
 */
public class DecoratedVariableElement extends DecoratedElement<VariableElement> implements VariableElement{

  public DecoratedVariableElement(VariableElement delegate, ProcessingEnvironment env) {
    super(delegate, env);
  }

  @Override
  public Object getConstantValue() {
    return this.delegate.getConstantValue();
  }

  void setDocComment(String docComment) {
    this.javaDoc.setValue(docComment);
  }

  //Inherited.
  public String getDocComment() {
    return String.valueOf(this.javaDoc);
  }

}
