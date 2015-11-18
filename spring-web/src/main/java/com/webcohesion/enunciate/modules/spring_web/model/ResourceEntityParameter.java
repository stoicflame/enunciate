package com.webcohesion.enunciate.modules.spring_web.model;

import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedVariableElement;
import com.webcohesion.enunciate.javac.decorations.type.TypeVariableContext;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import com.webcohesion.enunciate.modules.spring_web.EnunciateSpringWebContext;
import com.webcohesion.enunciate.util.TypeHintUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * An entity parameter.
 *
 * @author Ryan Heaton
 */
public class ResourceEntityParameter extends DecoratedElement<Element> {

  private final TypeMirror type;

  public ResourceEntityParameter(RequestMapping method, VariableElement delegate, TypeVariableContext variableContext, EnunciateSpringWebContext context) {
    super(delegate, context.getContext().getProcessingEnvironment());
    TypeMirror typeMirror;
    final TypeHint hintInfo = getAnnotation(TypeHint.class);
    if (hintInfo != null) {
      typeMirror = TypeHintUtils.getTypeHint(hintInfo, this.env, delegate.asType());
    }
    else {
      typeMirror = delegate.asType();
    }

    //now resolve any type variables.
    typeMirror = variableContext.resolveTypeVariables(typeMirror, this.env);
    
    this.type = typeMirror;

    if (delegate instanceof DecoratedVariableElement) {
      getJavaDoc().setValue(((DecoratedVariableElement)delegate).getDocComment());
    }
  }

  public ResourceEntityParameter(Element delegate, TypeMirror type, ProcessingEnvironment env) {
    super(delegate, env);
    this.type = type;
  }


  public TypeMirror getType() {
    return type;
  }

}
