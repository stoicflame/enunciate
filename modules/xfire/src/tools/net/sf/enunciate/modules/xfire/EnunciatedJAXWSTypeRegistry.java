package net.sf.enunciate.modules.xfire;

import org.codehaus.xfire.aegis.type.TypeCreator;
import org.codehaus.xfire.jaxws.type.JAXWSTypeRegistry;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;

/**
 * @author Ryan Heaton
 */
public class EnunciatedJAXWSTypeRegistry extends JAXWSTypeRegistry {

  private final HashMap<String, String[]> parameterNames;

  public EnunciatedJAXWSTypeRegistry(String typeSetId) {
    InputStream paramNamesStream = getClass().getResourceAsStream("/" + typeSetId + ".message.part.names");
    if (paramNamesStream == null) {
      throw new IllegalArgumentException("Unkown type set id: " + typeSetId);
    }

    try {
      ObjectInputStream ois = new ObjectInputStream(paramNamesStream);
      this.parameterNames = (HashMap<String, String[]>) ois.readObject();
      ois.close();
    }
    catch (Exception e) {
      throw new IllegalArgumentException("Unable to read parameter names for type set " + typeSetId, e);
    }
  }

  @Override
  protected TypeCreator createTypeCreator() {
    return new EnunciatedJAXWSTypeCreator(this.parameterNames);
  }
}
