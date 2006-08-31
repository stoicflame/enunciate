package net.sf.enunciate.modules.xml;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

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
      final QName qname = (QName) obj;
      return new QNameModel(qname, this);
    }

    return super.wrap(obj);
  }
}
