package net.sf.enunciate.modules.xfire;

import freemarker.template.TemplateException;
import net.sf.enunciate.InAPTTestCase;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.contract.jaxws.EndpointInterface;
import net.sf.enunciate.main.Enunciate;
import net.sf.jelly.apt.freemarker.FreemarkerModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

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
    EndpointInterface ei = new EndpointInterface(getDeclaration("net.sf.modules.xfire.SimpleEI"));
    model.add(ei);
    EndpointInterface ei2 = new EndpointInterface(getDeclaration("net.sf.modules.xfire.SimpleEIDifferentNS"));
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
    File file = (File) enunciate.getProperty("property.names.file");
    assertNotNull(file);
    ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
    HashMap<String, String[]> paramNames = (HashMap<String, String[]>) in.readObject();
    String[] params = paramNames.get("net.sf.modules.xfire.SimpleEI.doNothing");
    assertNotNull(params);
    assertEquals(2, params.length);
    assertEquals("firstParam", params[0]);
    assertEquals("secondParam", params[1]);
    params = paramNames.get("net.sf.modules.xfire.SimpleEIDifferentNS.doNothing");
    assertNotNull(params);
    assertEquals(2, params.length);
    assertEquals("someParam", params[0]);
    assertEquals("anotherParam", params[1]);
  }
}
