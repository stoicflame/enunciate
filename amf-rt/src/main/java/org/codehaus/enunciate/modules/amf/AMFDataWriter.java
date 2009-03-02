package org.codehaus.enunciate.modules.amf;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;

/**
 * Defines a writer for AMF data.
 *
 * @author Ryan Heaton
 */
public interface AMFDataWriter {

  /**
   * Whether an object of the specified type is writeable from a stream.
   *
   * @param realType The real type of the object.
   * @param genericType The declared generic type to which the object is to conform.
   * @return Whether an object of the specified type is writeable from a stream.
   */
  boolean isWriteable(Class realType, Type genericType);

  /**
   * Write the object to the specified stream.
   *
   * @param obj The object to write.
   * @param realType The real type of the object.
   * @param genericType The generic type of the object.
   * @param stream The stream to write to.
   */
  void writeTo(Object obj, Class realType, Type genericType, OutputStream stream) throws IOException;
}