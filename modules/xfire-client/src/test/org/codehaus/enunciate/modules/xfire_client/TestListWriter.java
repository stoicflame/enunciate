package org.codehaus.enunciate.modules.xfire_client;

import junit.framework.TestCase;
import org.codehaus.xfire.aegis.type.DefaultTypeMappingRegistry;
import org.codehaus.xfire.aegis.type.TypeMapping;
import org.codehaus.xfire.MessageContext;

import java.util.Arrays;

/**
 * @author Ryan Heaton
 */
public class TestListWriter extends TestCase {

  /**
   * tests writing a list.
   */
  public void testWriteList() throws Exception {
    TypeMapping typeMapping = new DefaultTypeMappingRegistry(true).getDefaultTypeMapping();
    ListWriter writer = new ListWriter(new long[]{1234567890, 987654321, 6789054321L}, typeMapping, new MessageContext());
    assertEquals("1234567890 987654321 6789054321", writer.getValue());

    writer = new ListWriter(Arrays.asList("twinkle,", "twinkle", "little", "star"), typeMapping, new MessageContext());
    assertEquals("twinkle, twinkle little star", writer.getValue());
  }
}
