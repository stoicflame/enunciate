package org.codehaus.enunciate.modules.xfire;

import freemarker.template.TemplateException;
import org.codehaus.enunciate.InAPTTestCase;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.main.Enunciate;
import net.sf.jelly.apt.freemarker.FreemarkerModel;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * @author Ryan Heaton
 */
public class TestXFireDeploymentModule extends InAPTTestCase {

  /**
   * tests doing the generate part.
   */
  public void testDoGenerate() throws Exception {
    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    FreemarkerModel.set(model);
    EndpointInterface ei = new EndpointInterface(getDeclaration("org.codehaus.modules.xfire.SimpleEI"));
    model.add(ei);
    EndpointInterface ei2 = new EndpointInterface(getDeclaration("org.codehaus.modules.xfire.SimpleEIDifferentNS"));
    model.add(ei2);

    final ArrayList<URL> processedTemplates = new ArrayList<URL>();
    XFireDeploymentModule module = new XFireDeploymentModule() {
      @Override
      public void processTemplate(URL templateURL, Object model) throws IOException, TemplateException {
        processedTemplates.add(templateURL);
      }
    };

    Enunciate enunciate = new Enunciate((String[]) null);
    enunciate.setGenerateDir(enunciate.createTempDir());
    module.init(enunciate);
    module.doFreemarkerGenerate();

    assertEquals(3, processedTemplates.size());
    assertTrue(processedTemplates.contains(module.getSpringServletTemplateURL()));
    assertTrue(processedTemplates.contains(module.getRPCRequestBeanTemplateURL()));
    assertTrue(processedTemplates.contains(module.getRPCResponseBeanTemplateURL()));
  }
}
