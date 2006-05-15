package net.sf.enunciate.template.strategies.jaxws;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.TypeDeclaration;
import net.sf.enunciate.decorations.jaxws.BindingType;
import net.sf.enunciate.decorations.jaxws.WebService;
import net.sf.enunciate.template.strategies.EnunciateTemplateLoopStrategy;
import net.sf.jelly.apt.TemplateException;
import net.sf.jelly.apt.TemplateModel;
import net.sf.jelly.apt.strategies.MissingParameterException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Loop through the binding types of a given endpoint interface.
 *
 * @author Ryan Heaton
 */
public class BindingTypeLoopStrategy extends EnunciateTemplateLoopStrategy<BindingType> {

  private String var = "bindingType";
  private WebService endpointInterface;

  //Inherited.
  protected Iterator<BindingType> getLoop(TemplateModel model) throws TemplateException {
    WebService endpointInterface = this.endpointInterface;

    if (endpointInterface == null) {
      endpointInterface = (WebService) model.getVariable("endpointInterface");

      if (endpointInterface == null) {
        throw new MissingParameterException("endpointInterface");
      }
    }

    Collection<BindingType> bindingTypes = new ArrayList<BindingType>();

    if (!endpointInterface.isEndpointImplmentation()) {
      //if the specified ei is not an implementation, we iterate through each binding type of its implementing classes.
      String eifqn = endpointInterface.getQualifiedName();
      AnnotationProcessorEnvironment env = getAnnotationProcessorEnvironment();
      Collection<Declaration> declarations = env.getDeclarationsAnnotatedWith((AnnotationTypeDeclaration) env.getTypeDeclaration(javax.jws.WebService.class.getName()));
      for (Declaration declaration : declarations) {
        WebService webService = new WebService((TypeDeclaration) declaration);
        if ((eifqn.equals(webService.getQualifiedName()) || (eifqn.equals(webService.getEndpointInterface())))) {
          String bindingType = webService.getBindingType();

          if (bindingType != null) {
            bindingTypes.add(BindingType.fromNamespace(bindingType));
          }
        }
      }
    }
    else {
      //if the specified ei is an implementation, just use its binding type.
      String bindingType = endpointInterface.getBindingType();
      if (bindingType != null) {
        bindingTypes.add(BindingType.fromNamespace(bindingType));
      }
    }

    if (bindingTypes.isEmpty()) {
      //spec says if no bindings are present, use SOAP 1.1
      bindingTypes.add(BindingType.SOAP_1_1);
    }

    return bindingTypes.iterator();
  }

  //Inherited.
  @Override
  protected void setupModelForLoop(TemplateModel model, BindingType bindingType, int index) throws TemplateException {
    super.setupModelForLoop(model, bindingType, index);

    if (var != null) {
      model.setVariable(var, bindingType);
    }
  }

  /**
   * The variable into which to store the binding type.
   *
   * @return The variable into which to store the binding type.
   */
  public String getVar() {
    return var;
  }

  /**
   * The variable into which to store the binding type.
   *
   * @param var The variable into which to store the binding type.
   */
  public void setVar(String var) {
    this.var = var;
  }

  /**
   * The endpoint interface for which to iterate over each binding type.
   *
   * @return The endpoint interface for which to iterate over each binding type.
   */
  public WebService getEndpointInterface() {
    return endpointInterface;
  }

  /**
   * The endpoint interface for which to iterate over each binding type.
   *
   * @param endpointInterface The endpoint interface for which to iterate over each binding type.
   */
  public void setEndpointInterface(WebService endpointInterface) {
    this.endpointInterface = endpointInterface;
  }

}
