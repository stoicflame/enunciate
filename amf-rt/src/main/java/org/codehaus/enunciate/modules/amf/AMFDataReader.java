package org.codehaus.enunciate.modules.amf;

import java.lang.reflect.Type;
import java.io.InputStream;
import java.io.IOException;

/**
 * Defines a reader for AMF data.
 *
 * @author Ryan Heaton
 */
public interface AMFDataReader {

  /**
   * Whether an object of the specified type is readable from a stream.
   *
   * @param realType The real type of the object.
   * @param genericType The declared generic type to which the object is to conform.
   * @return Whether an object of the specified type is readable from a stream.
   */
  boolean isReadable(Class realType, Type genericType);

  /**
   * Read the object from the specified stream.
   *
   * @param realType The real type of the object.
   * @param genericType The generic type of the object.
   * @param stream The stream to read from.
   * @return The object that was read.
   */
  Object readFrom(Class realType, Type genericType, InputStream stream) throws IOException;
}
