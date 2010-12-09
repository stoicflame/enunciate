package org.codehaus.enunciate.modules.jersey;

import com.sun.jersey.spi.container.WebApplication;
import org.codehaus.enunciate.modules.jersey.response.StatusMessageResponse;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * @author Ryan Heaton
 */
public class JerseyAdaptedHttpServletResponse extends HttpServletResponseWrapper {

  private final WebApplication wa;

  public JerseyAdaptedHttpServletResponse(HttpServletResponse response, WebApplication wa) {
    super(response);
    this.wa = wa;
  }

  @Override
  public void sendError(int sc) throws IOException {
    String message = getCurrentStatusMessage();
    if (message != null) {
      super.sendError(sc, message);
    }
    else {
      super.sendError(sc);
    }
  }

  @Override
  public void setStatus(int sc) {
    String message = getCurrentStatusMessage();
    if (message != null) {
      super.setStatus(sc, message);
    }
    else {
      super.setStatus(sc);
    }
  }

  public String getCurrentStatusMessage() {
    if (this.wa != null && this.wa.getThreadLocalHttpContext() != null && this.wa.getThreadLocalHttpContext().getResponse() != null) {
      Response res = this.wa.getThreadLocalHttpContext().getResponse().getResponse();
      if (res instanceof StatusMessageResponse) {
        return ((StatusMessageResponse) res).getStatusMessage();
      }
    }

    return null;
  }

}
