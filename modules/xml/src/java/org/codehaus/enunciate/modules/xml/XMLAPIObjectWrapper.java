package org.codehaus.enunciate.modules.xml;

import freemarker.ext.beans.StringModel;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.codehaus.enunciate.config.SchemaInfo;
import org.codehaus.enunciate.config.WsdlInfo;
import net.sf.jelly.apt.decorations.JavaDoc;

import javax.xml.namespace.QName;

/**
 * Special wrapper for processing the XML templates.
 *
 * @author Ryan Heaton
 */
public class XMLAPIObjectWrapper extends DefaultObjectWrapper {

  @Override
  public TemplateModel wrap(Object obj) throws TemplateModelException {
    if (obj instanceof QName) {
      return new QNameModel((QName) obj, this);
    }
    else if (obj instanceof SchemaInfo) {
      return new SchemaInfoModel((SchemaInfo) obj, this);
    }
    else if (obj instanceof WsdlInfo) {
      return new WsdlInfoModel((WsdlInfo) obj, this);
    }
    else if (obj instanceof JavaDoc) {
      return new StringModel(obj, this);
    }
    else {
      return super.wrap(obj);
    }
  }
}
