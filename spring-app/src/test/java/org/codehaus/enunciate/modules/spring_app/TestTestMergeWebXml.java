/*
 * Copyright 2006-2008 Web Cohesion
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

package org.codehaus.enunciate.modules.spring_app;

import freemarker.ext.dom.NodeModel;
import junit.framework.TestCase;
import net.sf.jelly.apt.strategies.FileStrategy;
import net.sf.jelly.apt.strategies.MissingParameterException;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.template.freemarker.EnunciateFileTransform;
import org.codehaus.enunciate.template.strategies.EnunciateFileStrategy;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;

/**
 * @author Ryan Heaton
 */
public class TestTestMergeWebXml extends TestCase {

  /**
   * tests merging two web.xml files.
   */
  public void testMergeWebXml() throws Exception {
    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
    final PrintWriter out = new PrintWriter(bytesOut);

    SpringAppDeploymentModule module = new SpringAppDeploymentModule();
    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    EnunciateFileTransform transform = new EnunciateFileTransform(null) {
      @Override
      public FileStrategy newStrategy() {
        return new EnunciateFileStrategy(null) {
          @Override
          public PrintWriter getWriter() throws IOException, MissingParameterException {
            return out;
          }
        };
      }
    };
    model.put("file", transform);
    model.put("source1", new SpringAppDeploymentModule().loadMergeXmlModel(TestTestMergeWebXml.class.getResourceAsStream("web.1.xml")));
    model.put("source2", new SpringAppDeploymentModule().loadMergeXmlModel(TestTestMergeWebXml.class.getResourceAsStream("web.2.xml")));
    module.processTemplate(SpringAppDeploymentModule.class.getResource("merge-web-xml.fmt"), model);
    model.put("source2", new SpringAppDeploymentModule().loadMergeXmlModel(TestTestMergeWebXml.class.getResourceAsStream("web.3.xml")));
    module.processTemplate(SpringAppDeploymentModule.class.getResource("merge-web-xml.fmt"), model);

    //todo: better tests?
    assertTrue(bytesOut.size() > 0);
    //uncomment to see what's being written.
//    System.out.println(bytesOut.toString("utf-8"));
  }

  /**
   * tests merging two web.xml files.
   */
  public void testMergePetclinicWebXml() throws Exception {
    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
    final PrintWriter out = new PrintWriter(bytesOut);

    SpringAppDeploymentModule module = new SpringAppDeploymentModule();
    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    EnunciateFileTransform transform = new EnunciateFileTransform(null) {
      @Override
      public FileStrategy newStrategy() {
        return new EnunciateFileStrategy(null) {
          @Override
          public PrintWriter getWriter() throws IOException, MissingParameterException {
            return out;
          }
        };
      }
    };
    model.put("file", transform);
    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    builderFactory.setNamespaceAware(false);
    NodeModel.setDocumentBuilderFactory(builderFactory);
    model.put("source1", new SpringAppDeploymentModule().loadMergeXmlModel(TestTestMergeWebXml.class.getResourceAsStream("web.1.xml")));
    model.put("source2", new SpringAppDeploymentModule().loadMergeXmlModel(TestTestMergeWebXml.class.getResourceAsStream("petclinic.web.xml")));
    module.processTemplate(SpringAppDeploymentModule.class.getResource("merge-web-xml.fmt"), model);

    //todo: better tests?
    assertTrue(bytesOut.size() > 0);
    //uncomment to see what's being written.
//    System.out.println(bytesOut.toString("utf-8"));
  }

}
