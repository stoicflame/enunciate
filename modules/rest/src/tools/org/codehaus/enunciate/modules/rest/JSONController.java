package org.codehaus.enunciate.modules.rest;

import org.codehaus.enunciate.rest.annotations.VerbType;
import org.springframework.beans.BeansException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * A controller for the JSON API.
 *
 * @author Ryan Heaton
 */
public class JSONController extends RESTController {

  private Map<String, String> ns2prefix;
  private boolean enabled = false;


  public JSONController() {
    super();

    setSubcontext("json");
  }

  @Override
  protected void initApplicationContext() throws BeansException {
    super.initApplicationContext();

    try {
      Class.forName("org.codehaus.jettison.mapped.MappedXMLOutputFactory", true, JSONController.class.getClassLoader());
      enabled = true;
    }
    catch (ClassNotFoundException e) {
      enabled = false;
    }
  }

  @Override
  protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
    if (enabled) {
      return super.handleRequestInternal(request, response);
    }
    else {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return null;
    }
  }

  @Override
  protected boolean isOperationAllowed(RESTOperation operation) {
    return super.isOperationAllowed(operation) && operation.getVerb() == VerbType.read;
  }

  @Override
  protected View createView(RESTOperation operation, Object result) {
    return new JSONResultView(operation, result, this.ns2prefix);
  }

  /**
   * The map of namespaces to prefixes.
   *
   * @param ns2prefix The map of namespaces to prefixes.
   */
  public void setNamespaces2Prefixes(Map<String, String> ns2prefix) {
    this.ns2prefix = ns2prefix;
  }
}
