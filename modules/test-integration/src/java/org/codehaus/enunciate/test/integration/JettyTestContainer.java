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

import javax.servlet.ServletContext;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Runs Jetty with properties specified in the
 * {@link JettyTestContainer.Initializer}
 * 
 * Treated as a Singleton because you really don't want to be running many of
 * these.
 * 
 * @author holmes
 */
public class JettyTestContainer {
	private static boolean runningMultiple = Boolean.getBoolean("runningMultiple");

	private static JettyTestContainer instance;

	protected Server jettyServer = null;

	private JettyTestContainer() {
		// enforce singleton
	}

	/**
	 * Starts the Jetty container. You will want to call this method from your
	 * BeforeClass methods.
	 * 
	 * If the container has already been started, this will just return as
	 * opposed to attempting to start it again
	 * 
	 * @param options
	 *            that you would like the {@link JettyTest} to run before
	 *            starting up the Jetty server. This can be more or less
	 *            anything want to do from a static method
	 */
	public Integer startServer(Initializer options) throws Exception {
		if (!isRunningContainer()) {
			options.initialize();

			WebAppContext context = new WebAppContext(options.getWebAppHome().getAbsolutePath(), options.getContext());
			context.setParentLoaderPriority(true);

			jettyServer = new Server(options.getPort());
			jettyServer.addHandler(context);
			jettyServer.start();
		}

		return jettyServer.getConnectors()[0].getLocalPort();
	}

	/**
	 * Attempts to shut down the Jetty Container.
	 * 
	 * Only shuts down if we're not running from maven, and if the container has
	 * previously been started.
	 * 
	 */
	public void stopServer() {
		if (isRunningContainer() && !runningMultiple) {
			if (jettyServer != null) {
				try {
					if (jettyServer.isRunning()) {
						jettyServer.stop();
					}
				} catch (Exception e) {
				} finally {
					jettyServer = null;
				}
			}
		}
	}

	/**
	 * @return is the container currently running?
	 */
	public boolean isRunningContainer() {
		return getJettyServer() != null;
	}

	/**
	 * Provides access to Jetty instance.
	 */
	public Server getJettyServer() {
		return jettyServer;
	}

	/**
	 * Given registered bean name, get the bean instance from Spring's
	 * WebApplicationContext.
	 * 
	 * @param registered
	 *            bean name
	 * @return returns single result or null
	 * @throws org.springframework.beans.BeansException
	 */
	public Object getWebContextBean(String beanName) {
		return getWebApplicationContext().getBean(beanName);
	}

	public WebApplicationContext getWebApplicationContext() {
		WebAppContext jettyWebAppContext = (WebAppContext) getJettyServer().getHandler();
		ServletContext servletContext = jettyWebAppContext.getServletHandler().getServletContext();
		WebApplicationContext springWebAppContext = WebApplicationContextUtils
				.getRequiredWebApplicationContext(servletContext);
		return springWebAppContext;
	}

	/**
	 * If you would like to start the Jetty Container, but pass things to it
	 * before it starts up, put those initialization options in here
	 * 
	 * @author Jason Holmes
	 */
	public static class Initializer {
		private Integer port;

		private File webAppHome;

		private String context;

		public void initialize() {
			// fill in your stuff here
		}

		public Integer getPort() {
			return this.port;
		}

		public void setPort(Integer port) {
			this.port = port;
		}

		public File getWebAppHome() {
			return this.webAppHome;
		}

		public void setWebAppHome(File webAppHome) {
			this.webAppHome = webAppHome;
		}

		public String getContext() {
			return this.context;
		}

		public void setContext(String context) {
			this.context = context;
		}
	}

	public static JettyTestContainer getInstance() {
		if (instance == null) {
			instance = new JettyTestContainer();
		}

		return instance;
	}

	public static void setInstance(JettyTestContainer instance) {
		JettyTestContainer.instance = instance;
	};
}
