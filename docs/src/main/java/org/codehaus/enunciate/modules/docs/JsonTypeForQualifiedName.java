package org.codehaus.enunciate.modules.docs;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.json.JsonAnyTypeDefinition;
import org.codehaus.enunciate.contract.json.JsonListTypeDefinition;
import org.codehaus.enunciate.contract.json.JsonType;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * <p>
 * Find the JSON type for the given fully qualified name.
 * </p>
 * @author Steven Cummings
 */
public class JsonTypeForQualifiedName implements TemplateMethodModelEx {

  public static final String NAME = "jsonTypeForQualifiedName";

  private final EnunciateFreemarkerModel model;

  public JsonTypeForQualifiedName(final EnunciateFreemarkerModel model) {
    assert model != null : "model:null";

    this.model = model;
  }

  /**
   * {@inheritDoc}
   */
  public Object exec(final List arguments) throws TemplateModelException {
    if (arguments.size() != 1) {
      throw new TemplateModelException("The " + NAME + " method expects exactly one argument");
    }

    final Object object = BeansWrapper.getDefaultInstance().unwrap((TemplateModel) arguments.get(0));
    if (object instanceof String) {
      final String qualifiedName = (String) object;

      if(qualifiedName.endsWith("[]")) {
        JsonType elementTypeDefinition = model.findJsonTypeDefinition(qualifiedName.substring(0, qualifiedName.length() - 2));
        return new JsonListTypeDefinition(elementTypeDefinition);
      }

      try {
        Class<?> type = Class.forName(qualifiedName);

        // NOTE For some reason, without this cast, I get a type compatibility error within eclipse
        if (Collection.class.isAssignableFrom(type) && ((Type) type) instanceof ParameterizedType) {
          ParameterizedType parameterizedType = (ParameterizedType) (Type) type;
          Class elementType = (Class) parameterizedType.getActualTypeArguments()[0];
          JsonType elementTypeDefinition = model.findJsonTypeDefinition(elementType.getName());
          return new JsonListTypeDefinition(elementTypeDefinition);
        }
      } catch (ClassNotFoundException e) {
        // NOTE Not handling it is acceptable because types being enunciated are often source-only at this stage.
        // TODO log out the exception
      }

      final JsonType jsonTypeDefinition = model.findJsonTypeDefinition(qualifiedName);
      if(jsonTypeDefinition != null) {
        return jsonTypeDefinition;
      }
    }
    return JsonAnyTypeDefinition.INSTANCE;
  }
}
