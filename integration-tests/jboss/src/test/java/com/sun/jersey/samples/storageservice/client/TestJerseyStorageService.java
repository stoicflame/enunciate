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

package com.sun.jersey.samples.storageservice.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.samples.storageservice.Container;
import com.sun.jersey.samples.storageservice.Containers;
import junit.framework.TestCase;

/**
 * @author Ryan Heaton
 */
public class TestJerseyStorageService extends TestCase {

  /**
   * tests the containers.
   */
  public void testContainers() throws Exception {
    WebResource resource = getStorageResource();

    ClientResponse response = resource.path("containers").accept("application/xml").get(ClientResponse.class);
    assertEquals(200, response.getStatus());
    Containers containers = response.getEntity(Containers.class);
    assertTrue(containers.getContainer() == null || containers.getContainer().isEmpty());

    response = resource.path("containers/one").get(ClientResponse.class);
    assertEquals(404, response.getStatus());

    Container containerOne = new Container();
    response = resource.path("containers/one").put(ClientResponse.class, containerOne);
    assertEquals(201, response.getStatus());

    response = resource.path("containers").accept("application/xml").get(ClientResponse.class);
    assertEquals(200, response.getStatus());
    containers = response.getEntity(Containers.class);
    assertEquals(1, containers.getContainer().size());

    response = resource.path("containers/one").get(ClientResponse.class);
    assertEquals(200, response.getStatus());
    containerOne = response.getEntity(Container.class);
    assertEquals("one", containerOne.getName());
    assertNotNull(containerOne.getUri());
    assertTrue(containerOne.getItem() == null || containerOne.getItem().isEmpty());

    String stringItem = "here is a string that we want to store";
    response = resource.path("containers/one/string").type("text/plain").put(ClientResponse.class, stringItem);
    assertEquals(201, response.getStatus());

    response = resource.path("containers/one").get(ClientResponse.class);
    assertEquals(200, response.getStatus());
    containerOne = response.getEntity(Container.class);
    assertEquals(1, containerOne.getItem().size());
    assertEquals("text/plain", containerOne.getItem("string").getMimeType());

    response = resource.path("containers/one/string").get(ClientResponse.class);
    assertEquals(200, response.getStatus());
    assertEquals(stringItem, response.getEntity(String.class));
  }

  protected WebResource getStorageResource() {
    return getStorageResource("rest");
  }

  protected WebResource getStorageResource(String subcontext) {
    ClientConfig config = new DefaultClientConfig();
    Client client = Client.create(config);
    int port = 8080;
    if (System.getProperty("container.port") != null) {
      port = Integer.parseInt(System.getProperty("container.port"));
    }

    String context = "storage";
    if (System.getProperty("container.test.jersey.context") != null) {
      context = System.getProperty("container.test.jersey.context");
    }

    return client.resource(String.format("http://localhost:%s/%s/%s", port, context, subcontext));
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();

    WebResource resource = getStorageResource();
    ClientResponse response = resource.path("containers").accept("application/xml").get(ClientResponse.class);
    if (200 == response.getStatus()) {
      Containers containers = response.getEntity(Containers.class);
      if (containers.getContainer() != null) {
        for (Container container : containers.getContainer()) {
          resource.path("containers/" + container.getName()).delete();
        }
      }
    }
  }
}
