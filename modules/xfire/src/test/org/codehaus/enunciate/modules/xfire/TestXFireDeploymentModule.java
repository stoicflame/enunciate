/*
 * Copyright 2006 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    Enunciate enunciate = new Enunciate(new String[0]);
    enunciate.setGenerateDir(enunciate.createTempDir());
    module.init(enunciate);
    module.doFreemarkerGenerate();

    assertEquals(3, processedTemplates.size());
    assertTrue(processedTemplates.contains(module.getSpringServletTemplateURL()));
    assertTrue(processedTemplates.contains(module.getRPCRequestBeanTemplateURL()));
    assertTrue(processedTemplates.contains(module.getRPCResponseBeanTemplateURL()));
  }
}
