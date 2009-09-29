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
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxb.ElementDeclaration;
import org.codehaus.enunciate.contract.json.JsonTypeDefinition;

/**
 * @author Ryan Heaton
 */
public class ResourcePayloadTypeAdapter implements RESTResourcePayload {

  private final DecoratedTypeMirror delegate;

  public ResourcePayloadTypeAdapter(DecoratedTypeMirror delegate) {
    this.delegate = delegate;
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

  public JsonTypeDefinition getJsonType() {
    // TODO Implement me?
    return null;
  }
}
