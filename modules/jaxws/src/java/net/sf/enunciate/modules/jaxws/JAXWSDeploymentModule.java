package net.sf.enunciate.modules.jaxws;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateException;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.config.WsdlInfo;
import net.sf.enunciate.contract.jaxws.*;
import net.sf.enunciate.contract.validation.Validator;
import net.sf.enunciate.modules.FreemarkerDeploymentModule;
import net.sf.enunciate.util.ClassDeclarationComparator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.TreeSet;

/**
 * Deployment module for the XML schemas and WSDL.
 *
 * @author Ryan Heaton
 */
public class JAXWSDeploymentModule extends FreemarkerDeploymentModule {

  /**
   * @return "xml"
   */
  @Override
  public String getName() {
    return "jaxws";
  }

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException {
    EnunciateFreemarkerModel model = getModel();
    model.setObjectWrapper(new DefaultObjectWrapper());

    Map<String, WsdlInfo> ns2wsdl = model.getNamespacesToWSDLs();

    URL requestBeanTemplate = JAXWSDeploymentModule.class.getResource("request-bean.fmt");
    URL responseBeanTemplate = JAXWSDeploymentModule.class.getResource("response-bean.fmt");
    URL faultBeanTemplate = JAXWSDeploymentModule.class.getResource("fault-bean.fmt");

    TreeSet<WebFault> visitedFaults = new TreeSet<WebFault>(new ClassDeclarationComparator());
    for (WsdlInfo wsdlInfo : ns2wsdl.values()) {
      for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
        for (WebMethod webMethod : ei.getWebMethods()) {
          for (WebMessage webMessage : webMethod.getMessages()) {
            if (webMessage instanceof RequestWrapper) {
              model.put("message", webMessage);
              processTemplate(requestBeanTemplate, model);
            }
            else if (webMessage instanceof ResponseWrapper) {
              model.put("message", webMessage);
              processTemplate(responseBeanTemplate, model);
            }
            else if ((webMessage instanceof WebFault) && ((WebFault) webMessage).isImplicitSchemaElement() && visitedFaults.add((WebFault) webMessage)) {
              model.put("message", webMessage);
              processTemplate(faultBeanTemplate, model);
            }
          }
        }
      }
    }

    getEnunciate().setProperty("jaxws.src.dir", new File(getEnunciate().getGenerateDir(), "jaxws"));
  }

  @Override
  public Validator getValidator() {
    return new JAXWSValidator();
  }

  /**
   * @return 1
   */
  @Override
  public int getOrder() {
    return 1;
  }
}
