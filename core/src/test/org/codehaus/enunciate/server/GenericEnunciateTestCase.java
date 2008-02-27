package org.codehaus.enunciate.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathIntrospector;
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

import org.codehaus.enunciate.server.util.JSONObjectJXPathHandler;

/**
 * Extends GenericWebServerTestCase to provide web service-specific testing.
 * 
 * @author reaster
 */
public class GenericEnunciateTestCase extends GenericWebServerTestCase {
	
	public final static String SOAP = "/soap";
	public final static String REST = "/rest";
	public final static String JSON = "/json";
	
	//protected static DocumentBuilder documentBuilder;
	protected static SAXBuilder documentBuilder;
	
	protected static XPath xPath;
	
	protected static Map<String, String> defaultNamespaceContext = new HashMap<String, String>();
	
	/**
	 * Start web server and initialize test environment.  Should be called once BEFORE all tests are run.
	 * @throws Exception
	 */
	//@org.junit.AfterClass
	public static void setUpBeforeClass() throws Exception {
		GenericWebServerTestCase.setUpBeforeClass();
		JXPathIntrospector.registerDynamicClass(JSONObject.class, JSONObjectJXPathHandler.class);
	}

	/**
	 * Shutdown web server and cleanup test environment.  Should be called once AFTER all tests are run.
	 * @throws Exception
	 */
	//@org.junit.AfterClass
	public static void tearDownAfterClass() {
		GenericWebServerTestCase.tearDownAfterClass();
	}

	////////////////////////////////////////////////////////////////////////////
	// Enunciate-specific methods
	////////////////////////////////////////////////////////////////////////////
	
	/** return SOAP WS endpoint given the name of the web service. */
	public static String getSoapEndpoint(String webServiceName) {
		return getBaseContextAsString() + SOAP + "/" + webServiceName;
	}
	
	/** return REST WS endpoint given a REST noun. */
	public static String getRestEndpoint(String noun) {
		return getBaseContextAsString() + REST + "/" + noun;
	}

	/** return JSON WS endpoint given a REST noun. */
	public static String getJsonEndpoint(String noun) {
		return getBaseContextAsString() + JSON + "/" + noun;
	}

//	static Object getServiceImplementation(Class clientServiceClass, Class clientServiceInterface) throws IllegalAccessException, InstantiationException {
//		Object obj = getWebContextBean("enunciateServiceFactory");
//		SpringAppServiceFactory factory = (SpringAppServiceFactory)obj;
//		Object serviceInstance = factory.getInstance(clientServiceClass, clientServiceInterface);
//		return serviceInstance;
//	}
	
	/**
	 * Given registered bean name, get the bean instance from Spring's WebApplicationContext.
	 * @param registered bean name
	 * @return returns single result or null
	 * @throws org.springframework.beans.BeansException 
	 */
	public static Object getWebContextBean(String beanName) {
        WebAppContext jettyWebAppContext = (WebAppContext)getJettyServer().getHandler();
        ServletContext servletContext = jettyWebAppContext.getServletHandler().getServletContext();
        WebApplicationContext springWebAppContext =
            WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        return springWebAppContext.getBean(beanName);
    }

	////////////////////////////////////////////////////////////////////////////
	// JXPath methods
	////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Pull a single value out of a JSON result using JXPath.
	 * 
	 * @param jsonObj a JSONObject containing parsed JSON object
	 * @param jxpath JXPath query
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
	 * @param json unparsed JSON string
	 * @param jxpath JXPath query
	 * @return returns single result or null
	 * @throws JSONException json text is not a valid JSON object 
	 */
	public static Object jXPathValue(String json, String jxpath) throws JSONException {
		return jXPathValue(new JSONObject(json), jxpath);
	}

	////////////////////////////////////////////////////////////////////////////
	// XPath methods
	////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Find single element in the XML document referenced by XPath expression.
	 * @param xml XML document as string
	 * @param xpathExpr XPath expression
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
	 * @param document XML document to search in
	 * @param xpathExpr XPath expression
	 * @return a JDOM object: Element, Attribute, Text, etc.
	 * @throws JDOMException
	 */
	public static Object xPathNode(Document document, String xpathExpr) throws JDOMException {
		XPath xpath = getXPath(xpathExpr);
		return xpath.selectSingleNode(document);
	}

