package org.codehaus.enunciate.modules.jersey;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

/**
 * @author Ryan Heaton
 */
public class SpringServlet extends ServletContainer {

  @Override
  protected void configure(ServletConfig sc, ResourceConfig rc, WebApplication wa) {
    getServletContext().getResourceAsStream("/WEB-INF/");
    //todo: load the known classes (don't use jersey's default loading mechanism)
//    rc.getResourceClasses().add(...)

    super.configure(sc, rc, wa);
  }

  @Override
  protected void initiate(ResourceConfig rc, WebApplication wa) {
    wa.initiate(rc, new SpringComponentProvider(WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext())));
  }

  @Override
  public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request = new HttpServletRequestWrapper(request) {
      @Override
      public String getServletPath() {
        //fool jersey into thinking it's always processing at the "/*" path.
        return "";
      }
    };

    super.service(request, response);
  }
}
