package org.codehaus.enunciate.contract.jaxrs;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.MirroredTypeException;
import com.sun.mirror.type.TypeMirror;
import net.sf.jelly.apt.decorations.declaration.DecoratedDeclaration;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.common.rest.RESTResourcePayload;
import org.codehaus.enunciate.contract.jaxb.ElementDeclaration;
import org.codehaus.enunciate.contract.json.JsonType;
import org.codehaus.enunciate.jaxrs.TypeHint;

/**
 * An entity parameter.
 *
 * @author Ryan Heaton
 */
public class ResourceEntityParameter extends DecoratedDeclaration implements RESTResourcePayload {

  private final TypeMirror type;

  public ResourceEntityParameter(ParameterDeclaration delegate) {
    super(delegate);
    TypeMirror typeMirror;
    TypeHint hintInfo = getAnnotation(TypeHint.class);
    if (hintInfo != null) {
      try {
        Class hint = hintInfo.value();
        AnnotationProcessorEnvironment env = net.sf.jelly.apt.Context.getCurrentEnvironment();
        TypeDeclaration type = env.getTypeDeclaration(hint.getName());
        typeMirror = env.getTypeUtils().getDeclaredType(type);
      }
      catch (MirroredTypeException e) {
        typeMirror = e.getTypeMirror();
      }
    }
    else {
      typeMirror = delegate.getType();
    }
    
    this.type = typeMirror;
  }

  public ResourceEntityParameter(Declaration delegate, TypeMirror type) {
    super(delegate);
    this.type = type;
  }

  // Inherited.
  public ElementDeclaration getXmlElement() {
    if (this.type instanceof ClassType) {
      ClassDeclaration declaration = ((ClassType) this.type).getDeclaration();
      if (declaration != null) {
        return ((EnunciateFreemarkerModel) FreemarkerModel.get()).findElementDeclaration(declaration);
      }
    }
    return null;
  }

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
