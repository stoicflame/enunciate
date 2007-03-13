package org.codehaus.enunciate.modules.xfire_client;

import junit.framework.TestCase;
import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.aegis.type.DefaultTypeMappingRegistry;

import java.util.Collection;

/**
 * @author Ryan Heaton
 */
public class TestListParser extends TestCase {

  /**
   * tests parsing an xml list.
   */
  public void testParseList() throws Exception {
    ListParser parser = new ListParser("123 456 789 0", Collection.class, Integer.class, new DefaultTypeMappingRegistry(true).getDefaultTypeMapping(), new MessageContext());
    Object listObj = parser.getList();
    assertTrue(listObj instanceof Collection);
    Collection coll = (Collection) listObj;
    assertEquals(4, coll.size());
    assertTrue(coll.contains(new Integer(123)));
    assertTrue(coll.contains(new Integer(456)));
    assertTrue(coll.contains(new Integer(789)));
    assertTrue(coll.contains(new Integer(0)));

    parser = new ListParser("123 456 789 0", int[].class, new DefaultTypeMappingRegistry(true).getDefaultTypeMapping(), new MessageContext());
    listObj = parser.getList();
    assertTrue(listObj.getClass().isArray());
    assertSame(Integer.TYPE, listObj.getClass().getComponentType());
    int[] ints = (int[]) listObj;
    assertEquals(123, ints[0]);
    assertEquals(456, ints[1]);
    assertEquals(789, ints[2]);
    assertEquals(0, ints[3]);
  }

}
