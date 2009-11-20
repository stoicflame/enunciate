package org.codehaus.enunciate.modules.docs;

import java.util.List;

import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.json.JsonType;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * <p>
 * Finds the name of the JSON type corresponding to the given qualified name.
 * </p>
 * @author Steven Cummings
 */
public class JsonTypeNameForQualifiedName implements TemplateMethodModelEx {
  /**
   * Method name.
   */
  public static final String NAME = "jsonTypeNameForQualifiedName";

  private final EnunciateFreemarkerModel model;

  public JsonTypeNameForQualifiedName(final EnunciateFreemarkerModel model)
  {
    assert model != null : "model must not be null";
    
    this.model = model;
  }

  /**
   * {@inheritDoc}
   */
  public Object exec(List arguments) throws TemplateModelException {
    if (arguments.size() != 1) {
      throw new TemplateModelException("The " + NAME + " method expects exactly one argument");
    }

    final Object object = BeansWrapper.getDefaultInstance().unwrap((TemplateModel) arguments.get(0));

    if (object instanceof String) {
      final String typeName = (String) object;
      final JsonType jsonType = model.findJsonTypeDefinition(typeName);
      if (jsonType != null) {
        return jsonType.getTypeName();
      }
      return typeName;
    }
    return null;
  }
}
