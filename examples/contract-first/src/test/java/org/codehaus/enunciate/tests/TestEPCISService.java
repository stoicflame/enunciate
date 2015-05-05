package org.codehaus.enunciate.tests;

import epcglobal.client.epcis.wsdl._1.impl.EPCISServicePortTypeImpl;
import epcglobal.client.epcis_query.xsd._1.ArrayOfString;
import epcglobal.client.epcis_query.xsd._1.EmptyParms;
import junit.framework.TestCase;

import java.util.TreeSet;

/**
 * @author Ryan Heaton
 */
public class TestEPCISService extends TestCase {


  /**
   * the servcie.
   */
  public void testEPCISService() throws Exception {
    EPCISServicePortTypeImpl service = new EPCISServicePortTypeImpl();
    ArrayOfString queryNames = service.getQueryNames(new EmptyParms());
    TreeSet<String> names = new TreeSet<String>();
    assertNotNull(queryNames.getString());
    names.addAll(queryNames.getString());
    assertTrue(names.remove("hello"));
    assertTrue(names.remove("how"));
    assertTrue(names.remove("are"));
    assertTrue(names.remove("you?"));
    assertTrue(names.isEmpty());
  }

}
