package net.sf.enunciate.modules.xfire;

import org.codehaus.xfire.aegis.stax.ElementReader;
import org.codehaus.xfire.MessageContext;
import junit.framework.TestCase;

/**
 * @author Ryan Heaton
 */
public class TestJAXWSType extends TestCase {

  /**
   * Basic deserialization if {@link SimpleBean}.
   */
  public void testSimpleBean() throws Exception {
    JAXWSType type = new JAXWSType(SimpleBean.class);
    ElementReader reader = new ElementReader(TestJAXWSType.class.getResourceAsStream("testSimpleBean.xml"));
    SimpleBean bean = (SimpleBean) type.readObject(reader, new MessageContext());
    assertEquals("Wednesday", bean.getStringProp());

  }
}
