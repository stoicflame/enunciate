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

package org.codehaus.enunciate.modules.gwt;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.*;
import freemarker.template.TemplateModelException;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;

import java.util.Collection;
import java.util.Map;

/**
 * Conversion from java types to C# types.
 *
 * @link http://livedocs.adobe.com/flex/2/docs/wwhelp/wwhimpl/common/html/wwhelp.htm?context=LiveDocs_Parts&file=00001104.html#270405
 * @author Ryan Heaton
 */
public class OverlayClientClassnameForMethod extends org.codehaus.enunciate.template.freemarker.ClientClassnameForMethod {

  public OverlayClientClassnameForMethod(Map<String, String> conversions) {
    super(conversions);
  }

  @Override
  public String convert(TypeDeclaration declaration) throws TemplateModelException {
    if (isCollection(declaration)) {
      return "com.google.gwt.core.client.JsArray";
    }

    return super.convert(declaration);
  }

  protected boolean isCollection(TypeDeclaration declaration) {
    String fqn = declaration.getQualifiedName();
    if (Collection.class.getName().equals(fqn)) {
      return true;
    }
    else if (Object.class.getName().equals(fqn)) {
      return false;
    }
    else {
      if (declaration instanceof ClassDeclaration) {
        DecoratedTypeMirror decorated = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(((ClassDeclaration)declaration).getSuperclass());
        if (decorated.isCollection()) {
          return true;
        }
      }

      for (InterfaceType interfaceType : declaration.getSuperinterfaces()) {
        DecoratedTypeMirror decorated = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(interfaceType);
        if (decorated.isCollection()) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public String convert(TypeMirror typeMirror) throws TemplateModelException {
    DecoratedTypeMirror decorated = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(typeMirror);
    if (decorated.isArray()) {
      DecoratedTypeMirror componentType = (DecoratedTypeMirror) ((ArrayType) decorated).getComponentType();
      if (componentType.isPrimitive()) {
        switch (((PrimitiveType)componentType).getKind()) {
          case BOOLEAN:
            return "com.google.gwt.core.client.JsArrayBoolean";
          case BYTE:
            return "java.lang.String";//byte arrays serialized as base64-encoded strings.
          case CHAR:
          case INT:
          case SHORT:
            return "com.google.gwt.core.client.JsArrayInteger";
          case DOUBLE:
          case FLOAT:
          case LONG:
            return "com.google.gwt.core.client.JsArrayNumber";
          default:
            return "com.google.gwt.core.client.JsArray";
        }
      }
      else {
        return "com.google.gwt.core.client.JsArray<" + super.convert(componentType) + ">";
      }
    }

    return super.convert(typeMirror);
  }

}