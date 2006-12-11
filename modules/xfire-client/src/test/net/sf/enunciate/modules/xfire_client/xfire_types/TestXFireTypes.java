package net.sf.enunciate.modules.xfire_client.xfire_types;

import junit.framework.TestCase;
import net.sf.enunciate.examples.xfire_client.schema.Circle;
import net.sf.enunciate.examples.xfire_client.schema.Color;
import net.sf.enunciate.examples.xfire_client.schema.LineStyle;

import javax.xml.bind.JAXBContext;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

/**
 * This is a special test case that depends on the sample schema included in this module.
 *
 * @author Ryan Heaton
 */
public class TestXFireTypes extends TestCase {

  /**
   * tests the basic shapes.
   */
  public void testBasicShapes() throws Exception {
    Circle circle = new Circle();
    circle.setColor(Color.BLUE);
    circle.setId("someid");
    circle.setLineStyle(LineStyle.solid);
    circle.setPositionX(8);
    circle.setPositionY(9);
    circle.setRadius(10);
    JAXBContext context = JAXBContext.newInstance(Circle.class);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    context.createMarshaller().marshal(circle, out);
    out.close();
    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    
  }

}
