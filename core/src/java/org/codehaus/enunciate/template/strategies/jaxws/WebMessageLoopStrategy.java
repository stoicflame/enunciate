package org.codehaus.enunciate.template.strategies.jaxws;

import org.codehaus.enunciate.config.WsdlInfo;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.jaxws.WebFault;
import org.codehaus.enunciate.contract.jaxws.WebMessage;
import org.codehaus.enunciate.contract.jaxws.WebMethod;
import org.codehaus.enunciate.template.strategies.EnunciateTemplateLoopStrategy;
import net.sf.jelly.apt.TemplateException;
import net.sf.jelly.apt.TemplateModel;
import net.sf.jelly.apt.strategies.MissingParameterException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Loop through the web messages unique to either a web method or a wsdl.  Priority is given to the specified web method.
 *
 * @author Ryan Heaton
 */
public class WebMessageLoopStrategy extends EnunciateTemplateLoopStrategy<WebMessage> {

  private String var = "webMessage";
  private boolean includeInput = true;
  private boolean includeOutput = true;
  private boolean includeHeaders = true;
  private boolean includeFaults = true;
  private WebMethod webMethod;
  private WsdlInfo wsdl;

  // Inherited.
  protected Iterator<WebMessage> getLoop(TemplateModel model) throws TemplateException {
    WebMethod webMethod = this.webMethod;
    WsdlInfo wsdlInfo = this.wsdl;
    if ((webMethod == null) && (wsdlInfo == null)) {
      throw new MissingParameterException("Either a webMethod or a wsdl must be specified to iterate over web messages.", "webMethod");
    }

    Collection<WebMessage> messages;
    if (webMethod != null) {
      messages = webMethod.getMessages();
    }
    else {
      messages = new ArrayList<WebMessage>();
      HashSet<String> foundFaults = new HashSet<String>();
      for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
        Collection<WebMethod> webMethods = ei.getWebMethods();
        for (WebMethod method : webMethods) {
          for (WebMessage webMessage : method.getMessages()) {
            if (webMessage.isFault() && !foundFaults.add(((WebFault) webMessage).getQualifiedName())) {
              continue;
            }

            messages.add(webMessage);
          }
        }
      }

    }

    Collection<WebMessage> io = new ArrayList<WebMessage>();
    for (WebMessage message : messages) {
      boolean include = (includeHeaders || !message.isHeader());
      include &= (includeOutput || !message.isOutput());
      include &= (includeInput || !message.isInput());
      include &= (includeFaults || !message.isFault());
      if (include) {
        io.add(message);
      }
    }

    return io.iterator();
  }

  // Inherited.
  @Override
  protected void setupModelForLoop(TemplateModel model, WebMessage webMessage, int index) throws TemplateException {
    super.setupModelForLoop(model, webMessage, index);

    if (var != null) {
      model.setVariable(var, webMessage);
    }
  }

  /**
   * The variable in which to put the message.
   *
   * @return The variable in which to put the message.
   */
  public String getVar() {
    return var;
  }

  /**
   * The variable in which to put the message.
   *
   * @param var The variable in which to put the message.
   */
  public void setVar(String var) {
    this.var = var;
  }

  /**
   * Whether or not to include input.
   *
   * @return Whether or not to include input.
   */
  public boolean isIncludeInput() {
    return includeInput;
  }

  /**
   * Whether or not to include input.
   *
   * @param includeInput Whether or not to include input.
   */
  public void setIncludeInput(boolean includeInput) {
    this.includeInput = includeInput;
  }

  /**
   * Whether or not to include output.
   *
   * @return Whether or not to include output.
   */
  public boolean isIncludeOutput() {
    return includeOutput;
  }

  /**
   * Whether or not to include output.
   *
   * @param includeOutput Whether or not to include output.
   */
  public void setIncludeOutput(boolean includeOutput) {
    this.includeOutput = includeOutput;
  }


  /**
   * Whether or not to include headers.
   *
   * @return Whether or not to include headers.
   */
  public boolean isIncludeHeaders() {
    return includeHeaders;
  }

  /**
   * Whether or not to include headers.
   *
   * @param includeHeaders Whether or not to include headers.
   */
  public void setIncludeHeaders(boolean includeHeaders) {
    this.includeHeaders = includeHeaders;
  }

  /**
   * Whether or not to include faults.
   *
   * @return Whether or not to include faults.
   */
  public boolean isIncludeFaults() {
    return includeFaults;
  }

  /**
   * Whether or not to include faults.
   *
   * @param includeFaults Whether or not to include faults.
   */
  public void setIncludeFaults(boolean includeFaults) {
    this.includeFaults = includeFaults;
  }

  /**
   * The web method.
   *
   * @return The web method.
   */
  public WebMethod getWebMethod() {
    return webMethod;
  }

  /**
   * The web method.
   *
   * @param webMethod The web method.
   */
  public void setWebMethod(WebMethod webMethod) {
    this.webMethod = webMethod;
  }

  /**
   * The wsdl containing the web messages.
   *
   * @return The wsdl containing the web messages.
   */
  public WsdlInfo getWsdl() {
    return wsdl;
  }

  /**
   * The wsdl containing the web messages.
   *
   * @param wsdl The wsdl containing the web messages.
   */
  public void setWsdl(WsdlInfo wsdl) {
    this.wsdl = wsdl;
  }

}
