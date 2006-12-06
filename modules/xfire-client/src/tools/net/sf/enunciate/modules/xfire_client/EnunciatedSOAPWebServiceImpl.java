package net.sf.enunciate.modules.xfire_client;

import org.codehaus.xfire.client.Client;
import org.codehaus.xfire.client.XFireProxyFactory;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.XFire;
import org.codehaus.xfire.XFireFactory;
import org.codehaus.xfire.transport.http.SoapHttpTransport;
import org.codehaus.xfire.transport.TransportManager;

/**
 * A base class for client-side soap web service implementations.
 *
 * @author Ryan Heaton
 */
public abstract class EnunciatedSOAPWebServiceImpl {

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
  
}
