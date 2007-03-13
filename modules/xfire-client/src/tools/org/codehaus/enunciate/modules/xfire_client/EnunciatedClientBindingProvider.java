package org.codehaus.enunciate.modules.xfire_client;

import org.codehaus.xfire.aegis.AegisBindingProvider;
import org.codehaus.xfire.aegis.type.TypeMappingRegistry;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.MessagePartContainer;

/**
 * An enunciate-specific client binding provider.
 * 
 * @author Ryan Heaton
 */
public class EnunciatedClientBindingProvider extends AegisBindingProvider {

  public EnunciatedClientBindingProvider(TypeMappingRegistry registry) {
    super(registry);
  }

  /**
   * No-op.  Message parts are not initialized because enunciate uses the JAXWS-specified logic for
   * (de)serializing messages, which is to deserialize the request/response wrappers.
   *
   * @param service The service.
   * @param container The container.
   * @param type The type of the message.
   */
  protected void initializeMessage(Service service, MessagePartContainer container, int type) {
    //no-op...
  }


}
