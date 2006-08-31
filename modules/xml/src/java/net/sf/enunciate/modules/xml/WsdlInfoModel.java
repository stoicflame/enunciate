package net.sf.enunciate.modules.xml;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import net.sf.enunciate.config.WsdlInfo;

/**
 * @author Ryan Heaton
 */
public class WsdlInfoModel extends StringModel {

  private final WsdlInfo wsdlInfo;

  public WsdlInfoModel(WsdlInfo wsdlInfo, BeansWrapper wrapper) {
    super(wsdlInfo, wrapper);
    this.wsdlInfo = wsdlInfo;
  }

  @Override
  public TemplateModel get(String key) throws TemplateModelException {
    if (("file".equals(key)) || ("location".equals(key))) {
      return wrap(wsdlInfo.getProperty(key));
    }

    return super.get(key);
  }
}
