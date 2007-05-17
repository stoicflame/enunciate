/*
 * Copyright 2006 Web Cohesion
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

package org.codehaus.enunciate.modules.xfire_client;

import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.TypeMirror;
import freemarker.template.TemplateModelException;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;

import java.util.Map;

/**
 * Just
 *
 * @author Ryan Heaton
 */
public class CollectionTypeForMethod extends ClientClassnameForMethod {

  public CollectionTypeForMethod(Map<String, String> conversions) {
    super(conversions);
  }

  @Override
  protected String convert(TypeMirror typeMirror) throws TemplateModelException {
    DecoratedTypeMirror decoratedMirror = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(typeMirror);
    if (decoratedMirror.isCollection()) {
      return convert(((DeclaredType) decoratedMirror).getDeclaration().getQualifiedName());
    }
    else {
      throw new TemplateModelException(typeMirror + " isn't a collection!");
    }
  }

}
