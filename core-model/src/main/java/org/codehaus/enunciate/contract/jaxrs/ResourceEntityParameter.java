package org.codehaus.enunciate.contract.jaxrs;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.MirroredTypeException;
import com.sun.mirror.type.TypeMirror;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedDeclaration;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxb.ElementDeclaration;
import org.codehaus.enunciate.contract.json.JsonType;
import org.codehaus.enunciate.jaxrs.TypeHint;

/**
 * An entity parameter.
 *
 * @author Ryan Heaton
 */
public class ResourceEntityParameter extends DecoratedDeclaration {

  private final TypeMirror type;

  public ResourceEntityParameter(ResourceMethod method, ParameterDeclaration delegate) {
    super(delegate);
    TypeMirror typeMirror;
    TypeHint hintInfo = getAnnotation(TypeHint.class);
    if (hintInfo != null) {
      try {
        Class hint = hintInfo.value();
        AnnotationProcessorEnvironment env = net.sf.jelly.apt.Context.getCurrentEnvironment();
        if (TypeHint.NO_CONTENT.class.equals(hint)) {
          typeMirror = env.getTypeUtils().getVoidType();
        }
        else {
          String hintName = hint.getName();

          if (TypeHint.NONE.class.equals(hint)) {
            hintName = hintInfo.qualifiedName();
          }

          if (!"##NONE".equals(hintName)) {
            TypeDeclaration type = env.getTypeDeclaration(hintName);
            typeMirror = TypeMirrorDecorator.decorate(env.getTypeUtils().getDeclaredType(type));
          }
          else {
            typeMirror = delegate.getType();
          }
        }
      }
      catch (MirroredTypeException e) {
        typeMirror = e.getTypeMirror();
      }
    }
    else {
      typeMirror = delegate.getType();

      if (getJavaDoc().get("inputWrapped") != null) { //support jax-doclets. see http://jira.codehaus.org/browse/ENUNCIATE-690
        String fqn = getJavaDoc().get("inputWrapped").get(0);
        AnnotationProcessorEnvironment env = net.sf.jelly.apt.Context.getCurrentEnvironment();
        TypeDeclaration type = env.getTypeDeclaration(fqn);
        if (type != null) {
          typeMirror = TypeMirrorDecorator.decorate(env.getTypeUtils().getDeclaredType(type));
        }
      }
    }
    
    this.type = typeMirror;
  }

  public ResourceEntityParameter(Declaration delegate, TypeMirror type) {
    super(delegate);
    this.type = type;
  }

  /**
   * The XML element associated with the entity parameter, or null if none (or unknown).
   *
   * @return The XML element associated with the entity parameter, or null if none (or unknown).
   */
  public ElementDeclaration getXmlElement() {
    if (this.type instanceof ClassType) {
      ClassDeclaration declaration = ((ClassType) this.type).getDeclaration();
      if (declaration != null) {
        return ((EnunciateFreemarkerModel) FreemarkerModel.get()).findElementDeclaration(declaration);
      }
    }
    return null;
  }

  /**
   * The JSON element associated with the entity parameter, or null if none (or unknown).
   *
   * @return The JSON element associated with the entity parameter, or null if none (or unknown).
   */
  public JsonType getJsonType() {
    if (this.type instanceof ClassType) {
      ClassDeclaration declaration = ((ClassType) this.type).getDeclaration();
      if (declaration != null) {
        return ((EnunciateFreemarkerModel) FreemarkerModel.get()).findJsonTypeDefinition(declaration);
      }
    }
    return null;
  }

  public TypeMirror getType() {
    return type;
  }

}
