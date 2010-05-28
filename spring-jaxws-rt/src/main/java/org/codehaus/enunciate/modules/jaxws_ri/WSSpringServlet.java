/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.modules.jaxws_ri;

import com.sun.xml.ws.transport.http.servlet.ServletAdapterList;
import com.sun.xml.ws.transport.http.servlet.SpringBinding;
import com.sun.xml.ws.transport.http.servlet.SpringBindingList;
import com.sun.xml.ws.transport.http.servlet.WSServletDelegate;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class WSSpringServlet extends HttpServlet {

  private WSServletDelegate delegate;

  public void init(ServletConfig servletConfig) throws ServletException {
    super.init(servletConfig);

    // get the configured adapters from Spring
    WebApplicationContext parent = WebApplicationContextUtils
      .getRequiredWebApplicationContext(getServletContext());

    ConfigurableWebApplicationContext wac = new XmlWebApplicationContext();
    wac.setParent(parent);
    wac.setServletContext(getServletContext());
    wac.setServletConfig(getServletConfig());
    wac.setNamespace(servletConfig.getServletName() + "-servlet");
    wac.refresh();

    Set<SpringBinding> bindings = new LinkedHashSet<SpringBinding>();

    // backward compatibility. recognize all bindings
    Map m = wac.getBeansOfType(SpringBindingList.class);
    for (SpringBindingList sbl : (Collection<SpringBindingList>) m.values()) {
      bindings.addAll(sbl.getBindings());
    }

    bindings.addAll(wac.getBeansOfType(SpringBinding.class).values());

    // create adapters
    ServletAdapterList l = new ServletAdapterList();
    for (SpringBinding binding : bindings) {
      binding.create(l);
    }

    delegate = new WSServletDelegate(l, getServletContext());
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
    delegate.doPost(request, response, getServletContext());
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException {
    delegate.doGet(request, response, getServletContext());
  }

  protected void doPut(HttpServletRequest request, HttpServletResponse response)
    throws ServletException {
    delegate.doPut(request, response, getServletContext());
  }

  protected void doDelete(HttpServletRequest request, HttpServletResponse response)
    throws ServletException {
    delegate.doDelete(request, response, getServletContext());
  }
}