	/**
	 * Find single element in the XML document referenced by XPath expression.
	 * @param xmlInputStream XML document to search in
	 * @param xpathExpr XPath expression
	 * @return a JDOM object: Element, Attribute, Text, etc.
	 * @throws JDOMException
	 */
	public static Object xPathNode(InputStream xmlInputStream, String xpathExpr) throws IOException, JDOMException {
		try {
			Document document = parseXml(xmlInputStream);
			return xPathNode(document, xpathExpr);
		} finally {
			try { xmlInputStream.close(); } catch (Exception e) { warn("%s on closing xml stream", e.getMessage()); }
		}
	}

	/**
	 * Find single element value in the XML document referenced by XPath expression.
	 * @param xml XML document as string
	 * @param xpathExpr XPath expression
	 * @return The string value of a JDOM Element, Attribute, Text object or the value of the first element in a collection.
	 * @throws IOException
	 * @throws JDOMException
	 */
	public static String xPathValue(String xml, String xpathExpr) throws IOException, JDOMException {
		return nodeToString(xPathNode(xml, xpathExpr));
	}			

	/**
	 * Find single element value in the XML document referenced by XPath expression.
	 * @param xml XML document as InputStream
	 * @param xpathExpr XPath expression
	 * @return The string value of a JDOM Element, Attribute, Text object or the value of the first element in a collection.
	 * @throws IOException
	 * @throws JDOMException
	 */
	public static String xPathValue(InputStream xmlInputStream, String xpathExpr) throws IOException, JDOMException  {
		return nodeToString(xPathNode(xmlInputStream, xpathExpr));
	}

	/**
	 * Find single element value in the XML document referenced by XPath expression.
	 * @param xml XML document
	 * @param xpathExpr XPath expression
	 * @return The string value of a JDOM Element, Attribute, Text object or the value of the first element in a collection.
	 * @throws IOException
	 * @throws JDOMException
	 */
	public static String xPathValue(Document document, String xpathExpr) throws IOException, JDOMException  {
		return nodeToString(xPathNode(document, xpathExpr));
	}

	/**
	 * Given any JDOM node, return it's string value.
	 * @param jdomNode any JDOM object like Element, Attribute, Text, etc.
	 * @return string value of JDOM Element, Attribute, Text object or the value of the first element in a collection.
	 */
	protected static String nodeToString(Object jdomNode) {
		if (jdomNode == null) {
			return null;
		} else if (jdomNode instanceof Element) {
			Element e = (Element)jdomNode;
			return e.getText();
		} else if (jdomNode instanceof Attribute) {
			Attribute a = (Attribute)jdomNode;
			return a.getValue();
		} else if (jdomNode instanceof Text) {
			Text t = (Text)jdomNode;
			return t.getValue();
		} else if (jdomNode instanceof Parent) {
			Parent p = (Parent)jdomNode;
			if (p.getContentSize()==0) {
				return p.toString();
			} else {
				return nodeToString( p.getContent().get(0) );
			}
		} else {
			return jdomNode.toString();
		}
	}			

	////////////////////////////////////////////////////////////////////////////
	// XML support methods
	////////////////////////////////////////////////////////////////////////////
	
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

//	public static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
//		if (documentBuilder == null) {
//			documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//		}
//		return documentBuilder;
//	}

	/**
	 * Returns XPath processor with parsed XPath expression.  If default namespaces have been specified, 
	 * the are automatically added the the XPath namespace context.
	 */
	public static XPath getXPath(String expression) throws JDOMException {
		XPath xpath = XPath.newInstance(expression);
		if (defaultNamespaceContext != null) {
			for(String prefix: defaultNamespaceContext.keySet()) {
				xpath.addNamespace(prefix, defaultNamespaceContext.get(prefix));
			}
		}
		return xpath;
	}
	
	/**
	 * Add default prefix-namespace pairs to XPath processor.
	 * @param strings any number of namespace prefix - namespace URL pairs
	 */
	public static void addDefaultNamespaces(String...strings) {
		if (strings.length % 2 != 0)
			throw new IllegalStateException("prefix-namespaces must come in matched pairs: "+strings);
		for(int i = 0; i<strings.length; i+=2) {
			String prefix = strings[i];
			String ns = strings[i+1];
			defaultNamespaceContext.put(prefix, ns);
		}
	}
	
}