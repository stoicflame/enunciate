package org.codehaus.enunciate.modules.java_client;

import com.sun.mirror.type.InterfaceType;
import freemarker.template.TemplateModelException;
import org.codehaus.enunciate.contract.jaxb.Accessor;

import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class ClientClassnameForMethod extends org.codehaus.enunciate.template.freemarker.ClientClassnameForMethod {

  public ClientClassnameForMethod(Map<String, String> conversions) {
    super(conversions);
  }

  @Override
  public String convert(Accessor accessor) throws TemplateModelException {
    if (!accessor.isXmlList() && !accessor.isAdapted() && accessor.getBareAccessorType() instanceof InterfaceType) {
      if (accessor.isCollectionType()) {
        return "java.util.List<Object>";
      }
      else {
        return "Object";
      }
    }

    return super.convert(accessor);
  }

}
