package net.sf.enunciate.decorations.jaxws;

import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.TypeVisitor;
import freemarker.template.SimpleHash;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * A decorated type mirror that is a web result.
 *
 * @author Ryan Heaton
 */
public class WebResult extends SimpleHash implements TypeMirror {

  private final TypeMirror delegate;
  private final String name;
  private final String targetNamespace;
  private final String partName;

  public WebResult(TypeMirror delegate, WebMethod method) {
    this.delegate = delegate;

    try {
      BeanInfo beanInfo = Introspector.getBeanInfo(delegate.getClass());
      PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
      for (PropertyDescriptor pd : pds) {
        Method getter = pd.getReadMethod();
        if (getter != null) {
          put(pd.getName(), getter.invoke(delegate));
        }
      }
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }

    javax.jws.WebResult annotation = method.getAnnotation(javax.jws.WebResult.class);

    String name = "return";
    if (annotation != null) {
      name = annotation.name();
    }
    this.name = name;
    put("name", name);

    String targetNamespace = null;
    if (annotation != null) {
      targetNamespace = annotation.targetNamespace();
      if ((targetNamespace == null) || ("".equals(targetNamespace))) {
        targetNamespace = method.getDeclaringWebService().getTargetNamespace();
      }
    }
    this.targetNamespace = targetNamespace;
    put("targetNamespace", targetNamespace);

    this.partName = "return";
    put("partName", "return");
  }

  public void accept(TypeVisitor typeVisitor) {
    delegate.accept(typeVisitor);
  }

  /**
   * The name of the web result.
   *
   * @return The name of the web result.
   */
  public String getName() {
    return name;
  }

  /**
   * The namespace of the web result.
   *
   * @return The namespace of the web result.
   */
  public String getTargetNamespace() {
    return targetNamespace;
  }

  /**
   * The part name.
   *
   * @return The part name.
   */
  public String getPartName() {
    return partName;
  }

}
