package org.codehaus.enunciate.modules.spring_app.config;

import java.util.Set;
import java.util.HashSet;

/**
 * @author Ryan Heaton
 */
public class WebResourceSecurityOptions extends SecurityOptions {

  private final Set<String> roles = new HashSet<String>();

}
