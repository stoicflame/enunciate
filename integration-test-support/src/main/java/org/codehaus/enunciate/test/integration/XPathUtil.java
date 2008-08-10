/*
 * Copyright (c) 2007 Garmin International. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * Garmin International.
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement
 * you entered into with Garmin International.
 *
 * Garmin International MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. Garmin International SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 */
package org.codehaus.enunciate.test.integration;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Parent;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

/**
 * @author holmes
 * 
 */
public class XPathUtil {
	protected Map<String, String> defaultNamespaceContext = new HashMap<String, String>();

	protected SAXBuilder documentBuilder;
	
	protected XPath xPath;

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
	public Object xPathNode(String xml, String xpathExpr) throws IOException, JDOMException {
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
	public Object xPathNode(Document document, String xpathExpr) throws JDOMException {
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
	public Object xPathNode(InputStream xmlInputStream, String xpathExpr) throws IOException, JDOMException {
		try {
			Document document = parseXml(xmlInputStream);
			return xPathNode(document, xpathExpr);
		} finally {
			try {
				xmlInputStream.close();
			} catch (Exception e) {
				e.printStackTrace(); //what else would you have me do?
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
	public String xPathValue(String xml, String xpathExpr) throws IOException, JDOMException {
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
	public String xPathValue(InputStream xmlInputStream, String xpathExpr) throws IOException, JDOMException {
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
	public String xPathValue(Document document, String xpathExpr) throws JDOMException {
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
	protected String nodeToString(Object jdomNode) {
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
	public Document parseXml(InputStream xmlInputStream) throws IOException, JDOMException {
		Document document = getDocumentBuilder().build(xmlInputStream);
		return document;
	}

	/** Return a XML Document given a valid XML string. */
	public Document parseXml(String xml) throws IOException, JDOMException {
		Document document = getDocumentBuilder().build(xml);
		return document;
	}

	/** Return shared SAXBUilder static instance. */
	public SAXBuilder getDocumentBuilder() {
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
	public XPath getXPath(String expression) throws JDOMException {
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
	public void addDefaultNamespaces(String... strings) {
		if (strings.length % 2 != 0)
			throw new IllegalStateException("prefix-namespaces must come in matched pairs: " + strings);
		for (int i = 0; i < strings.length; i += 2) {
			String prefix = strings[i];
			String ns = strings[i + 1];
			defaultNamespaceContext.put(prefix, ns);
		}
	}

}
