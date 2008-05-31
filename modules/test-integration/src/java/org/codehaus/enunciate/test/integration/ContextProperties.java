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
import java.net.URL;
import java.util.Properties;

import org.codehaus.enunciate.test.integration.util.FileUtil;

/**
 * The default set of Properties for a deployed Enunciate Service
 * 
 * @author holmes
 */
public class ContextProperties {

	public static final String DEFAULT_PROPERTIES_PATH = "./target/test-classes/jetty.properties";

	public static final String CONTEXT_PROPERTY_NAME = "deployedContext";

	public static final String PORT_PROPERTY_NAME = "deployedPort";

	protected String protocol = "http";

	protected String host = "localhost";

	protected String soapSubContext = "/soap";

	protected String restSubContext = "/rest";

	protected String jsonSubContext = "/json";

	protected String relativeContext;

	protected Integer specifiedPort;

	protected Properties properties;

	protected URL baseContext;

	public ContextProperties() {
		this(DEFAULT_PROPERTIES_PATH);
	}

	/**
	 * @param propertiesPath
	 *            to load the properties file from
	 */
	public ContextProperties(String propertiesPath) {
		try {
			loadProperties(propertiesPath);
			baseContext = new URL(protocol, host, specifiedPort, relativeContext);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void loadProperties(String propertiesPath) throws IOException, IllegalStateException {
		properties = FileUtil.loadAndVerifyJettyPropertiesFile(propertiesPath, false);

		relativeContext = properties.getProperty(getContextPropertyName());
		if (relativeContext == null) {
			throw new IllegalStateException("Required property '" + getContextPropertyName() + "' not found in "
					+ propertiesPath);
		}

		final String port = properties.getProperty(getPortPropertyName());
		if (port == null) {
			throw new IllegalStateException("Required property '" + getPortPropertyName() + "' not found in "
					+ propertiesPath);
		}
		specifiedPort = Integer.parseInt(port);

		host = properties.getProperty("host", host);
		protocol = properties.getProperty("protocol", protocol);
		soapSubContext = properties.getProperty("soapSubContext", soapSubContext);
		restSubContext = properties.getProperty("restSubContext", restSubContext);
		jsonSubContext = properties.getProperty("jsonSubContext", jsonSubContext);
	}

	/**
	 * @return the property name to lookup for retrieving the relative context
	 *         from the properties file (hint, should return context, or
	 *         deployedContext)
	 * 
	 */
	protected String getContextPropertyName() {
		return CONTEXT_PROPERTY_NAME;
	}

	/**
	 * @return the property name to lookup for retrieving the port that the
	 *         application is deployed on
	 */
	protected String getPortPropertyName() {
		return PORT_PROPERTY_NAME;
	}

	public String getProtocol() {
		return this.protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getSoapSubContext() {
		return this.soapSubContext;
	}

	public void setSoapSubContext(String soapSubContext) {
		this.soapSubContext = soapSubContext;
	}

	public String getRestSubContext() {
		return this.restSubContext;
	}

	public void setRestSubContext(String restSubContext) {
		this.restSubContext = restSubContext;
	}

	public String getJsonSubContext() {
		return this.jsonSubContext;
	}

	public void setJsonSubContext(String jsonSubContext) {
		this.jsonSubContext = jsonSubContext;
	}

	public String getRelativeContext() {
		return this.relativeContext;
	}

	public void setRelativeContext(String relativeContext) {
		this.relativeContext = relativeContext;
	}

	public Integer getSpecifiedPort() {
		return this.specifiedPort;
	}

	public void setSpecifiedPort(Integer specifiedPort) {
		this.specifiedPort = specifiedPort;
	}

	public Properties getProperties() {
		return this.properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public URL getBaseContext() {
		return this.baseContext;
	}

	public void setBaseContext(URL baseContext) {
		this.baseContext = baseContext;
	}
}
