package net.sf.enunciate.template.strategies.jaxws;

import net.sf.enunciate.template.strategies.EnunciateTemplateLoopStrategy;
import net.sf.enunciate.decorations.jaxws.WebMessage;
import net.sf.enunciate.decorations.jaxws.WebMethod;
import net.sf.jelly.apt.TemplateException;
import net.sf.jelly.apt.TemplateModel;
import net.sf.jelly.apt.strategies.MissingParameterException;

import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;

/**
 * Loop through the web messages of a given web method.
 *
 * @author Ryan Heaton
 */
public class WebMessageLoopStrategy extends EnunciateTemplateLoopStrategy<WebMessage> {

  private String var = "webMessage";
  private boolean includeInput = true;
  private boolean includeOutput = true;
  private boolean includeHeaders = true;
  private boolean includeFaults = true;
  private boolean includeComplex = true;
  private boolean includeSimple = true;
  private WebMethod webMethod;

  // Inherited.
  protected Iterator<WebMessage> getLoop(TemplateModel model) throws TemplateException {
    WebMethod webMethod = this.webMethod;
    if (webMethod == null) {
      webMethod = (WebMethod) model.getVariable("webMethod");

      if (webMethod == null) {
        throw new MissingParameterException("webMethod");
      }
    }

    Collection<WebMessage> io = new ArrayList<WebMessage>();
    Collection<WebMessage> messages = webMethod.getMessages();
    for (WebMessage message : messages) {
      boolean include = (includeHeaders || !message.isHeader());
      include &= (includeComplex || !message.isComplex());
      include &= (includeOutput || !message.isOutput());
      include &= (includeInput || !message.isInput());
      include &= (includeFaults || !message.isFault());
      include &= (includeSimple || !message.isSimple());
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
   * Whether or not to include simple messages.
   *
   * @return Whether or not to include simple messages.
   */
  public boolean isIncludeSimple() {
    return includeSimple;
  }

  /**
   * Whether or not to include simple messages.
   *
   * @param includeSimple Whether or not to include simple messages.
   */
  public void setIncludeSimple(boolean includeSimple) {
    this.includeSimple = includeSimple;
  }

  /**
   * Whether or not to include complex messages.
   *
   * @return Whether or not to include complex messages.
   */
  public boolean isIncludeComplex() {
    return includeComplex;
  }

  /**
   * Whether or not to include complex messages.
   *
   * @param includeComplex Whether or not to include complex messages.
   */
  public void setIncludeComplex(boolean includeComplex) {
    this.includeComplex = includeComplex;
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

}
