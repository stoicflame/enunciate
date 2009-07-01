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

package org.codehaus.enunciate.contract.jaxb;

import com.sun.mirror.declaration.MemberDeclaration;

/**
 * An accessor that is marshalled in xml to an xml value.
 *
 * @author Ryan Heaton
 */
public class Value extends Accessor {

  public Value(MemberDeclaration delegate, TypeDefinition typedef) {
    super(delegate, typedef);
  }

  /**
   * There's no name of a value accessor
   *
   * @return null.
   */
  public String getName() {
    return null;
  }

  /**
   * The target namespace of the value.
   *
   * @return The target namespace of the value.
   */
  public String getNamespace() {
    return getTypeDefinition().getNamespace();
  }

  @Override
  public boolean isValue() {
    return true;
  }

  public void generateExampleXml(org.jdom.Element parent) {
    parent.setContent(new org.jdom.Text("..."));
  }
}
