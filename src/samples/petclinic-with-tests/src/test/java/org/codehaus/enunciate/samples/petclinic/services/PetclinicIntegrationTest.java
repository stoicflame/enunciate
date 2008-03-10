package org.codehaus.enunciate.samples.petclinic.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.log4j.Logger;
import org.codehaus.enunciate.test.integration.GenericEnunciateTestCase;
import org.jdom.Document;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Sample integration tests using Jetty as a embedded test container.
 *
 * @author reaster
 */
public class PetclinicIntegrationTest extends GenericEnunciateTestCase {

       private static Logger logger = Logger.getLogger(PetclinicIntegrationTest.class);

       public static final String GET_OWNER_NOUN = "owner";

//      static protected String WS_NAME = "clinicService";
//      static Clinic clinic = null;
//
//      static Clinic getClinic() {
//              if (clinic == null) {
//                      clinic = new ClinicImpl(getSoapEndpoint(WS_NAME));
//              }
//              return clinic;
//      }

       @BeforeClass
       public static void setUpBeforeClass() throws Exception {
               //GenericEnunciateTestCase.setRelativeContext("/petclinic-1.0-SNAPSHOT");
               //GenericEnunciateTestCase.setWebappHome(new java.io.File("target/petclinic-1.0-SNAPSHOT"));
               GenericEnunciateTestCase.setUpBeforeClass();
               addDefaultNamespaces("ns0", "http://enunciate.codehaus.org/petclinic");

               //Object obj = GenericEnunciateTestCase.getWebContextBean("clinic");
               //ClinicImpl serviceImpl = (ClinicImpl)obj; get ClassCastException cus in different classloaders
               //logger.info("found ClinicDefault in WebContext: " + obj);
               // serviceImpl.setExampleManager( new ExampleManagerMock());
       }

       @AfterClass
       public static void tearDownAfterClass() {
               GenericEnunciateTestCase.tearDownAfterClass();
       }

       @Test
       public void testServiceIsRunning() throws Exception {
               GetMethod method = new GetMethod(getBaseContextAsString());
               try {
                       int statusCode = httpClient.executeMethod(method);
                       assertEquals("HttpStatus.SC_OK", HttpStatus.SC_OK, statusCode);
                       byte[] responseBody = method.getResponseBody();
                       assertNotNull("body read succesfully", responseBody);
               } finally {
                       method.releaseConnection();
               }
       }

       @Test
       public void testRestGetOwnerById() throws Exception {
               //<ns0:owner xmlns:ns0="http://enunciate.codehaus.org/petclinic" id="1"><address>3 Third Street, Suite 0</address><city>Orlando, FL</city><firstName>Sally</firstName><lastName>Gates</lastName><telephone>111-1111, x0</telephone><petIds><petId>16</petId></petIds></ns0:owner>
               GetMethod method = new GetMethod(getRestEndpoint(GET_OWNER_NOUN)+"/1");
               try {
                       int statusCode = httpClient.executeMethod(method);
                       assertEquals("HttpStatus.SC_OK", HttpStatus.SC_OK, statusCode);
                       Document xml = parseXml(method.getResponseBodyAsStream());
                       //new XMLOutputter().output(xml, System.out);
                       assertNotNull("body read succesfully", xml);
                       assertEquals("id==1", "1", xPathValue(xml, "/ns0:owner/@id"));
                       assertEquals("firstName==Sally", "Sally", xPathValue(xml, "/ns0:owner/firstName"));
               } finally {
                       method.releaseConnection();
               }
       }

       @Test
       public void testJsonGetOwnerById() throws Exception {
               //{"ns0.owner":{"@id":"1","address":"3 Third Street, Suite 0","city":"Orlando, FL","firstName":"Sally","lastName":"Gates","telephone":"111-1111, x0","petIds":{"petId":16}}}
               GetMethod method = new GetMethod(getJsonEndpoint(GET_OWNER_NOUN)+"/1");
               try {
                       int statusCode = httpClient.executeMethod(method);
                       assertEquals("HttpStatus.SC_OK", HttpStatus.SC_OK, statusCode);
                       String json = method.getResponseBodyAsString();
                       logger.info(json);
                       JSONObject jsonObj = new JSONObject(json);
                       JXPathContext context = JXPathContext.newContext(jsonObj);
                       assertEquals("id==1", "1", context.getValue("./ns0.owner[@name='@id']"));
                       assertEquals("firstName==Sally", "Sally", context.getValue("./ns0.owner/firstName"));
               } finally {
                       method.releaseConnection();
               }
       }

}