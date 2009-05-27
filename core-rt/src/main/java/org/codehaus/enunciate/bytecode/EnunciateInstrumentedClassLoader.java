package org.codehaus.enunciate.bytecode;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;

/**
 * @author Ryan Heaton
 */
public class EnunciateInstrumentedClassLoader extends ClassLoader {

  private final InstrumentationInfo instrumentation;

  public EnunciateInstrumentedClassLoader(ClassLoader parent, InstrumentationInfo instrumentation) throws ClassNotFoundException {
    super(parent);
    this.instrumentation = instrumentation;

    //redefine all the instrumented classes.
    for (String instrumentedClass : instrumentation.getInstrumentedClasses()) {
      findClass(instrumentedClass);
    }
  }

  @Override
  public Class<?> findClass(String name) throws ClassNotFoundException {
    ClassWriter writer = new ClassWriter(0);
    ClassReader reader;
    try {
      reader = new ClassReader(name);
    }
    catch (IOException e) {
      throw new ClassNotFoundException("Unable to read class " + name + ".", e);
    }

    reader.accept(new WebParamAnnotationInstrumentation(writer, this.instrumentation), 0);
    byte[] bytes = writer.toByteArray();
    return defineClass(name, bytes, 0, bytes.length);
  }

}
