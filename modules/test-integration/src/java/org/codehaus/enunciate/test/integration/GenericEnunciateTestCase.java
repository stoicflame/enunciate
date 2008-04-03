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

package org.codehaus.enunciate.test.integration;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.codehaus.enunciate.test.integration.util.JSONObjectJXPathHandler;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Parent;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.json.JSONException;
import org.json.JSONObject;
import org.mortbay.jetty.webapp.WebAppContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * <h1>Web Service Integration Testing</h1>
 *
 * <p>Integration testing support allows web services to be tested in-place skipping the deployment step. Web service integration tests are supported
 * by two abstract base classes:</p>
 *
 * <ul>
 *  <li>org.codehaus.enunciate.test.integration.GenericEnunciateTestCase</li>
 *  <li>org.codehaus.enunciate.test.integration.GenericWebServerTestCase</li>
 * </ul>
 *
 * <h2>Configuration</h2>
 *
 * <p>Standard Maven dependency:</p>
 *
 * <code class="console">
 * &gt;dependency&lt;
 *	&gt;groupId&lt;org.codehaus.enunciate&gt;/groupId&lt;
 * 	&gt;artifactId&lt;enunciate-test-integration&gt;/artifactId&lt;
 *	&gt;scope&lt;test&gt;/scope&lt;
 * &gt;/dependency&lt;
 * </code>
 *
 * Minimal Jetty configuration requires two properties: the web application context and location of the exploded war. For development, you can hard-code
 * these settings in your @BeforeClass method:
 *
 * <code class="console">
 * &#064;BeforeClass
 * public static void startServer() throws Exception {
 * 	JettyTest.startServer(new JettyTest.Initializer() {
 * 	&#064;Override
 * 	public void initialize() {
 * 		GenericWebServerTestCase.setRelativeContext("/my-webapp-SNAPSHOT-1.0");
 * 		GenericWebServerTestCase.setWebappHome("target/my-webapp-SNAPSHOT-1.0");
 * 		GenericWebServerTestCase.setUpBeforeClass();
 * 	}
 * });
 * </code>
 *
 * Because these values change with the version, you might wan't to switch to the filtered property file configuration,
 * before you release your project to integration:
 *
 * <code class="console">
 * &#064;BeforeClass
 * public static void startServer() throws Exception {
 * 	JettyTest.startServer(new JettyTest.Initializer() {
 * 	&#064;Override
 * 	public void initialize() {
 * 		GenericWebServerTestCase.setPropertyFileLocation("target/test-classes/jetty.properties");
 * 		GenericWebServerTestCase.setUpBeforeClass();
 * 	}
 * });
 * </code>
 *
 * Your <tt>src/test/resources/jetty.properties</tt> file should look like this:
 *
 * <code class="console">
 * context=/${project.build.finalName}
 * webapp.home=${project.build.directory}/${project.build.finalName}
 * host=localhost
 * protocol=http
 * port=0
 * </code>
 *
 * <h1>Basic Testing</h1>
 *
 * <p>About the simplest test you can write is to use the provided <<<httpClient>>> instance to just read a page
 * and verify that there are no errors:</p>
 *
 * <code class="console">
 * &#064;Test
 * public void testServiceIsRunning() throws Exception {
 * 	GetMethod method = new GetMethod(getBaseContextAsString());
 * 	try {
 * 		int statusCode = httpClient.executeMethod(method);
 * 		assertEquals("HttpStatus.SC_OK", HttpStatus.SC_OK, statusCode);
 * 		byte[] responseBody = method.getResponseBody();
 * 		assertNotNull("body read succesfully", responseBody);
 * 	} finally {
 * 		method.releaseConnection();
 * 	}
 * }
 * </code>
 *
 * <p>The <tt>getBaseContextAsString</tt> method will return the application context, including the host, protocol and port assigned by Jetty.  If a port of
 * <tt>0</tt> is specified, Jetty will pick an unused port to run the tests on. You just need to call one of the methods the returns the selected port to
 * access the endpoint.</p>
 *
 * <h1>Enunciate Tests</h1>
 *
 * <p>This JUnit test framework can be used to test (at least) three types of web service clients Enunciate supports:</p>
 *
 * <ul>
 *   <li>REST clients</li>
 *   <li>JSON clients</li>
 *   <li>Java SOAP clients</li>
 * </ul>
 *
 * <p>To make this possible, the integration test environment leverages JUnit, HttpClient, JDOM, json.org, JXPath and EasyMock third party libraries.</p>
 *
 * <h2>REST Tests</h2>
 *
 * <p>REST endpoints can be obtained by calling <tt>getRestEndpoint(MY_WEB_SERVICE_NOUN)</tt>, appending any "proper nouns" or parameters your web service
 * needs to return a valid result, in this case we are requesting an object with the ID of '1'.  Using the <tt>parseXml</tt> and <tt>xPathValue</tt> helper
 * methods, you can write tests that look like this:</p>
 *
 * <code class="console>
 * &#064;Test
 * public void testRestGetOwnerById() throws Exception {
 * 	GetMethod method = new GetMethod(getRestEndpoint(MY_WEB_SERVICE_NOUN)+"/1");
 * 	try {
 * 		int statusCode = httpClient.executeMethod(method);
 * 		assertEquals("HttpStatus.SC_OK", HttpStatus.SC_OK, statusCode);
 * 		Document xml = parseXml(method.getResponseBodyAsStream());
 * 		//new XMLOutputter().output(xml, System.out);  //prints result to console
 * 		assertNotNull("body read successfully", xml);
 * 		assertEquals("id==1", "1", xPathValue(xml, "/ns0:owner/&#064;id"));
 * 		assertEquals("firstName==Sally", "Sally", xPathValue(xml, "/ns0:owner/firstName"));
 * 	} finally {
 * 		method.releaseConnection();
 * 	}
 * }
 * </code>
 *
 * <p>Although, you could test your result using string methods, more robust tests can be written using
 *  parsed XML and XPath.  XPath also allows very concise, readable assert statements.
 *  Most of the time you'll just call the <tt>xPathValue</tt> method to get the result as a string, but
 *  if this doesn't meet your needs, you can call <tt>xPathNode</tt> to get the JDOM node directly.</p>
 *
 * <p>For this example, the XML result is:</p>
 *
 * <code class="console>
 * &gt;ns0:owner xmlns:ns0="http://enunciate.codehaus.org/petclinic" id="1"&lt;
 *   &gt;address&lt;3 Third Street, Suite 0&gt;/address&lt;
 *   &gt;city&lt;Orlando, FL&gt;/city&lt;
 *   &gt;firstName&lt;Sally&gt;/firstName&lt;
 *   &gt;lastName&lt;Gates&gt;/lastName&lt;
 *   &gt;telephone&lt;111-1111, x0&gt;/telephone&lt;
 *   &gt;petIds&lt;&gt;petId&lt;16&gt;/petId&lt;&gt;/petIds&lt;
 * &gt;/ns0:owner&lt;
 * </code>
 *
 * <h2>JSON Tests</h2>
 *
 * <p>The JSON endpoint is obtained by calling <tt>getJsonEndpoint(MY_WEB_SERVICE_NOUN)</tt>:</p>
 *
 * <code class="console>
 * &#064;Test
 * public void testJsonGetOwnerById() throws Exception {
 * 	GetMethod method = new GetMethod(getJsonEndpoint(MY_WEB_SERVICE_NOUN)+"/1");
 * 	try {
 * 		int statusCode = httpClient.executeMethod(method);
 * 		assertEquals("HttpStatus.SC_OK", HttpStatus.SC_OK, statusCode);
 * 		String json = method.getResponseBodyAsString();
 * 		logger.info(json);
 * 		JSONObject jsonObj = new JSONObject(json);
 * 		JXPathContext context = JXPathContext.newContext(jsonObj);
 * 		assertEquals("id==1", "1", context.getValue("./ns0.owner[&#064;name='&#064;id']"));
 * 		assertEquals("firstName==Sally", "Sally", context.getValue("./ns0.owner/firstName"));
 * 	} finally {
 * 		method.releaseConnection();
 * 	}
 * </code>
 *
 * <p>Using JXPath adds a bit of complexity but allows the tests to be less fragile.  First
 *  you parse the JSON using <tt>new JSONObject(json)</tt>, then it is added to the JXPath
 *  using <tt>JXPathContext.newContext(jsonObj)</tt> to create a context. Lastly, the context is
 *  used to pull values out of the object using an XPath-like syntax.</p>
 *
 * <p>For this example, the JSON returned is:</p>
 *
 * <code class="console>
 * {"ns0.owner":
 * 	{
 * 		"&#064;id":"1",
 * 		"address":"3 Third Street, Suite 0",
 * 		"city":"Orlando, FL",
 * 		"firstName":"Sally",
 * 		"lastName":"Gates",
 * 		"telephone":"111-1111, x0",
 * 		"petIds":{"petId":16}
 *  }
 * }
 * </code>
 *
 * <h2>Java SOAP Tests</h2>
 *
 * <p> SOAP tests are strait forward because they are just standard JUnit Java tests - you
 *  don't need to know or care about the XML SOAP envelopes.  These tests use the generated Java
 *  client code.  All you have to do is pass the SOAP endpoint to the client constructor:</p>
 *
 * <code class="console>
 * &#064;Test
 * public void testGetUploadMetaDataSoap() throws Exception {
 * 	UploadedFile uploadedFile = new UploadedFile(99, "myFile.txt");
 * 	UploadService uploadedFileService = new UploadServiceImpl(getSoapEndpoint("UploadServiceService"));
 *
 * 	UploadMetaData metaData = uploadedFileService.readUploadMetaData(uploadedFile.getId().toString());
 *
 * 	assertEquals("check id", uploadedFile.getId().getIdValue(), metaData.getUploadedFileId());
 * 	assertEquals("check filename", uploadedFile.getFileName(), metaData.getFileName());
 * }
 * </code>
 *
 * <p>Because the client code is generated by Enunciate and XFire, it won't exist until after your first
 * Maven build.  The <tt>maven-enunciate-plugin</tt> is configured to add the generated source to your test
 * classpath, thereafter you can develop in Eclipse without class-not-found errors.</p>
 *
 * <h1>Using Easy Mock</h1>
 *
 * <p>If you rely on server-side components like Session EJBs, they will not be present in the Jetty environment.
 *  Using mock objects for your session and manager beans avoids this problem and makes your tests more agile.</p>
 *
 * <p>Mock objects are simply created by calling EasyMock's <tt>createMock</tt> method passing in your interface.
 *  Then you use the <tt>getWebContextBean</tt> helper method to pluck your service instance out of the web context.
 *  Finally, you can replace the real EJB instance with your mock instance on the service:</p>
 *
 * <code class="console>
 * &#064;Before
 * public void setUp() throws Exception {
 * 	mockUploadFileManager = createMock(UploadFileManager.class);
 * 	uploadService = (com.garmin.upload.api.impl.UploadServiceImpl) getWebContextBean("uploadService");
 * 	uploadService.setUploadFileManager(mockUploadFileManager);
 * }
 * </code>
 *
 * <p>Notice we are woring with the server-side code here, not the generated Enunciate code used in the
 * client-side tests.</p>
 *
 * <p>Next you tell EasyMock what service methods you'll be hitting and what to return, terminated
 *  by the <tt>replay</tt> method.  Then you invoke the client service and test the results in standard
 *  JUnit assert statements:</p>
 *
 * <code class="console>
 * &#064;Test
 * public void testGetUploadMetaDataSoap() throws Exception {
 * 	expect(mockUploadFileManager.findUploadedFileById(99)).andReturn(uploadedFile);
 * 	replay(mockUploadFileManager);
 *
 * 	UploadMetaData metaData = uploadedFileService.readUploadMetaData(uploadedFile.getId().toString());
 *
 * 	assertEquals("check id", uploadedFile.getId().getIdValue(), metaData.getUploadedFileId());
 * 	assertEquals("check filename", uploadedFile.getFileName(), metaData.getFileName());
 * }
 * </code>
 *
 * <p>The one missing detail is how to get our hands on the service instance.  You need to tell Spring what
 * to name the "bean" so we can look it up using <tt>getWebContextBean</tt>.  This is done using standard
 * Spring configuration, in this example, in a file named <tt>src/main/config/service-beans.xml</tt>:</p>
 *
 * <code class="console>
 * &gt;beans&lt;
 *   &gt;bean id="uploadService" class="com.garmin.upload.api.impl.UploadServiceImpl"/&lt;
 * &gt;/beans&lt;
 * </code>
 *
 * <p>Then using the <tt>springImport</tt> hook provided by Enunciate, we can pull this file into the Spring context:</p>
 *
 * <code class="console>
 * &gt;enunciate label="upload-service"&lt;
 * 	&gt;modules&lt;
 * 		&gt;spring-app&lt;
 * 			&gt;springImport file="service-beans.xml"/&lt;
 *   ...
 * </code>
 *
 * @author reaster
 * @docFileName test_integration.html
 */
public class GenericEnunciateTestCase extends GenericWebServerTestCase {

	public final static String SOAP = "/soap";

	public final static String REST = "/rest";

	public final static String JSON = "/json";

	protected static SAXBuilder documentBuilder;

	protected static XPath xPath;

	protected static Map<String, String> defaultNamespaceContext = new HashMap<String, String>();

	/**
	 * Start web server and initialize test environment. Should be called once
	 * BEFORE all tests are run.
	 * 
	 * @deprecated use
	 *             {@link #startServer(org.codehaus.enunciate.test.integration.GenericWebServerTestCase.Initializer)}
	 *             instead
	 */
	@Deprecated
	public static void setUpBeforeClass() throws Exception {
		startServer(null);
	}

	/**
	 * Used for starting the server
	 * 
	 * @see GenericWebServerTestCase#startServer(org.codehaus.enunciate.test.integration.GenericWebServerTestCase.Initializer)
	 */
	public static void startServer(Initializer initializer) throws Exception {
		GenericWebServerTestCase.startServer(initializer);
		JXPathIntrospector.registerDynamicClass(JSONObject.class, JSONObjectJXPathHandler.class);
	}

	/**
	 * Shutdown web server and cleanup test environment. Should be called once
	 * AFTER all tests are run.
	 * 
	 * @deprecated use {@link #stopServer()} instead
	 */
	@Deprecated
	public static void tearDownAfterClass() {
		stopServer();
	}

	/**
	 * Stops the server if it's running.
	 * 
	 * @see GenericWebServerTestCase#stopServer()
	 */
	public static void stopServer() {
		GenericWebServerTestCase.stopServer();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Enunciate-specific methods
	// //////////////////////////////////////////////////////////////////////////

	/** return SOAP WS endpoint given the name of the web service. */
	public static String getSoapEndpoint(String webServiceName) {
		return getBaseContextAsString() + SOAP + "/" + webServiceName;
	}

	/** return REST WS endpoint given a REST noun. */
	public static String getRestEndpoint(String noun) {
		return getBaseContextAsString() + REST + "/" + noun + "/";
	}

	/** return JSON WS endpoint given a REST noun. */
	public static String getJsonEndpoint(String noun) {
		return getBaseContextAsString() + JSON + "/" + noun + "/";
	}

	// static Object getServiceImplementation(Class clientServiceClass, Class
	// clientServiceInterface) throws IllegalAccessException,
	// InstantiationException {
	// Object obj = getWebContextBean("enunciateServiceFactory");
	// SpringAppServiceFactory factory = (SpringAppServiceFactory)obj;
	// Object serviceInstance = factory.getInstance(clientServiceClass,
	// clientServiceInterface);
	// return serviceInstance;
	// }

	/**
	 * Given registered bean name, get the bean instance from Spring's
	 * WebApplicationContext.
	 * 
	 * @param registered
	 *            bean name
	 * @return returns single result or null
	 * @throws org.springframework.beans.BeansException
	 */
	public static Object getWebContextBean(String beanName) {
		WebAppContext jettyWebAppContext = (WebAppContext) getJettyServer().getHandler();
		ServletContext servletContext = jettyWebAppContext.getServletHandler().getServletContext();
		WebApplicationContext springWebAppContext = WebApplicationContextUtils
				.getRequiredWebApplicationContext(servletContext);
		return springWebAppContext.getBean(beanName);
	}

	// //////////////////////////////////////////////////////////////////////////
	// JXPath methods
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * Pull a single value out of a JSON result using JXPath.
	 * 
	 * @param jsonObj
	 *            a JSONObject containing parsed JSON object
	 * @param jxpath
	 *            JXPath query
	 * @return returns single result or null
	 */
	public static Object jXPathValue(JSONObject jsonObj, String jxpath) {
		JXPathContext context = JXPathContext.newContext(jsonObj);
		Object value = context.getValue(jxpath);
		return value;
	}

	/**
	 * Pull a single value out of a JSON result using JXPath.
	 * 
	 * @param json
	 *            unparsed JSON string
	 * @param jxpath
	 *            JXPath query
	 * @return returns single result or null
	 * @throws JSONException
	 *             json text is not a valid JSON object
	 */
	public static Object jXPathValue(String json, String jxpath) throws JSONException {
		return jXPathValue(new JSONObject(json), jxpath);
	}

	// //////////////////////////////////////////////////////////////////////////
	// XPath methods
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * Find single element in the XML document referenced by XPath expression.
	 * 
	 * @param xml
	 *            XML document as string
	 * @param xpathExpr
	 *            XPath expression
	 * @return a JDOM object: Element, Attribute, Text, etc.
	 * @throws IOException
	 * @throws JDOMException
	 */
	public static Object xPathNode(String xml, String xpathExpr) throws IOException, JDOMException {
		Document document = parseXml(xml);
		return xPathNode(document, xpathExpr);
	}

	/**
	 * Find single element in the XML document referenced by XPath expression.
	 * 
	 * @param document
	 *            XML document to search in
	 * @param xpathExpr
	 *            XPath expression
	 * @return a JDOM object: Element, Attribute, Text, etc.
	 * @throws JDOMException
	 */
	public static Object xPathNode(Document document, String xpathExpr) throws JDOMException {
		XPath xpath = getXPath(xpathExpr);
		return xpath.selectSingleNode(document);
	}

	/**
	 * Find single element in the XML document referenced by XPath expression.
	 * 
	 * @param xmlInputStream
	 *            XML document to search in
	 * @param xpathExpr
	 *            XPath expression
	 * @return a JDOM object: Element, Attribute, Text, etc.
	 * @throws JDOMException
	 */
	public static Object xPathNode(InputStream xmlInputStream, String xpathExpr) throws IOException, JDOMException {
		try {
			Document document = parseXml(xmlInputStream);
			return xPathNode(document, xpathExpr);
		} finally {
			try {
				xmlInputStream.close();
			} catch (Exception e) {
				warn("%s on closing xml stream", e.getMessage());
			}
		}
	}

	/**
	 * Find single element value in the XML document referenced by XPath
	 * expression.
	 * 
	 * @param xml
	 *            XML document as string
	 * @param xpathExpr
	 *            XPath expression
	 * @return The string value of a JDOM Element, Attribute, Text object or the
	 *         value of the first element in a collection.
	 * @throws IOException
	 * @throws JDOMException
	 */
	public static String xPathValue(String xml, String xpathExpr) throws IOException, JDOMException {
		return nodeToString(xPathNode(xml, xpathExpr));
	}

	/**
	 * Find single element value in the XML document referenced by XPath
	 * expression.
	 * 
	 * @param xml
	 *            XML document as InputStream
	 * @param xpathExpr
	 *            XPath expression
	 * @return The string value of a JDOM Element, Attribute, Text object or the
	 *         value of the first element in a collection.
	 * @throws IOException
	 * @throws JDOMException
	 */
	public static String xPathValue(InputStream xmlInputStream, String xpathExpr) throws IOException, JDOMException {
		return nodeToString(xPathNode(xmlInputStream, xpathExpr));
	}

	/**
	 * Find single element value in the XML document referenced by XPath
	 * expression.
	 * 
	 * @param xml
	 *            XML document
	 * @param xpathExpr
	 *            XPath expression
	 * @return The string value of a JDOM Element, Attribute, Text object or the
	 *         value of the first element in a collection.
	 * @throws JDOMException
	 */
	public static String xPathValue(Document document, String xpathExpr) throws JDOMException {
		return nodeToString(xPathNode(document, xpathExpr));
	}

	/**
	 * Given any JDOM node, return it's string value.
	 * 
	 * @param jdomNode
	 *            any JDOM object like Element, Attribute, Text, etc.
	 * @return string value of JDOM Element, Attribute, Text object or the value
	 *         of the first element in a collection.
	 */
	protected static String nodeToString(Object jdomNode) {
		if (jdomNode == null) {
			return null;
		} else if (jdomNode instanceof Element) {
			Element e = (Element) jdomNode;
			return e.getText();
		} else if (jdomNode instanceof Attribute) {
			Attribute a = (Attribute) jdomNode;
			return a.getValue();
		} else if (jdomNode instanceof Text) {
			Text t = (Text) jdomNode;
			return t.getValue();
		} else if (jdomNode instanceof Parent) {
			Parent p = (Parent) jdomNode;
			if (p.getContentSize() == 0) {
				return p.toString();
			}
			return nodeToString(p.getContent().get(0));
		} else {
			return jdomNode.toString();
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// XML support methods
	// //////////////////////////////////////////////////////////////////////////

	/** Return a XML Document given a valid XML InputStream. */
	public static Document parseXml(InputStream xmlInputStream) throws IOException, JDOMException {
		Document document = getDocumentBuilder().build(xmlInputStream);
		return document;
	}

	/** Return a XML Document given a valid XML string. */
	public static Document parseXml(String xml) throws IOException, JDOMException {
		Document document = getDocumentBuilder().build(xml);
		return document;
	}

	/** Return shared SAXBUilder static instance. */
	public static SAXBuilder getDocumentBuilder() {
		if (documentBuilder == null) {
			documentBuilder = new SAXBuilder();
		}
		return documentBuilder;
	}

	// public static DocumentBuilder getDocumentBuilder() throws
	// ParserConfigurationException {
	// if (documentBuilder == null) {
	// documentBuilder =
	// DocumentBuilderFactory.newInstance().newDocumentBuilder();
	// }
	// return documentBuilder;
	// }

	/**
	 * Returns XPath processor with parsed XPath expression. If default
	 * namespaces have been specified, the are automatically added the the XPath
	 * namespace context.
	 */
	public static XPath getXPath(String expression) throws JDOMException {
		XPath xpath = XPath.newInstance(expression);
		if (defaultNamespaceContext != null) {
			for (String prefix : defaultNamespaceContext.keySet()) {
				xpath.addNamespace(prefix, defaultNamespaceContext.get(prefix));
			}
		}
		return xpath;
	}

	/**
	 * Add default prefix-namespace pairs to XPath processor.
	 * 
	 * @param strings
	 *            any number of namespace prefix - namespace URL pairs
	 */
	public static void addDefaultNamespaces(String... strings) {
		if (strings.length % 2 != 0)
			throw new IllegalStateException("prefix-namespaces must come in matched pairs: " + strings);
		for (int i = 0; i < strings.length; i += 2) {
			String prefix = strings[i];
			String ns = strings[i + 1];
			defaultNamespaceContext.put(prefix, ns);
		}
	}

}