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

package org.codehaus.enunciate.modules.xfire_client;

import org.codehaus.xfire.XFire;
import org.codehaus.xfire.XFireFactory;
import org.codehaus.xfire.soap.SoapConstants;
import org.codehaus.xfire.client.Client;
import org.codehaus.xfire.client.XFireProxyFactory;
import org.codehaus.xfire.client.XFireProxy;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.transport.TransportManager;
import org.codehaus.xfire.transport.Channel;
import org.codehaus.xfire.transport.http.SoapHttpTransport;
import org.codehaus.xfire.transport.http.HttpChannel;
import org.codehaus.xfire.transport.http.CommonsHttpMessageSender;
import org.apache.commons.httpclient.HttpState;

import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * A base class for client-side soap web service implementations.
 *
 * @author Ryan Heaton
 */
public abstract class EnunciatedSOAPWebServiceImpl {

  private final Object proxy;
  private final Client client;

  /**
   * Construct an enunciated SOAP web service that implements the specified interface.
   *
   * @param iface The interface.
   * @param uuid The UUID of the interface.
   * @param endpoint The endpoint URL of the SOAP port.
   */
  protected EnunciatedSOAPWebServiceImpl(Class iface, String uuid, String endpoint) {
    this.proxy = loadProxy(iface, uuid, endpoint);
    XFireProxy xfireProxy = (XFireProxy) Proxy.getInvocationHandler(proxy);
    this.client = xfireProxy.getClient();
    setMTOMEnabled(true);
  }

  /**
   * Load an XFire client proxy that implements the specified interface.
   *
   * @param iface The interface.
   * @param uuid The UUID of the interface.
   * @param endpoint The endpoint URL of the SOAP port.
   * @return The proxy.
   */
  protected final Object loadProxy(Class iface, String uuid, String endpoint) {
    XFire xFire = XFireFactory.newInstance().getXFire();
    TransportManager transportManager = xFire.getTransportManager();

    Service service;
    try {
      ExplicitJAXWSAnnotationServiceFactory factory = new ExplicitJAXWSAnnotationServiceFactory(uuid, transportManager);
      service = factory.create(iface);
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new IllegalStateException(e);
    }

    SoapHttpTransport soapTransport = new SoapHttpTransport();
    if (!soapTransport.isUriSupported(endpoint)) {
      throw new IllegalArgumentException("Endpoint " + endpoint + " is not a supported SOAP endpoint.");
    }
    soapTransport.addOutHandler(new EnunciatedClientSoapSerializerHandler());

    Client client = new Client(soapTransport, service, endpoint);
    return new XFireProxyFactory(xFire).create(client);
  }

  /**
   * The xfire proxy that will handle the web service calls on the client-side.
   *
   * @return The xfire proxy that will handle the web service calls on the client-side.
   */
  public final Object getProxy() {
    return proxy;
  }

  /**
   * The xfire client object that backs this web service.
   *
   * @return The xfire client object that backs this web service.
   */
  public final Client getXFireClient() {
    return client;
  }

  /**
   * Whether MTOM is enabled.
   *
   * @param MTOMEnabled Whether MTOM is enabled.
   */
  public final void setMTOMEnabled(boolean MTOMEnabled) {
    this.client.setProperty(SoapConstants.MTOM_ENABLED, String.valueOf(MTOMEnabled));
  }

  /**
   * Sets the HTTP AUTH credentials for this service.
   *
   * @param username The username.
   * @param password The password.
   */
  public final void setHttpAuthCredentials(String username, String password) {
    this.client.setProperty(Channel.USERNAME, username);
    this.client.setProperty(Channel.PASSWORD, password);
  }

  /**
   * Sets the http headers to use for this service.
   *
   * @param httpHeaders The http headers to use.
   */
  public final void setHttpHeaders(Map httpHeaders) {
    this.client.setProperty(EnunciatedHttpMessageSender.HTTP_HEADERS, httpHeaders);
  }

  /**
   * Sets the request handler for this service.
   *
   * @param requestHandler The request handler.
   */
  public final void setRequestHandler(RequestHandler requestHandler) {
    this.client.setProperty(EnunciatedHttpMessageSender.REQUEST_HANDLER, requestHandler);
  }

  /**
   * Gets the HTTP state for this service.
   *
   * @return The http state, or null if none has been set or created yet.
   */
  public final HttpState getHttpState() {
    try {
      HttpChannel httpChannel = (HttpChannel) getXFireClient().getTransport().createChannel(getXFireClient().getEndpointUri());
      return (HttpState) httpChannel.getProperty(CommonsHttpMessageSender.HTTP_STATE);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Sets the HTTP state for this service.
   *
   * @param state The http state.
   */
  public final void setHttpState(HttpState state) {
    try {
      HttpChannel httpChannel = (HttpChannel) getXFireClient().getTransport().createChannel(getXFireClient().getEndpointUri());
      httpChannel.setProperty(CommonsHttpMessageSender.HTTP_STATE, state);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
