package org.codehaus.enunciate.modules.gwt;

import java.util.List;

/**
 * @author Ryan Heaton
 */
@SuppressWarnings ( {"all"} )
public class WarningExposingGWTEndpointImpl extends GWTEndpointImpl {

  public WarningExposingGWTEndpointImpl(Object serviceBean) {
    super(serviceBean);
  }

  @Override
  protected Class getServiceInterface() {
    return URLAdapter.class;
  }


  public List<URLAdapter> urlAdapterExample() {
    try {
      return (List<URLAdapter>) invokeOperation("whatever");
    }
    catch (Exception e) {
      return null;
    }
  }
}
