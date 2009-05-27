package org.codehaus.enunciate.bytecode;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;

/**
 * Unique key to a java method.
 *
 * @author Ryan Heaton
 */
@XmlAccessorType( XmlAccessType.FIELD )
public class MethodKey {

  private String classDescriptor;
  private String methodName;
  private String methodDescriptor;

  protected MethodKey() {
  }

  public MethodKey(String classDescriptor, String methodName, String methodDescriptor) {
    this.classDescriptor = classDescriptor;
    this.methodName = methodName;
    this.methodDescriptor = methodDescriptor;
  }

  /**
   * The descriptor for the class that declares this method.
   *
   * @return The descriptor for the class that declares this method.
   */
  public String getClassDescriptor() {
    return classDescriptor;
  }

  /**
   * The method name.
   *
   * @return The method name.
   */
  public String getMethodName() {
    return methodName;
  }

  /**
   * The method descriptor (see {@link org.objectweb.asm.Type Type}).
   *
   * @return The method descriptor.
   */
  public String getMethodDescriptor() {
    return methodDescriptor;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    MethodKey methodKey = (MethodKey) o;
    return classDescriptor.equals(methodKey.classDescriptor) 
      && methodDescriptor.equals(methodKey.methodDescriptor)
      && methodName.equals(methodKey.methodName);
  }

  @Override
  public int hashCode() {
    int result = classDescriptor.hashCode();
    result = 31 * result + methodName.hashCode();
    result = 31 * result + methodDescriptor.hashCode();
    return result;
  }
}
