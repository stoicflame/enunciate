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

package org.codehaus.enunciate.tests.xfire_integration;

import junit.framework.TestCase;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * @author Ryan Heaton
 */
public class TestFullJAXRSApi extends TestCase {

  /**
   * Tests the full REST API
   */
  public void testFullRestApi() throws Exception {
    int port = 8080;
    if (System.getProperty("container.port") != null) {
      port = Integer.parseInt(System.getProperty("container.port"));
    }

    String context = "full";
    if (System.getProperty("container.test.context") != null) {
      context = System.getProperty("container.test.context");
    }

    URL url = new URL("http://localhost:" + port + "/" + context + "/rest/source/valid");
    HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
    httpConnection.setRequestMethod("GET");
    httpConnection.setRequestProperty("Accept", "*/*");
    httpConnection.connect();
    assertEquals(200, httpConnection.getResponseCode());
    httpConnection.disconnect();

    httpConnection = (HttpURLConnection) url.openConnection();
    httpConnection.setRequestMethod("GET");
    httpConnection.setRequestProperty("Accept", "application/json");
    httpConnection.connect();
    assertEquals(200, httpConnection.getResponseCode());
    httpConnection.disconnect();

    url = new URL("http://localhost:" + port + "/" + context + "/xml/source/valid");
    httpConnection = (HttpURLConnection) url.openConnection();
    httpConnection.setRequestMethod("GET");
    httpConnection.setRequestProperty("Accept", "*/*");
    httpConnection.connect();
    assertEquals(200, httpConnection.getResponseCode());
    httpConnection.disconnect();

    url = new URL("http://localhost:" + port + "/" + context + "/json/source/valid");
    httpConnection = (HttpURLConnection) url.openConnection();
    httpConnection.setRequestMethod("GET");
    httpConnection.setRequestProperty("Accept", "*/*");
    httpConnection.connect();
    assertEquals(200, httpConnection.getResponseCode());
    assertEquals("application/json", httpConnection.getHeaderField("Content-Type"));
    httpConnection.disconnect();
  }

}