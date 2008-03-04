package org.codehaus.enunciate.test.integration.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Static file utility methods for web server configuration.
 * 
 * @author reaster
 */
public final class FileUtil {

	public static Properties loadAndVerifyJettyPropertiesFile(String propertyFilePath, boolean required) throws IOException, IllegalStateException {
		Properties props = new Properties();
		String jettyPropertyFile = System.getProperty("jetty.properties", propertyFilePath);
		File file = new File(jettyPropertyFile);
		if (!file.exists()) {
			if (required) {
				throw new FileNotFoundException("Jetty configuration file not found: "+file.getAbsolutePath());
			}
		} else {
			//sanity check
			String str = fileToString(file);
			if (str.indexOf("${") != -1) {
				throw new IllegalStateException("Unfiltered properties found in "+file.getAbsolutePath()+" - run: mvn process-test-resources");
			}
			props.load(new FileInputStream(file));
		}
		return props;
	}

	public static String fileToString(File file) throws IOException {
		FileInputStream stream = new FileInputStream (file);
        byte[] bytes = new byte[stream.available()];
        stream.read(bytes);
        stream.close ();
        String str = new String(bytes);
		return str;
	}
}
