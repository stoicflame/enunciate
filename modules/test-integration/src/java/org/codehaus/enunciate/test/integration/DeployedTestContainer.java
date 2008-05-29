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
 * @author holmes
 * 
 */
public abstract class DeployedTestContainer {

	private static DeployedTestContainer instance;
	
	public static final String DEFAULT_PROPERTIES_PATH = "./target/test-classes/jetty.properties";

	protected String protocol = "http";

	protected String host = "localhost";

	protected String soapSubContext = "/soap";

	protected String restSubContext = "/rest";

	protected String jsonSubContext = "/json";

	protected String relativeContext;

	protected Integer specifiedPort;

	protected Properties properties;

	protected URL baseContext;

	public DeployedTestContainer() {
		this(DEFAULT_PROPERTIES_PATH);
	}

	/**
	 * @param propertiesPath
	 *            to load the properties file from
	 */
	public DeployedTestContainer(String propertiesPath) {
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

	public ContextProperties getContextProperties() {
		final ContextProperties contextProperties = new ContextProperties();
		contextProperties.setBaseContext(baseContext);
		contextProperties.setHost(host);
		contextProperties.setJsonSubContext(jsonSubContext);
		contextProperties.setProtocol(protocol);
		contextProperties.setRelativeContext(relativeContext);
		contextProperties.setRestSubContext(restSubContext);
		contextProperties.setSoapSubContext(soapSubContext);
		contextProperties.setSpecifiedPort(specifiedPort);
		
		return contextProperties;
	}

	/**
	 * @return the property name to lookup for retrieving the relative context
	 *         from the properties file (hint, should return context, or
	 *         deployedContext)
	 * 
	 */
	protected abstract String getContextPropertyName();

	/**
	 * @return the property name to lookup for retrieving the port that the
	 *         application is deployed on
	 */
	protected abstract String getPortPropertyName();

	public static DeployedTestContainer getInstance() {
		return instance;
	}

	public static void setInstance(DeployedTestContainer instance) {
		DeployedTestContainer.instance = instance;
	}
}
