package com.sun.jersey.samples.storageservice;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * @author Ryan Heaton
 */
@Provider
@Produces("application/xml")
public class ItemProvider implements ContextResolver<Item> {
  @Override
  public Item getContext(Class<?> type) {
    return new Item();
  }
}
