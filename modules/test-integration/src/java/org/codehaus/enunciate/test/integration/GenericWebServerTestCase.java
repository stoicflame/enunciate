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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;
import org.codehaus.enunciate.test.integration.util.FileUtil;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * This class is responsible for starting and stopping the Jetty container in a
 * test environment. This is mainly used for for running lightweight integration
 * tests on webapps.
 * 
 * Ensure that -DrunningMultiple=true is available as a System property, and
 * this class will only start the container once as well as prevent it from
 * being stopped at the end of each Test Class.
 * 
 * @BeforeClass is not present on this class - you must have it in your
 *              subclass.
 * 
 * You start the container with a call to
 * {@link #startServer(org.codehaus.enunciate.test.integration.JettyTest.Initializer)}
 * and pass it an optional {@link JettyTest.Initializer} to initialize with any
 * additional options.
 * 
 * You will most likely include calls in your annotated classes like so ...
 * @BeforeClass {@link #startServer(org.codehaus.enunciate.test.integration.GenericWebServerTestCase.Initializer)}
 * @AfterClass {@link #stopServer()}
 * 
 * Your sub-class will have a method that looks much like this...
 * 
 * <pre>
 * &#064;BeforeClass
 * public static void startServer() throws Exception {
 * 	JettyTest.startServer(new JettyTest.Initializer() {
 * 		&#064;Override
 * 		public void initialize() {
 * 			HttpUnitOptions.setLoggingHttpHeaders(true);
 * 			GenericEnunciateTestCase.setPropertyFileLocation(&quot;src/test/resources/jetty.properties&quot;);
 * 		}
 * 	});
 * }
 * </pre>
 * 
 * {@link #setUpBeforeClass()} has been deprecated in favor of the new
 * {@link #startServer(org.codehaus.enunciate.test.integration.GenericWebServerTestCase.Initializer)}
 * 
 * A shared Apache Commons HttpClient instance is provided and tests can
 * retrieve the webapp's full URL by calling getBaseContextAsString():
 * 
 * <pre>
 * public void testServiceIsRunning() throws Exception {
 * 	GetMethod method = new GetMethod(GenericWebServerTestCase.getBaseContextAsString());
 * 	try {
 * 		int statusCode = GenericWebServerTestCase.getHttpClient().executeMethod(method);
 * 		assertEquals(&quot;HttpStatus.SC_OK&quot;, HttpStatus.SC_OK, statusCode);
 * 	} finally {
 * 		method.releaseConnection();
 * 	}
 * }
 * </pre>
 * 
 * Minimally the relativeContext and webappHome properties must be set before
 * calling setUpBeforeClass(). You can handle this in the Initializer...
 * 
 * <pre>
 * @BeforeClass
 * public static void startServer() throws Exception {
 * 	JettyTest.startServer(new JettyTest.Initializer() {
 * 		&#064;Override
 * 		public void initialize() {
 *  		GenericWebServerTestCase.setRelativeContext(&quot;/my-webapp&quot;);
 * 			GenericWebServerTestCase.setWebappHome(&quot;target/my-webapp&quot;);
 * 			GenericWebServerTestCase.setUpBeforeClass();
 * 		}
 * 	});
 * </pre>
 * 
 * Alternatively, configuration can be loaded from a properties file. The
 * default, Maven-friendly location of the 'jetty.properties' file is the
 * './target/test-classes/jetty.properties', but the location can be changed at
 * startup:
 * 
 * <pre>
 * @BeforeClass
 * public static void startServer() throws Exception {
 * 	JettyTest.startServer(new JettyTest.Initializer() {
 * 		&#064;Override
 * 		public void initialize() {
 * 			GenericEnunciateTestCase.setPropertyFileLocation(&quot;src/test/resources/jetty.properties&quot;);
 *  		GenericWebServerTestCase.setRelativeContext(&quot;/my-webapp&quot;);
 * 			GenericWebServerTestCase.setWebappHome(&quot;target/my-webapp&quot;);
 * 			GenericWebServerTestCase.setUpBeforeClass();
 * 		}
 * 	});
 * </pre>
 * 
 * One motivation for supporting property file configuration is to allow
 * filtering by Maven. Here is how that might look:
 * 
 * <pre>
 * context=/${project.build.finalName}
 * webapp.home=${project.build.directory}/${project.build.finalName}
 * host=localhost
 * protocol=http
 * port=0
 * </pre>
 * 
 * The default port is <code>0</code> which tells the web server to pick an
 * arbitrary port. The actual port used is available from the relevant static
 * property methods, such as <code>getActualPort</code>. Tests expecting a
 * specific port can set this via the <code>setSpecifiedPort</code> methhod or
 * <code>port</code> property.
 * 
 * @author reaster
 */
abstract public class GenericWebServerTestCase {

	private static boolean runningMultiple = Boolean.getBoolean("runningMultiple");

	public static String DEFAULT_JETTY_PROPERTIES_PATH = "./target/test-classes/jetty.properties";

	protected final static int DEFAULT_PORT = 0;

	protected final static String DEFAULT_PROTOCOL = "http";

	protected final static String DEFAULT_HOST = "localhost";

	protected static int specifiedPort = DEFAULT_PORT;

	protected static String protocol = DEFAULT_PROTOCOL;

	protected static String host = DEFAULT_HOST;

	protected static int actualPort;

	protected static String relativeContext;

	protected static File webappHome;

	protected static URL baseContext;

	protected static String baseContextString;

	protected static String jettyPropertiesPath = DEFAULT_JETTY_PROPERTIES_PATH;

	protected static Properties properties;

	protected static Server jettyServer = null;

	protected static HttpClient httpClient = null;

	private static boolean _debug = true;

	private static boolean _verbose = false;

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
	public static void startServer(Initializer options) throws Exception {
		if (!isRunningContainer()) {
			if (options != null) {
				options.initialize();
			}
			
			loadAndVerifyJettyProperties();
			jettyServer = new Server(specifiedPort);
			WebAppContext context = new WebAppContext(webappHome.getAbsolutePath(), relativeContext);
			context.setParentLoaderPriority(true);
			jettyServer.addHandler(context);
			jettyServer.start();
			actualPort = jettyServer.getConnectors()[0].getLocalPort();
			baseContext = new URL(protocol, host, actualPort, relativeContext);
			info("Loading WebAppContext[%s] from %s", baseContext.toExternalForm(), webappHome.getAbsolutePath());
			httpClient = new HttpClient();
		}
	}

	/**
	 * Attempts to shut down the Jetty Container.
	 * 
	 * Only shuts down if we're not running from maven, and if the container has
	 * previously been started.
	 * 
	 */
	public static void stopServer() {
		if (isRunningContainer() && !runningMultiple) {
			if (jettyServer != null) {
				try {
					if (jettyServer.isRunning()) {
						jettyServer.stop();
					}
				} catch (Exception e) {
					warn("%s on shutting down Jetty server", e.getMessage());
				} finally {
					jettyServer = null;
				}
			}
			httpClient = null;
		}
	}

	/**
	 * @return is the container currently running?
	 */
	public static boolean isRunningContainer() {
		return getJettyServer() != null;
	}

	/**
	 * If you would like to start the Jetty Container, but pass things to it
	 * before it starts up, put those initialization options in here
	 * 
	 * @author Jason Holmes
	 */
	public static class Initializer {
		public void initialize() {
			// fill in your stuff here
		}
	};

	/**
	 * This initializer tells Jetty to load its property file from
	 * /src/test/resources instead of the default location
	 */
	public static final GenericWebServerTestCase.Initializer STATIC_JETTY_PROPERTIES = new GenericWebServerTestCase.Initializer() {
		@Override
		public void initialize() {
			setPropertyFileLocation("src/test/resources/jetty.properties");
		}
	};

	/**
	 * Start embedded web server and static properties including baseContext.
	 * 
	 * @deprecated use
	 *             {@link #startServer(org.codehaus.enunciate.test.integration.GenericWebServerTestCase.Initializer)}
	 *             instead
	 * 
	 */
	@Deprecated
	public static void setUpBeforeClass() throws Exception {
		startServer(null);
	}

	/**
	 * @deprecated use {@link #stopServer()} instead
	 */
	@Deprecated
	public static void tearDownAfterClass() {
		stopServer();
	}

	protected static Properties loadAndVerifyJettyProperties() throws IOException, IllegalStateException {
		properties = FileUtil.loadAndVerifyJettyPropertiesFile(jettyPropertiesPath, false);
		relativeContext = properties.getProperty("context", relativeContext);
		if (relativeContext == null) {
			throw new IllegalStateException("Required 'context' property not found in " + jettyPropertiesPath);
		}
		String webappDir = properties.getProperty("webapp.home", (webappHome == null) ? null : webappHome
				.getAbsolutePath());
		if (webappDir == null) {
			throw new IllegalStateException("Required 'webapp.home' property not found in " + jettyPropertiesPath);
		}
		webappHome = new File(webappDir);
		host = properties.getProperty("host", "localhost");
		specifiedPort = Integer.parseInt(properties.getProperty("port", "" + DEFAULT_PORT));
		protocol = properties.getProperty("protocol", "http");
		if (!webappHome.exists()) {
			throw new FileNotFoundException("webapp.home does not exist: " + webappHome.getAbsolutePath());
		}
		return properties;
	}

	/**
	 * Provides access to Jetty instance.
	 */
	public static Server getJettyServer() {
		return jettyServer;
	}

	public static void setRelativeContext(String context) {
		GenericWebServerTestCase.relativeContext = context;
	}

	public static void setWebappHome(File webappHome) {
		GenericWebServerTestCase.webappHome = webappHome;
	}

	/**
	 * Set the port to run tests on. The only time this will be different from
	 * the <code>actualPort</code> is when <code>0</code> (the default) is
	 * used, in which case the server assigns an arbitrary unused port.
	 */
	public static void setSpecifiedPort(int port) {
		specifiedPort = port;
	}

	/**
	 * Returns the port that was requested. The only time this will be different
	 * from the <code>actualPort</code> is when <code>0</code> (the default)
	 * is used, in which case the server assigns an arbitrary unused port.
	 */
	public static int getSpecifiedPort() {
		return specifiedPort;
	}

	/** Returns the port that was actually assigned */
	public static int getActualPort() {
		return actualPort;
	}

	/** Returns the context of the webapp, excluding protocol, host and port. */
	public static String getRelativeContext() {
		return relativeContext;
	}

	/** Base URL of the webapp including: protocol, host, port, webapp context. */
	public static URL getBaseContext() {
		return baseContext;
	}

	/** Host of the service. Default is <code>localhost</code>. */
	public static String getHost() {
		return host;
	}

	/** Directory where the expanded webapp is located. */
	public static File getWebappHome() {
		return webappHome;
	}

	/** Return a shared instance of HttpClient for use in tests */
	public static HttpClient getHttpClient() {
		return httpClient;
	}

	/** BaseContext as a string. */
	public static String getBaseContextAsString() {
		if (baseContextString == null) {
			baseContextString = baseContext.toExternalForm();
		}
		return baseContextString;
	}

	public static Properties getWebappProperties() {
		return properties;
	}

	public static void setPropertyFileLocation(String path) {
		jettyPropertiesPath = path;
	}

	/**
	 * Whether to be verbose.
	 * 
	 * @return Whether to be verbose.
	 */
	public static boolean isVerbose() {
		return _verbose || isDebug();
	}

	/**
	 * Whether to be verbose.
	 * 
	 * @param verbose
	 *            Whether to be verbose.
	 */
	public static void setVerbose(boolean verbose) {
		_verbose = verbose;
	}

	/**
	 * Whether to print debugging information.
	 * 
	 * @return Whether to print debugging information.
	 */
	public static boolean isDebug() {
		return _debug;
	}

	/**
	 * Whether to print debugging information.
	 * 
	 * @param debug
	 *            Whether to print debugging information.
	 */
	public static void setDebug(boolean debug) {
		_debug = debug;
	}

	/**
	 * Handle an info-level message.
	 * 
	 * @param message
	 *            The info message.
	 * @param formatArgs
	 *            The format args of the message.
	 */
	public static void info(String message, Object... formatArgs) {
		if (isVerbose()) {
			System.out.println(String.format(message, formatArgs));
		}
	}

	/**
	 * Handle a debug-level message.
	 * 
	 * @param message
	 *            The debug message.
	 * @param formatArgs
	 *            The format args of the message.
	 */
	public static void debug(String message, Object... formatArgs) {
		if (isDebug()) {
			System.out.println(String.format(message, formatArgs));
		}
	}

	/**
	 * Handle a warn-level message.
	 * 
	 * @param message
	 *            The warn message.
	 * @param formatArgs
	 *            The format args of the message.
	 */
	public static void warn(String message, Object... formatArgs) {
		System.out.println(String.format(message, formatArgs));
	}
}
