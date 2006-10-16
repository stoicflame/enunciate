package net.sf.enunciate.modules.xfire_client;

import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.TypeMirror;
import freemarker.template.TemplateModelException;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converts a fully-qualified class name to its alternate client fully-qualified class name.
 *
 * @author Ryan Heaton
 */
public class ComponentTypeForMethod extends ClientClassnameForMethod {

  public ComponentTypeForMethod(Map<String, String> conversions) {
    super(conversions);
  }

  public ComponentTypeForMethod(Map<String, String> conversions, boolean jdk15) {
    super(conversions, jdk15);
  }

  @Override
  protected String convert(TypeMirror typeMirror) throws TemplateModelException {
    if (typeMirror instanceof ArrayType) {
      return super.convert(((ArrayType) typeMirror).getComponentType());
    }
    else if (typeMirror instanceof DecoratedTypeMirror) {
      DecoratedTypeMirror decoratedTypeMirror = ((DecoratedTypeMirror) typeMirror);
      if (decoratedTypeMirror.isCollection()) {
        DeclaredType declaredType = (DeclaredType) typeMirror;
        Collection<TypeMirror> actualTypeArguments = declaredType.getActualTypeArguments();
        if (actualTypeArguments.size() > 0) {
          return super.convert(actualTypeArguments.iterator().next());
        }
      }
    }

    throw new TemplateModelException("No component type for " + typeMirror);
  }

}
