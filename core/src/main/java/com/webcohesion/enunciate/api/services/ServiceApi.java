package com.webcohesion.enunciate.api.services;

import java.util.List;

/**
 * @author Ryan Heaton
 */
public interface ServiceApi {

  String getContextPath();

  List<ServiceGroup> getServiceGroups();

}
