package org.codehaus.enunciate.bytecode;

import org.objectweb.asm.Type;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Ryan Heaton
 */
@XmlRootElement
@XmlAccessorType( XmlAccessType.FIELD )
public class InstrumentationInfo {

  private Map<MethodKey, String[]> parameterNames;

  protected InstrumentationInfo() {
  }

  public InstrumentationInfo(Map<MethodKey, String[]> parameterNames) {
    this.parameterNames = parameterNames;
  }

  /**
   * Load the instrumentation info from the specified input stream.
   *
   * @param in The input stream.
   * @return The instrumentation info.
   */
  public static InstrumentationInfo loadFrom(InputStream in) {
    try {
      return (InstrumentationInfo) JAXBContext.newInstance(InstrumentationInfo.class).createUnmarshaller().unmarshal(in);
    }
    catch (JAXBException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Write this instrumentation info to a file.
   *
   * @param out The file to which to write this instrumentation info.
   */
  public void writeTo(OutputStream out) {
    try {
      JAXBContext.newInstance(getClass()).createMarshaller().marshal(this, out);
    }
    catch (JAXBException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Map of methods to parameter names.
   *
   * @return The map of method keys to parameter names.
   */
  public Map<MethodKey, String[]> getParameterNames() {
    return parameterNames;
  }

  /**
   * The class names of all instrumented classes.
   *
   * @return The class names of all instrumented classes.
   */
  public Set<String> getInstrumentedClasses() {
    TreeSet<String> instrumentedClasses = new TreeSet<String>();
    for (MethodKey methodKey : this.parameterNames.keySet()) {
      instrumentedClasses.add(Type.getObjectType(methodKey.getClassDescriptor()).getClassName());
    }
    return instrumentedClasses;
  }
}
