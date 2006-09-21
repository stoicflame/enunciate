package net.sf.enunciate.modules.xfire_client.annotations;

import org.codehaus.xfire.annotations.HandlerChainAnnotation;

import java.io.Serializable;

/**
 * @author Ryan Heaton
 */
public class SerializableHandlerChainAnnotation extends HandlerChainAnnotation implements Serializable {

  public SerializableHandlerChainAnnotation(String file, String name) {
    super(file, name);
  }

}
