package net.sf.enunciate.modules.xfire_client;

import freemarker.template.*;
import net.sf.enunciate.EnunciateException;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.config.SchemaInfo;
import net.sf.enunciate.config.WsdlInfo;
import net.sf.enunciate.contract.jaxb.TypeDefinition;
import net.sf.enunciate.contract.jaxws.*;
import net.sf.enunciate.contract.validation.Validator;
import net.sf.enunciate.main.Enunciate;
import net.sf.enunciate.modules.FreemarkerDeploymentModule;
import net.sf.enunciate.modules.xfire_client.annotations.*;
import net.sf.enunciate.modules.xfire_client.config.ClientPackageConversion;
import net.sf.enunciate.modules.xfire_client.config.XFireClientRuleSet;
import net.sf.enunciate.util.ClassDeclarationComparator;
import net.sf.jelly.apt.decorations.JavaDoc;
import net.sf.jelly.apt.freemarker.FreemarkerJavaDoc;
import org.apache.commons.digester.RuleSet;
import org.codehaus.xfire.annotations.HandlerChainAnnotation;
import org.codehaus.xfire.annotations.WebParamAnnotation;
import org.codehaus.xfire.annotations.soap.SOAPBindingAnnotation;

import javax.jws.soap.SOAPBinding;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;

/**
 * Deployment module for the XFire client code.
 *
 * @author Ryan Heaton
 */
public class XFireClientDeploymentModule extends FreemarkerDeploymentModule {

  private String jarName = null;
  private String defaultHost = "localhost";
  private int defaultPort = 8080;
  private String defaultContext = "/";
  private final Map<String, String> clientPackageConversions;
  private final XFireClientRuleSet configurationRules;
  private String uuid;

  public XFireClientDeploymentModule() {
    this.clientPackageConversions = new HashMap<String, String>();
    this.configurationRules = new XFireClientRuleSet();
    this.uuid = String.valueOf(System.currentTimeMillis());
  }

  /**
   * @return "xfire-client"
   */
  @Override
  public String getName() {
    return "xfire-client";
  }

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException {
    EnunciateFreemarkerModel model = getModel();

    //generate the JDK 1.4 client code.
    Map<String, String> conversions = getClientPackageConversions();
    model.put("packageFor", new ClientPackageForMethod(conversions));
    ClientClassnameForMethod classnameFor = new ClientClassnameForMethod(conversions, false);
    model.put("classnameFor", classnameFor);
    model.put("componentTypeFor", new ComponentTypeForMethod(conversions, false));

    String uuid = this.uuid;
    model.put("uuid", uuid);
    model.put("defaultHost", getDefaultHost());
    model.put("defaultContext", getDefaultContext());
    model.put("defaultPort", String.valueOf(getDefaultPort()));

    URL xfireEnumTemplate = getTemplateURL("xfire-enum-type.fmt");
    URL xfireSimpleTemplate = getTemplateURL("xfire-simple-type.fmt");
    URL xfireComplexTemplate = getTemplateURL("xfire-complex-type.fmt");

    URL eiTemplate = getTemplateURL("jdk14/client-endpoint-interface.fmt");
    URL soapImplTemplate = getTemplateURL("jdk14/client-soap-endpoint-impl.fmt");
    URL faultTemplate = getTemplateURL("jdk14/client-web-fault.fmt");
    URL enumTypeTemplate = getTemplateURL("jdk14/client-enum-type.fmt");
    URL simpleTypeTemplate = getTemplateURL("jdk14/client-simple-type.fmt");
    URL complexTypeTemplate = getTemplateURL("jdk14/client-complex-type.fmt");
    URL faultBeanTemplate = getTemplateURL("jdk14/client-fault-bean.fmt");
    URL requestBeanTemplate = getTemplateURL("jdk14/client-request-bean.fmt");
    URL responseBeanTemplate = getTemplateURL("jdk14/client-response-bean.fmt");

    //process the endpoint interfaces and gather the list of web faults...
    ExplicitWebAnnotations annotations = new ExplicitWebAnnotations();
    TreeSet<WebFault> allFaults = new TreeSet<WebFault>(new ClassDeclarationComparator());
    List<String> typeList = new ArrayList<String>();
    for (WsdlInfo wsdlInfo : model.getNamespacesToWSDLs().values()) {
      for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
        //first process the templates for the endpoint interfaces.
        model.put("endpointInterface", ei);

        processTemplate(eiTemplate, model);
        processTemplate(soapImplTemplate, model);
        addExplicitAnnotations(annotations, ei, classnameFor);

        for (WebMethod webMethod : ei.getWebMethods()) {
          for (WebMessage webMessage : webMethod.getMessages()) {
            if (webMessage instanceof RequestWrapper) {
              model.put("message", webMessage);
              processTemplate(requestBeanTemplate, model);
              typeList.add(getBeanName(classnameFor, ((RequestWrapper) webMessage).getRequestBeanName()));
            }
            else if (webMessage instanceof ResponseWrapper) {
              model.put("message", webMessage);
              processTemplate(responseBeanTemplate, model);
              typeList.add(getBeanName(classnameFor, ((ResponseWrapper) webMessage).getResponseBeanName()));
            }
            else if (webMessage instanceof RPCInputMessage) {
              RPCInputMessage rpcInputMessage = ((RPCInputMessage) webMessage);
              model.put("message", new RPCInputRequestBeanAdapter(rpcInputMessage));
              processTemplate(requestBeanTemplate, model);
              typeList.add(getBeanName(classnameFor, rpcInputMessage.getRequestBeanName()));
            }
            else if (webMessage instanceof RPCOutputMessage) {
              RPCOutputMessage outputMessage = ((RPCOutputMessage) webMessage);
              model.put("message", new RPCOutputResponseBeanAdapter(outputMessage));
              processTemplate(responseBeanTemplate, model);
              typeList.add(getBeanName(classnameFor, outputMessage.getResponseBeanName()));
            }
            else if (webMessage instanceof WebFault) {
              allFaults.add((WebFault) webMessage);
            }
          }

          addExplicitAnnotations(annotations, webMethod, classnameFor);
        }
      }
    }

