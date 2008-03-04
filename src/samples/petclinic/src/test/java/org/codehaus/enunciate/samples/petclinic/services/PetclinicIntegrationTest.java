package org.codehaus.enunciate.samples.petclinic.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.StringTokenizer;

import javax.xml.XMLConstants;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.log4j.Logger;
import org.codehaus.enunciate.samples.petclinic.services.Clinic;
import org.codehaus.enunciate.test.integration.GenericEnunciateTestCase;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
//import org.w3c.dom.Document;
//import org.w3c.dom.NamedNodeMap;
//import org.w3c.dom.Node;

public class PetclinicIntegrationTest extends GenericEnunciateTestCase {

	private static Logger logger = Logger.getLogger(PetclinicIntegrationTest.class);

	public static final String GET_OWNER_NOUN = "owner";

	static protected String WS_NAME = "clinicService";

	static Clinic clinic = null;

//	static Clinic getClinic() {
//		if (clinic == null) {
//			clinic = new ClinicImpl(getSoapEndpoint(WS_NAME));
//		}
//		return clinic;
//	}
//
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		GenericEnunciateTestCase.setUpBeforeClass();
		Object obj = GenericEnunciateTestCase.getWebContextBean("clinic");
		//ClinicImpl serviceImpl = (ClinicImpl)obj;
		logger.info("found ClinicDefault in WebContext: " + obj);
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

//	@Test
//	public void testRestReadExampleType() throws Exception {
//		addDefaultNamespaces("xsi",
//				XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "xs",
//				XMLConstants.W3C_XML_SCHEMA_NS_URI, "ns1",
//				"http://connectapi.garmin.com/1.0_SNAPSHOT/example-service-data");
//		String rest = getRestEndpoint(EXAMPLE_TYPE_NOUN) + "/99";
//		GetMethod method = new GetMethod(rest);
//		try {
//			int statusCode = httpClient.executeMethod(method);
//			assertEquals("HttpStatus.SC_OK", HttpStatus.SC_OK, statusCode);
//			Document xml = parseXml(method.getResponseBodyAsStream());
//			String idValue = xPathValue(xml, "//id");
//			assertEquals("contains id", "99", idValue);
//			String valueValue = xPathValue(xml, "//value");
//			assertEquals("contains id", "true", valueValue);
//			// TODO get XPath to work using namespaces
//			// String xsiTypeValue = xPathValue(xml, "//value/@xsi:type");
//			// assertEquals("contains value type", "xs:boolean", xsiTypeValue);
//			Node xsiTypeNode = xPathNode(xml, "//value");
//			NamedNodeMap attrs = xsiTypeNode.getAttributes();
//			Node xsiAttrValue = attrs.getNamedItem("xsi:type");
//			String xsiTypeValue2 = xsiAttrValue.getNodeValue();
//			assertEquals("contains value type", "xs:boolean", xsiTypeValue2);
//		} finally {
//			method.releaseConnection();
//		}
//	}
//
//	@Test
//	public void testJsonReadExampleType() throws Exception {
//		String rest = getJsonEndpoint(EXAMPLE_TYPE_NOUN) + "/99";
//		// result:
//		// {"ns1.exampleType":{"id":"99","value":{"@xsi.type":"xs:boolean"}}}
//		GetMethod method = new GetMethod(rest);
//		try {
//			int statusCode = httpClient.executeMethod(method);
//			assertEquals("HttpStatus.SC_OK", HttpStatus.SC_OK, statusCode);
//			// testing using strings
//			String body = method.getResponseBodyAsString();
//			assertTrue("contains id", body.indexOf("id\":\"99") > 1);
//			assertTrue("contains value", body
//					.indexOf("\"@xsi.type\":\"xs:boolean\"") > 1);
//			// testing using JSONObject
//			JSONObject jsonObj = new JSONObject(body);
//			Object result = poorMansJXPath(jsonObj,
//					"ns1.exampleType/value/@xsi.type");
//			assertEquals("@xsi.type == xs:boolean", "xs:boolean", result);
//			// testing using jXPathValue helper method
//			Object xsiTypeValue = jXPathValue(jsonObj,
//					"./ns1.exampleType/value[@name='@xsi.type']");
//			assertEquals("@xsi.type == xs:boolean", "xs:boolean", xsiTypeValue);
//			// testing using JXPathContext
//			JXPathContext context = JXPathContext.newContext(jsonObj);
//			Object xsiTypeValue2 = context
//					.getValue("./ns1.exampleType/value[@name='@xsi.type']");
//			assertEquals("@xsi.type == xs:boolean", "xs:boolean", xsiTypeValue2);
//		} finally {
//			method.releaseConnection();
//		}
//	}
//
//	@Test
//	public void testReadExampleTypeSoapClient() {
//		// we are testing this instance: ExampleType("99", Boolean.TRUE);
//		ExampleType exampleType = getClinic().readExampleType("99");
//		assertNotNull("exampleType not null", exampleType);
//		assertEquals("value == TRUE", Boolean.TRUE, exampleType.getValue());
//	}
//
//	@Test
//	public void testReadExampleTypesSoapClient() {
//		ExampleTypes exampleTypes = getClinic().readExampleTypes();
//		assertNotNull(exampleTypes);
//		assertFalse(exampleTypes.getList().isEmpty());
//	}

}
