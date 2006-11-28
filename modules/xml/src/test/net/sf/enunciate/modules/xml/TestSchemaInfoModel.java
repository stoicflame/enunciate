package net.sf.enunciate.modules.xml;

import junit.framework.TestCase;
import net.sf.enunciate.config.SchemaInfo;
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
    schemaInfo.setProperty("file", "something.xsd");
    schemaInfo.setProperty("location", "http://localhost:8080/something.xsd");
    SchemaInfoModel model = new SchemaInfoModel(schemaInfo, new BeansWrapper());
    assertEquals("something.xsd", model.get("file").toString());
    assertEquals("http://localhost:8080/something.xsd", model.get("location").toString());
  }
  
}
