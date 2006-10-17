package net.sf.enunciate.modules.xfire_client;

import org.codehaus.xfire.aegis.AegisBindingProvider;
import org.codehaus.xfire.aegis.type.TypeMappingRegistry;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.MessagePartContainer;

/**
 * @author Ryan Heaton
 */
public class EnunciatedClientBindingProvider extends AegisBindingProvider {

  public EnunciatedClientBindingProvider(TypeMappingRegistry registry) {
    super(registry);
  }

  protected void initializeMessage(Service service, MessagePartContainer container, int type) {
    //no-op...
  }


}
