package com.webcohesion.enunciate.modules.jaxrs.model;

import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import com.webcohesion.enunciate.modules.jaxrs.EnunciateJaxrsContext;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * An entity parameter.
 *
 * @author Ryan Heaton
 */
public class ResourceEntityParameter extends DecoratedElement<Element> {

  private final TypeMirror type;

  public ResourceEntityParameter(ResourceMethod method, VariableElement delegate, EnunciateJaxrsContext context) {
    super(delegate, context.getContext().getProcessingEnvironment());
    TypeMirror typeMirror;
    final TypeHint hintInfo = getAnnotation(TypeHint.class);
    if (hintInfo != null) {
      try {
        Class hint = hintInfo.value();
        if (TypeHint.NO_CONTENT.class.equals(hint)) {
          typeMirror = env.getTypeUtils().getNoType(TypeKind.VOID);
        }
        else {
          String hintName = hint.getName();

          if (TypeHint.NONE.class.equals(hint)) {
            hintName = hintInfo.qualifiedName();
          }

          if (!"##NONE".equals(hintName)) {
            TypeElement type = env.getElementUtils().getTypeElement(hintName);
            typeMirror = TypeMirrorDecorator.decorate(env.getTypeUtils().getDeclaredType(type), this.env);
          }
          else {
            typeMirror = delegate.asType();
          }
        }
      }
      catch (MirroredTypeException e) {
        typeMirror = e.getTypeMirror();
      }
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
    
    this.type = typeMirror;
  }

  public ResourceEntityParameter(Element delegate, TypeMirror type, ProcessingEnvironment env) {
    super(delegate, env);
    this.type = type;
  }


  public TypeMirror getType() {
    return type;
  }

}
