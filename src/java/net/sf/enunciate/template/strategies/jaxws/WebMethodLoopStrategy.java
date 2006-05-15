package net.sf.enunciate.template.strategies.jaxws;

import net.sf.enunciate.template.strategies.EnunciateTemplateLoopStrategy;
import net.sf.enunciate.decorations.jaxws.WebService;
import net.sf.enunciate.decorations.jaxws.WebMethod;
import net.sf.jelly.apt.TemplateException;
import net.sf.jelly.apt.TemplateModel;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import net.sf.jelly.apt.strategies.MissingParameterException;

import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;

import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;

import javax.xml.ws.Holder;

/**
 * A loop strategy for all the web methods of an endpoint interface.
 *
 * @author Ryan Heaton
 */
public class WebMethodLoopStrategy extends EnunciateTemplateLoopStrategy<WebMethod> {

  private String var = "webMethod";
  private WebService endpointInterface;

  protected Iterator<WebMethod> getLoop(TemplateModel model) throws TemplateException {
    WebService endpointInterface = this.endpointInterface;
    if (endpointInterface == null) {
      endpointInterface = (WebService) model.getVariable("endpointInterface");
      
      if (endpointInterface == null) {
        throw new MissingParameterException("endpointInterface");
      }
    }

    Collection<WebMethod> webMethods = new ArrayList<WebMethod>();
    for (MethodDeclaration method : endpointInterface.getMethods()) {
      if (WebMethod.isWebMethod(method)) {
        //todo: handle in/out parameters.
        for (ParameterDeclaration parameter : method.getParameters()) {
          DecoratedTypeMirror parameterType = (DecoratedTypeMirror) parameter.getType();
          if (parameterType.isInstanceOf(Holder.class.getName())) {
            throw new UnsupportedOperationException(parameter.getPosition() + ": enunciate currently doesn't support in/out parameters.  Maybe someday...");
          }
        }

        webMethods.add(new WebMethod(method));
      }
    }

    return webMethods.iterator();
  }

  // Inherited.
  @Override
  protected void setupModelForLoop(TemplateModel model, WebMethod method, int index) throws TemplateException {
    super.setupModelForLoop(model, method, index);

    if (var != null) {
      model.setVariable(var, method);
    }
  }

  /**
   * The endpoint interface.
   *
   * @return The endpoint interface.
   */
  public WebService getEndpointInterface() {
    return endpointInterface;
  }

  /**
   * The endpoint interface.
   *
   * @param endpointInterface The endpoint interface.
   */
  public void setEndpointInterface(WebService endpointInterface) {
    this.endpointInterface = endpointInterface;
  }

  /**
   * The variable into which to store the current web method.
   *
   * @return The variable into which to store the current web method.
   */
  public String getVar() {
    return var;
  }

  /**
   * The variable into which to store the current web method.
   *
   * @param var The variable into which to store the current web method.
   */
  public void setVar(String var) {
    this.var = var;
  }
}
