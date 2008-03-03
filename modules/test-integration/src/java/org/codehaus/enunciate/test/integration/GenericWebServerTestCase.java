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
 * JUnit base class for running lightweight integration tests on webapps with an embedded 
 * web server (Jetty).
 * 
 * This class provides static setUpBeforeClass() and tearDownAfterClass() methods to 
 * start and stop the web server.  JUnit 4 testers will call these methods from their
 * @BeforeClass and @AfterClass methods.
 * 
 * A shared Apache Commons HttpClient instance is provided and tests can retrieve the webapp's full URL by 
 * calling getBaseContextAsString():
 * <pre>
 * public void testServiceIsRunning() throws Exception {
 *   GetMethod method = new GetMethod(GenericWebServerTestCase.getBaseContextAsString());
 *   try {
 *     int statusCode = GenericWebServerTestCase.getHttpClient().executeMethod(method);
 *     assertEquals("HttpStatus.SC_OK", HttpStatus.SC_OK, statusCode);
 *   } finally {
 *     method.releaseConnection();
 *   }
 * }
 * </pre>
 * 
 * Minimally the relativeContext and webappHome properties must be set before
 * calling setUpBeforeClass():
 * <pre>
 * public static void beforeTest() {
 *   GenericWebServerTestCase.setRelativeContext("/my-webapp");
 *   GenericWebServerTestCase.setWebappHome("target/my-webapp");
 *   GenericWebServerTestCase.setUpBeforeClass();
 * }
 * </pre>
 * 
 * Alternatively, configuration can be loaded from a properties file.
 * The default, Maven-friendly location of the 'jetty.properties' file is the 
 * './target/test-classes/jetty.properties', but the location can be changed
 * at startup:
 * <pre>
 * public static void beforeTest() {
 *   GenericWebServerTestCase.setPropertyFileLocation("src/config/webapp-config.properties");
 *   GenericWebServerTestCase.setUpBeforeClass();
 * }
 * </pre>
 * 
 * One motivation for supporting property file configuration is to allow filtering by
 * Maven.  Here is how that might look:
 * <pre>
 * context=/${project.build.finalName}
 * webapp.home=${project.build.directory}/${project.build.finalName}
 * host=localhost
 * protocol=http
 * port=0
 * </pre>
 * 
 * The default port is <code>0</code> which tells the web server to pick an arbitrary port.
 * The actual port used is available from the relevant static property methods,
 * such as <code>getActualPort</code>.  Tests expecting a specific port can set this
 * via the <code>setSpecifiedPort</code> methhod or <code>port</code> property.
 * 
 * @author reaster
 */
abstract public class GenericWebServerTestCase {

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

	protected static HttpClient httpClient;

	private static boolean _debug = true;

	private static boolean _verbose = false;

	/**
	 * Start embedded web server and static properties including baseContext.
	 * 
	 * @throws Exception
	 */
	// @org.junit.BeforeClass
	public static void setUpBeforeClass() throws Exception {
		if (httpClient == null) {
			loadAndVerifyJettyProperties();
			jettyServer = new Server(specifiedPort);
			jettyServer.addHandler(new WebAppContext(webappHome.getAbsolutePath(), relativeContext));
			//org.mortbay.log.Log.getLog().setDebugEnabled(false); // TODO too verbose
			jettyServer.start();
			actualPort = jettyServer.getConnectors()[0].getLocalPort();
			baseContext = new URL(protocol, host, actualPort, relativeContext);
			info("Loading WebAppContext[%s] from %s",  baseContext.toExternalForm(), webappHome.getAbsolutePath());
			httpClient = new HttpClient();
		}
	}

	// @org.junit.AfterClass
	public static void tearDownAfterClass() {
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

	protected static Properties loadAndVerifyJettyProperties()
			throws IOException, IllegalStateException {
		properties = FileUtil.loadAndVerifyJettyPropertiesFile(jettyPropertiesPath, false);
		relativeContext = properties.getProperty("context", relativeContext);
		if (relativeContext == null) {
			throw new IllegalStateException(
					"Required 'context' property not found in " + jettyPropertiesPath);
		}
		String webappDir = properties.getProperty("webapp.home", webappHome.getAbsolutePath());
		if (webappDir == null) {
			throw new IllegalStateException(
					"Required 'webapp.home' property not found in " + jettyPropertiesPath);
		}
		webappHome = new File(webappDir);
		host = properties.getProperty("host", "localhost");
		specifiedPort = Integer.parseInt(properties.getProperty("port", ""+DEFAULT_PORT));
		protocol = properties.getProperty("protocol", "http");
		if (!webappHome.exists()) {
			throw new FileNotFoundException("webapp.home does not exist: " + webappHome.getAbsolutePath());
		}
		return properties;
	}

	/** Provides access to Jetty instance. */
	public static Server getJettyServer() {
		return jettyServer;
	}

	public static void setRelativeContext(String context) {
		relativeContext = context;
	}

	public static void setWebappHome(File webappHome) {
		GenericWebServerTestCase.webappHome = webappHome;
	}

	/**
	 * Set the port to run tests on. The only time this will be different
	 * from the <code>actualPort</code> is when <code>0</code> (the default)
	 * is used, in which case the server assigns an arbitrary unused port.
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
