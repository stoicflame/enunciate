package net.sf.enunciate.modules.rest;

import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.enunciate.rest.annotations.RESTError;

import java.io.IOException;

/**
 * Has the response send the appropriate error, according to the error code of the (possibly annotated) exception class.
 *
 * @author Ryan Heaton
 */
public class RESTExceptionHandler implements HandlerExceptionResolver {

  public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object object, Exception exception) {
    int errorCode = 500;
    RESTError errorInfo = exception.getClass().getAnnotation(RESTError.class);
    if (errorInfo == null) {
      errorCode = errorInfo.errorCode();
    }

    try {
      response.sendError(errorCode, exception.getMessage());
    }
    catch (IOException e) {
      //fall through...
    }
    
    return null;
  }
}
