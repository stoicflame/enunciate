package net.sf.enunciate.decorations.jaxws;

import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.TypeVisitor;
import org.apache.commons.beanutils.BeanUtils;

import java.util.HashMap;

/**
 * A decorated type mirror that is a web result.
 *
 * @author Ryan Heaton
 */
public class WebResult extends HashMap implements TypeMirror {

  private final TypeMirror delegate;

  public WebResult(TypeMirror delegate, WebMethod method) {
    this.delegate = delegate;

    try {
      putAll(BeanUtils.describe(delegate));
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }

    javax.jws.WebResult annotation = method.getAnnotation(javax.jws.WebResult.class);

    String name = "return";
    if (annotation != null) {
      name = annotation.name();
    }
    put("name", name);

    String targetNamespace = null;
    if (annotation != null) {
      targetNamespace = annotation.targetNamespace();
      if ((targetNamespace == null) || ("".equals(targetNamespace))) {
        targetNamespace = method.getDeclaringWebService().getTargetNamespace();
      }
    }
    put("targetNamespace", targetNamespace);

    put("partName", "return");
  }

  public void accept(TypeVisitor typeVisitor) {
    delegate.accept(typeVisitor);
  }

}
