package com.webcohesion.enunciate.module;

import com.webcohesion.enunciate.api.ApiRegistry;

/**
 * @author Ryan Heaton
 */
public interface ApiRegistryConsumerModule extends EnunciateModule {

  void setApiRegistry(ApiRegistry registry);
}
