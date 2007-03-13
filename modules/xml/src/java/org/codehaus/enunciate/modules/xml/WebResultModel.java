package org.codehaus.enunciate.modules.xml;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.codehaus.enunciate.contract.jaxws.WebResult;

/**
 * @author Ryan Heaton
 */
public class WebResultModel implements TemplateHashModel {

  private WebResult result;
  private BeansWrapper wrapper;

  public WebResultModel(WebResult result, BeansWrapper wrapper) {
    this.result = result;
    this.wrapper = wrapper;
  }

  public TemplateModel get(String key) throws TemplateModelException {
    if ("name".equals(key)) {
      return this.wrapper.wrap(result.getName());
    }
    else if ("targetNamespace".equals(key)) {
      return this.wrapper.wrap(result.getTargetNamespace());
    }
    else if ("partName".equals(key)) {
      return this.wrapper.wrap(result.getPartName());
    }
    else if ("webMethod".equals(key)) {
      return this.wrapper.wrap(result.getWebMethod());
    }
    else if ("delegate".equals(key)) {
      return this.wrapper.wrap(null);
    }
    else {
      return ((TemplateHashModel) this.wrapper.wrap(result.getDelegate())).get(key);
    }
  }

  public boolean isEmpty() throws TemplateModelException {
    return false;
  }

}
