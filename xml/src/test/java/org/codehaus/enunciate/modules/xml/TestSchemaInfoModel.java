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

package org.codehaus.enunciate.modules.xml;

import junit.framework.TestCase;
import org.codehaus.enunciate.config.SchemaInfo;
import freemarker.ext.beans.BeansWrapper;

/**
 * @author Ryan Heaton
 */
public class TestSchemaInfoModel extends TestCase {

  /**
   * tests the get.
   */
  public void testGet() throws Exception {
    SchemaInfo schemaInfo = new SchemaInfo();
    schemaInfo.setProperty("filename", "something.xsd");
    schemaInfo.setProperty("location", "http://localhost:8080/something.xsd");
    SchemaInfoModel model = new SchemaInfoModel(schemaInfo, new BeansWrapper());
    assertEquals("something.xsd", model.get("filename").toString());
    assertEquals("http://localhost:8080/something.xsd", model.get("location").toString());
  }
  
}
