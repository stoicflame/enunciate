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

package com.sun.jersey.samples.storageservice.resources;

import com.sun.jersey.samples.storageservice.Item;

import javax.ws.rs.Path;
import javax.ws.rs.GET;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Ryan Heaton
 */
@Path ("/spring")
public class SpringManagedResource {

  private Item specialItem;

  /**
   * Get an item.
   *
   * @return The item to get.
   */
  @GET
  @Path ("/item")
  public Item getSpecialItem() {
    return specialItem;
  }

  @Autowired
  public void setSpecialItem(Item specialItem) {
    this.specialItem = specialItem;
  }
}
