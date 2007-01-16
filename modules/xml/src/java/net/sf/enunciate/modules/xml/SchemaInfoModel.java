package net.sf.enunciate.modules.xml;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import net.sf.enunciate.config.SchemaInfo;

/**
 * The model for a {@link net.sf.enunciate.config.SchemaInfo}, taking into account the custom properties set
 * by the configuration for the xml module.
 *
 * @author Ryan Heaton
 */
public class SchemaInfoModel extends StringModel {

  private final SchemaInfo schemaInfo;

  public SchemaInfoModel(SchemaInfo schemaInfo, BeansWrapper wrapper) {
    super(schemaInfo, wrapper);
    this.schemaInfo = schemaInfo;
  }

  @Override
  public TemplateModel get(String key) throws TemplateModelException {
    if (("filename".equals(key)) || ("location".equals(key))) {
      return wrap(schemaInfo.getProperty(key));
    }

    return super.get(key);
  }
}
