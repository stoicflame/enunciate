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
