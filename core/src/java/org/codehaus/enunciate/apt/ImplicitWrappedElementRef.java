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

package org.codehaus.enunciate.apt;

import org.codehaus.enunciate.contract.jaxb.ImplicitRootElement;
import org.codehaus.enunciate.contract.jaxb.ImplicitChildElement;
import org.codehaus.enunciate.contract.jaxb.Element;

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

  public Collection<ImplicitChildElement> getChildElements() {
    return Arrays.asList((ImplicitChildElement) new ImplicitChildElementRef(this.element));
  }

  public String getElementName() {
    return this.element.getWrapperName();
  }

  public String getElementDocs() {
    return element.getJavaDoc() != null ? element.getJavaDoc().toString() : null;
  }

  public QName getTypeQName() {
    return null;
  }
}