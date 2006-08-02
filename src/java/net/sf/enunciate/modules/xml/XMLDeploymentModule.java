package net.sf.enunciate.modules.xml;

import freemarker.template.TemplateException;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.config.SchemaInfo;
import net.sf.enunciate.config.WsdlInfo;
import net.sf.enunciate.contract.jaxws.EndpointInterface;
import net.sf.enunciate.main.Enunciate;
import net.sf.enunciate.modules.FreemarkerDeploymentModule;
import net.sf.jelly.apt.freemarker.FreemarkerModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Deployment module for the XML schemas and WSDL.
 *
 * @author Ryan Heaton
 */
public class XMLDeploymentModule extends FreemarkerDeploymentModule {

  private XMLAPILookup lookup;

  protected URL getTemplateURL() {
    return XMLDeploymentModule.class.getResource("xml.fmt");
  }

  @Override
  public void processTemplate() throws IOException, TemplateException {
    super.processTemplate();

    EnunciateFreemarkerModel model = (EnunciateFreemarkerModel) FreemarkerModel.get();

    HashMap<String, String> ns2artifact = new HashMap<String, String>();
    HashMap<String, String> service2artifact = new HashMap<String, String>();
    Map<String, WsdlInfo> wsdls = model.getNamespacesToWSDLs();
    for (String ns : wsdls.keySet()) {
      WsdlInfo wsdl = wsdls.get(ns);
      String file = wsdl.getFile();
      ns2artifact.put(ns, file);
      for (EndpointInterface endpointInterface : wsdl.getEndpointInterfaces()) {
        service2artifact.put(endpointInterface.getServiceName(), file);
      }
    }

    Map<String, SchemaInfo> schemas = model.getNamespacesToSchemas();
    for (String ns : schemas.keySet()) {
      service2artifact.put(ns, schemas.get(ns).getFile());
    }

    lookup = new XMLAPILookup(ns2artifact, service2artifact);
  }

  @Override
  protected void doBuild() throws IOException {
    Enunciate enunciate = getEnunciate();
    File classes = new File(enunciate.getWebInf(), "classes");
    File xmlDir = new File(enunciate.getPreprocessDir(), "xml");

    //copy all generated xml files to the WEB-INF/classes directory.
    enunciate.copyDir(xmlDir, classes);

    if (lookup != null) {
      //store the lookup.
      FileOutputStream out = new FileOutputStream(new File(xmlDir, "xml-api.lookup"));
      lookup.store(out);
      out.close();
    }
    else {
      System.err.println("ERROR: No lookup was generated!  The contoller used to serve up the WSDLs and schemas will not function!");
    }
  }
}
