package org.codehaus.enunciate.modules.xfire_client;

import org.codehaus.xfire.XFire;
import org.codehaus.xfire.XFireFactory;
import org.codehaus.xfire.soap.SoapConstants;
import org.codehaus.xfire.client.Client;
import org.codehaus.xfire.client.XFireProxyFactory;
import org.codehaus.xfire.client.XFireProxy;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.transport.TransportManager;
import org.codehaus.xfire.transport.http.SoapHttpTransport;

import java.lang.reflect.Proxy;

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
  //todo: make this a statically-accessed helper method.  Then you could actually replace this class with another in the config.
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
}
