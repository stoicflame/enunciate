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

import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedVariableElement;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.decorations.type.TypeVariableContext;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.javac.javadoc.JavaDocTagHandler;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import com.webcohesion.enunciate.modules.jaxrs.EnunciateJaxrsContext;
import com.webcohesion.enunciate.util.TypeHintUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * An entity parameter.
 *
 * @author Ryan Heaton
 */
public class ResourceEntityParameter extends DecoratedElement<Element> {

  private final TypeMirror type;

  public ResourceEntityParameter(ResourceMethod method, VariableElement delegate, TypeVariableContext variableContext, EnunciateJaxrsContext context) {
    super(delegate, context.getContext().getProcessingEnvironment());
    TypeMirror typeMirror;
    final TypeHint hintInfo = getAnnotation(TypeHint.class);
    if (hintInfo != null) {
      typeMirror = TypeHintUtils.getTypeHint(hintInfo, this.env, delegate.asType());
    }
    else {
      typeMirror = delegate.asType();

      if (getJavaDoc().get("inputWrapped") != null) { //support jax-doclets. see http://jira.codehaus.org/browse/ENUNCIATE-690
        String fqn = getJavaDoc().get("inputWrapped").get(0);
        TypeElement type = env.getElementUtils().getTypeElement(fqn);
        if (type != null) {
          typeMirror = TypeMirrorDecorator.decorate(env.getTypeUtils().getDeclaredType(type), this.env);
        }
      }
    }

    typeMirror = TypeMirrorDecorator.decorate(typeMirror, this.env);
    if (((DecoratedTypeMirror)typeMirror).isInstanceOf(java.io.InputStream.class)) {
      //special case for input stream: just treat it as a generic object.
      typeMirror = this.env.getElementUtils().getTypeElement(Object.class.getName()).asType();
    }
    else {
      //now resolve any type variables.
      typeMirror = variableContext.resolveTypeVariables(typeMirror, this.env);
    }

    this.type = typeMirror;
  }

  public ResourceEntityParameter(Element delegate, TypeMirror type, DecoratedProcessingEnvironment env) {
    super(delegate, env);
    this.type = type;
  }


  public TypeMirror getType() {
    return type;
  }

  @Override
  protected JavaDoc getJavaDoc(JavaDocTagHandler tagHandler, boolean useDelegate) {
    return super.getJavaDoc(tagHandler, true);
  }


}