    //process the gathered web faults.
    for (WebFault webFault : allFaults) {
      String faultClass = classnameFor.convert(webFault);
      boolean implicit = webFault.isImplicitSchemaElement();
      String faultBean = implicit ? getBeanName(classnameFor, webFault.getImplicitFaultBeanQualifiedName()) : classnameFor.convert(webFault.getExplicitFaultBean());

      model.put("fault", webFault);
      processTemplate(faultTemplate, model);

      if (implicit) {
        processTemplate(faultBeanTemplate, model);
        typeList.add(faultBean);
      }

      annotations.fault2WebFault.put(faultClass, new WebFaultAnnotation(webFault.getElementName(), webFault.getTargetNamespace(), faultBean, implicit));
    }

    //process each type for client-side stubs.
    for (SchemaInfo schemaInfo : model.getNamespacesToSchemas().values()) {
      for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
        model.put("type", typeDefinition);
        URL template = typeDefinition.isEnum() ? enumTypeTemplate : typeDefinition.isSimple() ? simpleTypeTemplate : complexTypeTemplate;
        processTemplate(template, model);

        template = typeDefinition.isEnum() ? xfireEnumTemplate : typeDefinition.isSimple() ? xfireSimpleTemplate : xfireComplexTemplate;
        processTemplate(template, model);

        typeList.add(classnameFor.convert(typeDefinition));
      }
    }

    //todo: generate the JDK 1.5 client code.

    enunciate.setProperty("client.annotations", annotations);
    enunciate.setProperty("client.type.list", typeList);
  }

  protected void addExplicitAnnotations(ExplicitWebAnnotations annotations, EndpointInterface ei, ClientClassnameForMethod conversion) {
    String clazz = conversion.convert(ei);

    SerializableWebServiceAnnotation wsAnnotation = new SerializableWebServiceAnnotation();
    wsAnnotation.setName(ei.getPortTypeName());
    wsAnnotation.setPortName(ei.getSimpleName() + "SOAPPort");
    wsAnnotation.setServiceName(ei.getServiceName());
    wsAnnotation.setTargetNamespace(ei.getTargetNamespace());
    annotations.class2WebService.put(clazz, wsAnnotation);

    SerializableSOAPBindingAnnotation sbAnnotation = new SerializableSOAPBindingAnnotation();
    sbAnnotation.setStyle(ei.getSoapBindingStyle() == SOAPBinding.Style.DOCUMENT ? SOAPBindingAnnotation.STYLE_DOCUMENT : SOAPBindingAnnotation.STYLE_RPC);
    sbAnnotation.setParameterStyle(ei.getSoapParameterStyle() == SOAPBinding.ParameterStyle.BARE ? SOAPBindingAnnotation.PARAMETER_STYLE_BARE : SOAPBindingAnnotation.PARAMETER_STYLE_WRAPPED);
    sbAnnotation.setUse(ei.getSoapUse() == SOAPBinding.Use.ENCODED ? SOAPBindingAnnotation.USE_ENCODED : SOAPBindingAnnotation.USE_LITERAL);
    annotations.class2SOAPBinding.put(clazz, sbAnnotation);

    HandlerChainAnnotation hcAnnotation = null; //todo: support this?

  }

  protected void addExplicitAnnotations(ExplicitWebAnnotations annotations, WebMethod webMethod, ClientClassnameForMethod conversion) {
    String classname = conversion.convert(webMethod.getDeclaringEndpointInterface());
    String methodName = webMethod.getSimpleName();
    String methodKey = String.format("%s.%s", classname, methodName);

    SerializableWebMethodAnnotation wmAnnotation = new SerializableWebMethodAnnotation();
    wmAnnotation.setOperationName(webMethod.getOperationName());
    wmAnnotation.setAction(webMethod.getAction());
    annotations.method2WebMethod.put(methodKey, wmAnnotation);

    WebResult webResult = webMethod.getWebResult();
    SerializableWebResultAnnotation wrAnnotation = new SerializableWebResultAnnotation();
    wrAnnotation.setHeader(webResult.isHeader());
    wrAnnotation.setName(webResult.getName());
    wrAnnotation.setPartName(webResult.getPartName());
    wrAnnotation.setTargetNamespace(webResult.getTargetNamespace());

    annotations.method2WebResult.put(methodKey, wrAnnotation);
    if (webMethod.isOneWay()) {
      annotations.oneWayMethods.add(methodKey);
    }

    for (WebMessage webMessage : webMethod.getMessages()) {
      if (webMessage instanceof RequestWrapper) {
        RequestWrapper requestWrapper = (RequestWrapper) webMessage;
        String beanName = getBeanName(conversion, requestWrapper.getRequestBeanName());
        RequestWrapperAnnotation annotation = new RequestWrapperAnnotation(requestWrapper.getElementName(), requestWrapper.getElementNamespace(), beanName);
        annotations.method2RequestWrapper.put(methodKey, annotation);
        Collection<ImplicitChildElement> childElements = requestWrapper.getChildElements();
        String[] propertyOrder = new String[childElements.size()];
        int i = 0;
        for (ImplicitChildElement childElement : childElements) {
          propertyOrder[i] = childElement.getElementName();
          i++;
        }
        annotations.class2PropertyOrder.put(beanName, propertyOrder);
      }
      else if (webMessage instanceof ResponseWrapper) {
        ResponseWrapper responseWrapper = (ResponseWrapper) webMessage;
        String beanName = getBeanName(conversion, responseWrapper.getResponseBeanName());
        ResponseWrapperAnnotation annotation = new ResponseWrapperAnnotation(responseWrapper.getElementName(), responseWrapper.getElementNamespace(), beanName);
        annotations.method2ResponseWrapper.put(methodKey, annotation);
        Collection<ImplicitChildElement> childElements = responseWrapper.getChildElements();
        String[] propertyOrder = new String[childElements.size()];
        int i = 0;
        for (ImplicitChildElement childElement : childElements) {
          propertyOrder[i] = childElement.getElementName();
          i++;
        }
        annotations.class2PropertyOrder.put(beanName, propertyOrder);
      }
    }

    int i = 0;
    for (WebParam webParam : webMethod.getWebParameters()) {
      SerializableWebParamAnnotation wpAnnotation = new SerializableWebParamAnnotation();
      wpAnnotation.setHeader(webParam.isHeader());
      wpAnnotation.setMode(webParam.getMode() == javax.jws.WebParam.Mode.INOUT ? WebParamAnnotation.MODE_INOUT : webParam.getMode() == javax.jws.WebParam.Mode.OUT ? WebParamAnnotation.MODE_OUT : WebParamAnnotation.MODE_IN);
      wpAnnotation.setName(webParam.getElementName());
      wpAnnotation.setPartName(webParam.getPartName());
      annotations.method2WebParam.put(String.format("%s.%s", methodKey, i), wpAnnotation);
      i++;
    }
  }

  /**
   * Get the bean name for a specified string.
   *
   * @param conversion The conversion to use.
   * @param preconvert The pre-converted fqn.
   * @return The converted fqn.
   */
  protected String getBeanName(ClientClassnameForMethod conversion, String preconvert) {
    String pckg = conversion.convert(preconvert.substring(0, preconvert.lastIndexOf('.')));
    String simpleName = preconvert.substring(preconvert.lastIndexOf('.') + 1);
    return pckg + "." + simpleName;
  }

  @Override
  protected void doCompile() throws EnunciateException, IOException {
    Enunciate enunciate = getEnunciate();
    File typesDir = getXFireTypesDir();
    Collection<String> typeFiles = enunciate.getJavaFiles(typesDir);
    Collection<String> jdk14Files = enunciate.getJavaFiles(getJdk14Dir());
    jdk14Files.addAll(typeFiles);

    enunciate.invokeJavac(enunciate.getClasspath(), getJdk14CompileDir(), Arrays.asList("-source", "1.4"), jdk14Files.toArray(new String[jdk14Files.size()]));

    List<String> typeList = (List<String>) enunciate.getProperty("client.type.list");
    if (typeList == null) {
      throw new EnunciateException("The client type list wasn't generated.");
    }
    PrintWriter writer = new PrintWriter(new File(getJdk14CompileDir(), uuid + ".types"));
    for (String type : typeList) {
      writer.println(type);
    }
    writer.close();

    ExplicitWebAnnotations annotations = (ExplicitWebAnnotations) enunciate.getProperty("client.annotations");
    if (annotations == null) {
      throw new EnunciateException("The client annotations weren't generated.");
    }

    FileOutputStream fos = new FileOutputStream(new File(getJdk14CompileDir(), uuid + ".annotations"));
    try {
      annotations.writeTo(fos);
    }
    catch (Exception e) {
      throw new EnunciateException(e);
    }
    finally {
      fos.close();
    }

    //todo: compile the jdk 1.5 client classes.

  }

  @Override
  protected void doBuild() throws EnunciateException, IOException {
    Enunciate enunciate = getEnunciate();
    String jarName = getJarName();

    if (jarName == null) {
      String label = "enunciate";
      if ((enunciate.getConfig() != null) && (enunciate.getConfig().getLabel() != null)) {
        label = enunciate.getConfig().getLabel();
      }

      jarName = label + "-client.jar";
    }

    File jdk14Jar = new File(getBuildDir(), jarName);
    enunciate.zip(getJdk14CompileDir(), jdk14Jar);
    enunciate.setProperty("client.jdk14.jar", jdk14Jar);
  }

  /**
   * Get a template URL for the template of the given name.
   *
   * @param template The specified template.
   * @return The URL to the specified template.
   */
  protected URL getTemplateURL(String template) {
    return XFireClientDeploymentModule.class.getResource(template);
  }

  /**
   * The directory for compiling.
   *
   * @return The directory for compiling.
   */
  protected File getCompileDir() {
    return new File(getEnunciate().getCompileDir(), "xfire-client");
  }

  /**
   * The directory for building.
   *
   * @return The directory for building.
   */
  protected File getBuildDir() {
    return new File(getEnunciate().getBuildDir(), "xfire-client");
  }

  /**
   * The directory for compiling the jdk 14 compatible classes.
   *
   * @return The directory for compiling the jdk 14 compatible classes.
   */
  protected File getJdk14CompileDir() {
    return new File(getCompileDir(), "jdk14");
  }

  /**
   * The directory for the jdk14 client files.
   *
   * @return The directory for the jdk14 client files.
   */
  protected File getJdk14Dir() {
    return new File(new File(getEnunciate().getGenerateDir(), "xfireclient"), "jdk14");
  }

  /**
   * The directory for the jdk14 client files.
   *
   * @return The directory for the jdk14 client files.
   */
  protected File getXFireTypesDir() {
    return new File(new File(getEnunciate().getGenerateDir(), "xfireclient"), "types");
  }

  /**
   * The name of the jar.
   *
   * @return The name of the jar.
   */
  public String getJarName() {
    return jarName;
  }

  /**
   * The name of the jar.
   *
   * @param jarName The name of the jar.
   */
  public void setJarName(String jarName) {
    this.jarName = jarName;
  }

  /**
   * The default host to which to point the client.
   *
   * @return The default host to which to point the client.
   */
  public String getDefaultHost() {
    return defaultHost;
  }

  /**
   * The default host to which to point the client.
   *
   * @param defaultHost The default host to which to point the client.
   */
  public void setDefaultHost(String defaultHost) {
    this.defaultHost = defaultHost;
  }

  /**
   * The default port to which to point the client.
   *
   * @return The default port to which to point the client.
   */
  public int getDefaultPort() {
    return defaultPort;
  }

  /**
   * The default port to which to point the client.
   *
   * @param defaultPort The default port to which to point the client.
   */
  public void setDefaultPort(int defaultPort) {
    this.defaultPort = defaultPort;
  }

  /**
   * The default context to which to point the client.
   *
   * @return The default context to which to point the client.
   */
  public String getDefaultContext() {
    return defaultContext;
  }

  /**
   * The default context to which to point the client.
   *
   * @param defaultContext The default context to which to point the client.
   */
  public void setDefaultContext(String defaultContext) {
    if (defaultContext == null) {
      defaultContext = "/";
    }
    else {
      if (!defaultContext.startsWith("/")) {
        defaultContext = "/" + defaultContext;
      }

      if (!defaultContext.endsWith("/")) {
        defaultContext = defaultContext + "/";
      }
    }

    this.defaultContext = defaultContext;
  }

  /**
   * A unique id to associate with this build of the xfire client.
   *
   * @return A unique id to associate with this build of the xfire client.
   */
  public String getUuid() {
    return uuid;
  }

  /**
   * A unique id to associate with this build of the xfire client.
   *
   * @param uuid A unique id to associate with this build of the xfire client.
   */
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  /**
   * An XFire configuration rule set.
   *
   * @return An XFire configuration rule set.
   */
  @Override
  public RuleSet getConfigurationRules() {
    return this.configurationRules;
  }

  /**
   * An xfire-client validator.
   *
   * @return An xfire-client validator.
   */
  @Override
  public Validator getValidator() {
    return new XFireClientValidator();
  }

  @Override
  protected ObjectWrapper getObjectWrapper() {
    return new DefaultObjectWrapper() {
      @Override
      public TemplateModel wrap(Object obj) throws TemplateModelException {
        if (obj instanceof JavaDoc) {
          return new FreemarkerJavaDoc((JavaDoc) obj);
        }

        return super.wrap(obj);
      }
    };
  }

  /**
   * The client package conversions.
   *
   * @return The client package conversions.
   */
  public Map<String, String> getClientPackageConversions() {
    return clientPackageConversions;
  }

  /**
   * Add a client package conversion.
   *
   * @param conversion The conversion to add.
   */
  public void addClientPackageConversion(ClientPackageConversion conversion) {
    String from = conversion.getFrom();
    String to = conversion.getTo();

    if (from == null) {
      throw new IllegalArgumentException("A 'from' attribute must be specified on a clientPackageConversion element.");
    }

    if (to == null) {
      throw new IllegalArgumentException("A 'to' attribute must be specified on a clientPackageConversion element.");
    }

    this.clientPackageConversions.put(from, to);
  }

}
