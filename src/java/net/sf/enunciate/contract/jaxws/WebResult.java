package net.sf.enunciate.contract.jaxws;

import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.TypeVisitor;
import freemarker.template.SimpleHash;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;

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
  private final WebMethod method;
  private final String docComment;

  protected WebResult(TypeMirror delegate, WebMethod method) {
    this.delegate = delegate;
    this.method = method;

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

    String targetNamespace = method.getDeclaringEndpointInterface().getTargetNamespace();
    if (annotation != null) {
      String annotatedNamespace = annotation.targetNamespace();
      if ((annotatedNamespace != null) && (!"".equals(annotatedNamespace))) {
        targetNamespace = annotatedNamespace;
      }
    }
    this.targetNamespace = targetNamespace;
    put("targetNamespace", targetNamespace);

    String partName = "return";
    if ((annotation != null) && (!"".equals(annotation.partName()))) {
      partName = annotation.partName();
    }
    this.partName = partName;
    put("partName", "return");

    DecoratedTypeMirror returnType = (DecoratedTypeMirror) method.getReturnType();
    String docComment = returnType.getDocComment();
    if ("".equals(docComment)) {
      docComment = null;
    }
    this.docComment = docComment;

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

  /**
   * The web method.
   *
   * @return The web method.
   */
  public WebMethod getWebMethod() {
    return method;
  }

  /**
   * The doc comment for this web result.
   *
   * @return The doc comment for this web result.
   */
  public String getDocComment() {
    return docComment;
  }

}
