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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.BeforeClass;

/**
 * Extend this when you want to run tests against a deployed server (i.e. jboss)
 * 
 * If you want to run against Jetty, extend from {@link BaseEnunciateWsTest}
 * instead
 * 
 * @author holmes
 */
public class WsTestContainer extends DeployedTestContainer {

	public static final String CONTEXT_PROPERTY_NAME = "context";

	public static final String PORT_PROPERTY_NAME = "port";

	private static JettyTestContainer jettyTestContainer = new JettyTestContainer();

	protected File webappHome;

	@BeforeClass
	public static void startUpServer() {
		setInstance(new WsTestContainer());
	}

	@Override
	protected String getContextPropertyName() {
		return CONTEXT_PROPERTY_NAME;
	}

	@Override
	protected String getPortPropertyName() {
		return PORT_PROPERTY_NAME;
	}

	@Override
	protected void loadProperties(String propertiesPath) throws IOException, IllegalStateException {
		super.loadProperties(propertiesPath);

		final String webappDir = properties.getProperty("webapp.home");
		if (webappDir == null) {
			throw new IllegalStateException("Required property 'webapp.home' not found in " + propertiesPath);
		}

		webappHome = new File(webappDir);
		if (!webappHome.exists()) {
			throw new FileNotFoundException("The webapp.home does not exist: " + webappHome.getAbsolutePath());
		}

		startServer();
	}

	/**
	 * Directory where the expanded webapp is located.
	 */
	public File getWebappHome() {
		return webappHome;
	}

	public void setWebappHome(File webappHome) {
		this.webappHome = webappHome;
	}

	public static JettyTestContainer getJettyTestContainer() {
		return jettyTestContainer;
	}

	public static void setJettyTestContainer(JettyTestContainer testContainer) {
		jettyTestContainer = testContainer;
	}

	/**
	 * Starts the server up based on properties that have been loaded. Once the
	 * server starts, it tells us what port we're on, and we pass that on to the
	 * parent
	 */
	private void startServer() {
		final JettyTestContainer.Initializer initializer = new JettyTestContainer.Initializer();
		initializer.setContext(relativeContext);
		initializer.setPort(specifiedPort);
		initializer.setWebAppHome(webappHome);

		try {
			specifiedPort = jettyTestContainer.startServer(initializer);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
