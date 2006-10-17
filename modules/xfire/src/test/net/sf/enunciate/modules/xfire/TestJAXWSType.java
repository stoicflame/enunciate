package net.sf.enunciate.modules.xfire;

import org.codehaus.xfire.aegis.stax.ElementReader;
import org.codehaus.xfire.MessageContext;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 * @author Ryan Heaton
 */
public class TestJAXWSType {

  /**
   * Basic deserialization if {@link SimpleBean}.
   */
  @Test
  public void testSimpleBean() throws Exception {
    JAXWSType type = new JAXWSType(SimpleBean.class);
    ElementReader reader = new ElementReader(TestJAXWSType.class.getResourceAsStream("testSimpleBean.xml"));
    SimpleBean bean = (SimpleBean) type.readObject(reader, new MessageContext());
    assertEquals(bean.getStringProp(), "Wednesday");

  }
}
