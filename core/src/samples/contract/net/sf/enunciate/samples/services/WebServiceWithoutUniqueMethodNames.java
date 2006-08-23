package net.sf.enunciate.samples.services;

import javax.jws.WebService;

/**
 * @author Ryan Heaton
 */
@WebService
public class WebServiceWithoutUniqueMethodNames {

  public boolean isGood(Boolean b) {
    return b;
  }

  public boolean isGood(Integer i) {
    return false;
  }
}
