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

import com.sun.jersey.samples.storageservice.Container;
import com.sun.jersey.samples.storageservice.Containers;
import com.sun.jersey.samples.storageservice.Item;
import junit.framework.TestCase;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 * @author Ryan Heaton
 */
public class TestJerseyStorageService extends TestCase {

  /**
   * tests the containers.
   */
  public void testContainers() throws Exception {
    WebTarget resource = getStorageResource();

    Response response = resource.path("containers").request("application/xml").get();
    assertEquals(200, response.getStatus());
    Containers containers = response.readEntity(Containers.class);
    assertTrue(containers.getContainer() == null || containers.getContainer().isEmpty());

    response = resource.path("containers/one").request().get();
    assertEquals(404, response.getStatus());

    Container containerOne = new Container();
    response = resource.path("containers/one").request().put(Entity.entity(containerOne, "application/xml"));
    assertEquals(201, response.getStatus());

    response = resource.path("containers").request("application/xml").get();
    assertEquals(200, response.getStatus());
    containers = response.readEntity(Containers.class);
    assertEquals(1, containers.getContainer().size());

    response = resource.path("containers/one").request().get();
    assertEquals(200, response.getStatus());
    containerOne = response.readEntity(Container.class);
    assertEquals("one", containerOne.getName());
    assertNotNull(containerOne.getUri());
    assertTrue(containerOne.getItem() == null || containerOne.getItem().isEmpty());

    String stringItem = "here is a string that we want to store";
    response = resource.path("containers/one/string").request().put(Entity.entity(stringItem, "text/plain"));
    assertEquals(201, response.getStatus());

    response = resource.path("containers/one").request().get();
    assertEquals(200, response.getStatus());
    containerOne = response.readEntity(Container.class);
    assertEquals(1, containerOne.getItem().size());
    assertEquals("text/plain", containerOne.getItem("string").getMimeType());

    response = resource.path("containers/one/string").request().get();
    assertEquals(200, response.getStatus());
    assertEquals(stringItem, response.readEntity(String.class));

    response = resource.path("spring/item").request().get();
    assertEquals(200, response.getStatus());
    Item item = response.readEntity(Item.class);
    assertEquals("SpringItem", item.getName());
    assertEquals("urn:SpringItem", item.getUri());
  }

  protected WebTarget getStorageResource() {
    Client client = ClientBuilder.newClient();
    int port = 8080;
    if (System.getProperty("container.port") != null) {
      port = Integer.parseInt(System.getProperty("container.port"));
    }

    String context = "storage";
    if (System.getProperty("container.test.jersey.context") != null) {
      context = System.getProperty("container.test.jersey.context");
    }

    return client.target(String.format("http://localhost:%s/%s", port, context));
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();

    WebTarget resource = getStorageResource();
    Response response = resource.path("containers").request("application/xml").get();
    if (200 == response.getStatus()) {
      Containers containers = response.readEntity(Containers.class);
      if (containers.getContainer() != null) {
        for (Container container : containers.getContainer()) {
          resource.path("containers/" + container.getName()).request().delete();
        }
      }
    }
  }
}
