package net.sf.enunciate.template.strategies.jaxws;

import net.sf.enunciate.template.strategies.EnunciateTemplateLoopStrategy;
import net.sf.enunciate.decorations.jaxws.WebFault;
import net.sf.enunciate.decorations.jaxws.WebService;
import net.sf.enunciate.decorations.jaxws.WebMethod;
import net.sf.enunciate.config.WsdlInfo;
import net.sf.jelly.apt.TemplateException;
import net.sf.jelly.apt.TemplateModel;
import net.sf.jelly.apt.strategies.MissingParameterException;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;

import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.ReferenceType;
import com.sun.mirror.type.DeclaredType;

/**
 * Loop through the web faults of a specified wsdl.
 *
 * @author Ryan Heaton
 */
public class WebFaultLoopStrategy extends EnunciateTemplateLoopStrategy<WebFault> {

  private String var = "webFault";
  private WsdlInfo wsdl;

  protected Iterator<WebFault> getLoop(TemplateModel model) throws TemplateException {
    WsdlInfo wsdl = this.wsdl;
    if (wsdl == null) {
      wsdl = (WsdlInfo) model.getVariable("wsdl");
      
      if (wsdl == null) {
        throw new MissingParameterException("wsdl");
      }
    }

    HashMap<String, TypeDeclaration> thrownDeclaredTypes = new HashMap<String, TypeDeclaration>();
    for (WebService ei : wsdl.getEndpointInterfaces()) {
      Collection<WebMethod> webMethods = ei.getWebMethods();
      for (WebMethod webMethod : webMethods) {
        for (ReferenceType thrownType : webMethod.getThrownTypes()) {
          if (thrownType instanceof DeclaredType) {
            TypeDeclaration thrownDeclaration = ((DeclaredType) thrownType).getDeclaration();
            if (thrownDeclaration != null) {
              thrownDeclaredTypes.put(thrownDeclaration.getQualifiedName(), thrownDeclaration);
            }
          }
        }
      }
    }

    Collection<TypeDeclaration> thrownTypes = thrownDeclaredTypes.values();

    Collection<WebFault> webFaults = new ArrayList<WebFault>();
    for (TypeDeclaration typeDeclaration : thrownTypes) {
      webFaults.add(new WebFault(typeDeclaration));
    }

    return webFaults.iterator();
  }

  @Override
  protected void setupModelForLoop(TemplateModel model, WebFault webFault, int index) throws TemplateException {
    super.setupModelForLoop(model, webFault, index);

    if (var != null) {
      model.setVariable(var, webFault);
    }
  }

  /**
   * The variable into which to store the web fault.
   *
   * @return The variable into which to store the web fault.
   */
  public String getVar() {
    return var;
  }

  /**
   * The variable into which to store the web fault.
   *
   * @param var The variable into which to store the web fault.
   */
  public void setVar(String var) {
    this.var = var;
  }

  /**
   * The WSDL from which to list the web faults.
   *
   * @return The WSDL from which to list the web faults.
   */
  public WsdlInfo getWsdl() {
    return wsdl;
  }

  /**
   * The WSDL from which to list the web faults.
   *
   * @param wsdl The WSDL from which to list the web faults.
   */
  public void setWsdl(WsdlInfo wsdl) {
    this.wsdl = wsdl;
  }


}
