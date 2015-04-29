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

package com.webcohesion.enunciate.modules.jaxb.model;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Arrays;

/**
 * An implicit wrapped element reference.
 *
 * @author Ryan Heaton
 */
public class ImplicitWrappedElementRef implements ImplicitRootElement {

  private final Element element;

  public ImplicitWrappedElementRef(Element element) {
    this.element = element;
  }

  @Override
  public Collection<ImplicitChildElement> getChildElements() {
    return Arrays.asList((ImplicitChildElement) new ImplicitChildElementRef(this.element));
  }

  @Override
  public String getElementName() {
    return this.element.getWrapperName();
  }

  @Override
  public String getTargetNamespace() {
    return this.element.getWrapperNamespace();
  }

  @Override
  public String getElementDocs() {
    return element.getJavaDoc() != null ? element.getJavaDoc().toString() : null;
  }

  @Override
  public QName getTypeQName() {
    return null;
  }

}