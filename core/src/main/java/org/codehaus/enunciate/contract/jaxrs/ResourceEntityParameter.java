package org.codehaus.enunciate.contract.jaxrs;

import net.sf.jelly.apt.decorations.declaration.DecoratedParameterDeclaration;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.contract.common.rest.RESTResourcePayload;
import org.codehaus.enunciate.contract.jaxb.RootElementDeclaration;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.type.ClassType;

/**
 * An entity parameter.
 *
 * @author Ryan Heaton
 */
public class ResourceEntityParameter extends DecoratedParameterDeclaration implements RESTResourcePayload {

  public ResourceEntityParameter(ParameterDeclaration delegate) {
    super(delegate);
  }
  
  // Inherited.
  public RootElementDeclaration getXmlElement() {
    TypeMirror type = getType();
    if (type instanceof ClassType) {
      ClassDeclaration declaration = ((ClassType) type).getDeclaration();
      if (declaration != null) {
        return ((EnunciateFreemarkerModel) FreemarkerModel.get()).findRootElementDeclaration(declaration);
      }
    }
    return null;
  }
}
