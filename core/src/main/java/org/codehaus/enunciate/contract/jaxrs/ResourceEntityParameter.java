package org.codehaus.enunciate.contract.jaxrs;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.TypeMirror;
import net.sf.jelly.apt.decorations.declaration.DecoratedDeclaration;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.common.rest.RESTResourcePayload;
import org.codehaus.enunciate.contract.jaxb.ElementDeclaration;
import org.codehaus.enunciate.contract.json.JsonType;

/**
 * An entity parameter.
 *
 * @author Ryan Heaton
 */
public class ResourceEntityParameter extends DecoratedDeclaration implements RESTResourcePayload {

  private final TypeMirror type;

  public ResourceEntityParameter(ParameterDeclaration delegate) {
    super(delegate);
    this.type = delegate.getType();
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
