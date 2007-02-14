package net.sf.enunciate.modules.xfire_client;

import freemarker.template.*;
import net.sf.enunciate.EnunciateException;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.config.SchemaInfo;
import net.sf.enunciate.config.WsdlInfo;
import net.sf.enunciate.config.EnunciateConfiguration;
import net.sf.enunciate.contract.jaxb.RootElementDeclaration;
import net.sf.enunciate.contract.jaxb.TypeDefinition;
import net.sf.enunciate.contract.jaxws.*;
import net.sf.enunciate.contract.validation.Validator;
import net.sf.enunciate.main.Enunciate;
import net.sf.enunciate.main.NamedFileArtifact;
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
import javax.xml.namespace.QName;
import java.io.*;
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
  private ExplicitWebAnnotations generatedAnnotations = null;
  private List<String> generatedTypeList = null;

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
    //load the references to the templates....
    URL xfireEnumTemplate = getTemplateURL("xfire-enum-type.fmt");
    URL xfireSimpleTemplate = getTemplateURL("xfire-simple-type.fmt");
    URL xfireComplexTemplate = getTemplateURL("xfire-complex-type.fmt");

    URL eiTemplate = getTemplateURL("client-endpoint-interface.fmt");
    URL soapImplTemplate = getTemplateURL("client-soap-endpoint-impl.fmt");
    URL faultTemplate = getTemplateURL("client-web-fault.fmt");
    URL simpleTypeTemplate = getTemplateURL("client-simple-type.fmt");
    URL complexTypeTemplate = getTemplateURL("client-complex-type.fmt");
    URL faultBeanTemplate = getTemplateURL("client-fault-bean.fmt");
    URL requestBeanTemplate = getTemplateURL("client-request-bean.fmt");
    URL responseBeanTemplate = getTemplateURL("client-response-bean.fmt");
    URL jdk14EnumTypeTemplate = getTemplateURL("client-jdk14-enum-type.fmt");
    URL jdk15EnumTypeTemplate = getTemplateURL("client-jdk15-enum-type.fmt");


    //set up the model, first allowing for jdk 14 compatability.
    EnunciateFreemarkerModel model = getModel();
    Map<String, String> conversions = getClientPackageConversions();
    ClientClassnameForMethod classnameFor = new ClientClassnameForMethod(conversions);
    ComponentTypeForMethod componentTypeFor = new ComponentTypeForMethod(conversions);
    model.put("packageFor", new ClientPackageForMethod(conversions));
    model.put("classnameFor", classnameFor);
    model.put("componentTypeFor", componentTypeFor);

    String uuid = this.uuid;
    model.put("uuid", uuid);

    String defaultProtocol = "http";
    String defaultHost = "localhost";
    String defaultContext = "";
    EnunciateConfiguration config = getEnunciate().getConfig();
    if (config != null) {
      if (config.getDeploymentProtocol() != null) {
        defaultProtocol = config.getDeploymentProtocol();
      }

      if (config.getDeploymentHost() != null) {
        defaultHost = config.getDeploymentHost();
      }

      if (config.getDeploymentContext() != null) {
        defaultContext = config.getDeploymentContext();
      }
    }

    if ((defaultContext.length() > 0) && (defaultContext.startsWith("/"))) {
      //no leading slashes in the context....
      defaultContext = defaultContext.substring(1);
    }

    if ((defaultContext.length() > 0) && (!defaultContext.endsWith("/"))) {
      //make sure there's an ending slash...
      defaultContext = defaultContext + "/";
    }

    model.put("defaultProtocol", defaultProtocol);
    model.put("defaultHost", defaultHost);
    model.put("defaultContext", defaultContext);


    // First, generate everything that is common to both jdk 14 and jdk 15
    // This includes all request/response beans and all xfire types.
    // Also, we're going to gather the annotation information, the type list,
    // and the list of unique web faults.
    info("Generating the XFire client classes that are common to both jdk 1.4 and jdk 1.5.");
    model.setFileOutputDirectory(getCommonJdkGenerateDir());
    generatedAnnotations = new ExplicitWebAnnotations();
    generatedTypeList = new ArrayList<String>();
    TreeSet<WebFault> allFaults = new TreeSet<WebFault>(new ClassDeclarationComparator());

    // Process the annotations, the request/response beans, and gather the set of web faults
    // for each endpoint interface.
    for (WsdlInfo wsdlInfo : model.getNamespacesToWSDLs().values()) {
      for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
        addExplicitAnnotations(ei, classnameFor);

        for (WebMethod webMethod : ei.getWebMethods()) {
          for (WebMessage webMessage : webMethod.getMessages()) {
            if (webMessage instanceof RequestWrapper) {
              model.put("message", webMessage);
              processTemplate(requestBeanTemplate, model);
              generatedTypeList.add(getBeanName(classnameFor, ((RequestWrapper) webMessage).getRequestBeanName()));
            }
            else if (webMessage instanceof ResponseWrapper) {
              model.put("message", webMessage);
              processTemplate(responseBeanTemplate, model);
              generatedTypeList.add(getBeanName(classnameFor, ((ResponseWrapper) webMessage).getResponseBeanName()));
            }
            else if (webMessage instanceof RPCInputMessage) {
              RPCInputMessage rpcInputMessage = ((RPCInputMessage) webMessage);
              model.put("message", new RPCInputRequestBeanAdapter(rpcInputMessage));
              processTemplate(requestBeanTemplate, model);
              generatedTypeList.add(getBeanName(classnameFor, rpcInputMessage.getRequestBeanName()));
            }
            else if (webMessage instanceof RPCOutputMessage) {
              RPCOutputMessage outputMessage = ((RPCOutputMessage) webMessage);
              model.put("message", new RPCOutputResponseBeanAdapter(outputMessage));
              processTemplate(responseBeanTemplate, model);
              generatedTypeList.add(getBeanName(classnameFor, outputMessage.getResponseBeanName()));
            }
            else if (webMessage instanceof WebFault) {
              allFaults.add((WebFault) webMessage);
            }
          }

          addExplicitAnnotations(webMethod, classnameFor);
        }
      }
    }

    //gather the annotation information and process the possible beans for each web fault.
    for (WebFault webFault : allFaults) {
      String faultClass = classnameFor.convert(webFault);
      boolean implicit = webFault.isImplicitSchemaElement();
      String faultBean = implicit ? getBeanName(classnameFor, webFault.getImplicitFaultBeanQualifiedName()) : classnameFor.convert(webFault.getExplicitFaultBean());

      if (implicit) {
        model.put("fault", webFault);
        processTemplate(faultBeanTemplate, model);
        generatedTypeList.add(faultBean);
      }

      String faultElementName = webFault.isImplicitSchemaElement() ? webFault.getElementName() : webFault.getExplicitFaultBean().getName();
      String faultElementNamespace = webFault.isImplicitSchemaElement() ? webFault.getTargetNamespace() : webFault.getExplicitFaultBean().getNamespace();
      this.generatedAnnotations.fault2WebFault.put(faultClass, new WebFaultAnnotation(faultElementName, faultElementNamespace, faultBean, implicit));
    }

    //process each xfire type for client-side stubs.
    for (SchemaInfo schemaInfo : model.getNamespacesToSchemas().values()) {
      for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
        model.put("type", typeDefinition);
        RootElementDeclaration rootElement = model.findRootElementDeclaration(typeDefinition);
        if (rootElement != null) {
          model.put("rootElementName", new QName(rootElement.getNamespace(), rootElement.getName()));
        }
        else {
          model.remove("rootElementName");
        }
        
        URL template = typeDefinition.isEnum() ? xfireEnumTemplate : typeDefinition.isSimple() ? xfireSimpleTemplate : xfireComplexTemplate;
        processTemplate(template, model);

        if (!typeDefinition.isAbstract()) {
          generatedTypeList.add(classnameFor.convert(typeDefinition));
        }
      }

      for (RootElementDeclaration rootElementDeclaration : schemaInfo.getGlobalElements()) {
        addExplicitAnnotations(rootElementDeclaration, classnameFor);
      }
    }
    model.remove("rootElementName");


    //Now, generate the jdk14-compatable client-side stubs.
    info("Generating the XFire client classes for jdk 1.4.");
    model.setFileOutputDirectory(getJdk14GenerateDir());
    for (WsdlInfo wsdlInfo : model.getNamespacesToWSDLs().values()) {
      for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
        model.put("endpointInterface", ei);

        processTemplate(eiTemplate, model);
        processTemplate(soapImplTemplate, model);
      }
    }

    for (WebFault webFault : allFaults) {
      model.put("fault", webFault);
      processTemplate(faultTemplate, model);
    }

    for (SchemaInfo schemaInfo : model.getNamespacesToSchemas().values()) {
      for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
        model.put("type", typeDefinition);
        URL template = typeDefinition.isEnum() ? jdk14EnumTypeTemplate : typeDefinition.isSimple() ? simpleTypeTemplate : complexTypeTemplate;
        processTemplate(template, model);
      }
    }


    //Now enable jdk-15 compatability and generate those client-side stubs.
    info("Generating the XFire client classes for jdk 1.5.");
    model.setFileOutputDirectory(getJdk15GenerateDir());
    classnameFor.setJdk15(true);
    componentTypeFor.setJdk15(true);
    for (WsdlInfo wsdlInfo : model.getNamespacesToWSDLs().values()) {
      for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
        model.put("endpointInterface", ei);

        processTemplate(eiTemplate, model);
        processTemplate(soapImplTemplate, model);
      }
    }

    for (WebFault webFault : allFaults) {
      model.put("fault", webFault);
      processTemplate(faultTemplate, model);
    }

    for (SchemaInfo schemaInfo : model.getNamespacesToSchemas().values()) {
      for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
        model.put("type", typeDefinition);
        URL template = typeDefinition.isEnum() ? jdk15EnumTypeTemplate : typeDefinition.isSimple() ? simpleTypeTemplate : complexTypeTemplate;
        processTemplate(template, model);
      }
    }
  }

  protected void addExplicitAnnotations(EndpointInterface ei, ClientClassnameForMethod conversion) {
    String clazz = conversion.convert(ei);

    SerializableWebServiceAnnotation wsAnnotation = new SerializableWebServiceAnnotation();
    wsAnnotation.setName(ei.getPortTypeName());
    wsAnnotation.setPortName(ei.getSimpleName() + "SOAPPort");
    wsAnnotation.setServiceName(ei.getServiceName());
    wsAnnotation.setTargetNamespace(ei.getTargetNamespace());
    this.generatedAnnotations.class2WebService.put(clazz, wsAnnotation);

    SerializableSOAPBindingAnnotation sbAnnotation = new SerializableSOAPBindingAnnotation();
    sbAnnotation.setStyle(ei.getSoapBindingStyle() == SOAPBinding.Style.DOCUMENT ? SOAPBindingAnnotation.STYLE_DOCUMENT : SOAPBindingAnnotation.STYLE_RPC);
    sbAnnotation.setParameterStyle(ei.getSoapParameterStyle() == SOAPBinding.ParameterStyle.BARE ? SOAPBindingAnnotation.PARAMETER_STYLE_BARE : SOAPBindingAnnotation.PARAMETER_STYLE_WRAPPED);
    sbAnnotation.setUse(ei.getSoapUse() == SOAPBinding.Use.ENCODED ? SOAPBindingAnnotation.USE_ENCODED : SOAPBindingAnnotation.USE_LITERAL);
    this.generatedAnnotations.class2SOAPBinding.put(clazz, sbAnnotation);

    HandlerChainAnnotation hcAnnotation = null; //todo: support this?
  }

  protected void addExplicitAnnotations(WebMethod webMethod, ClientClassnameForMethod conversion) {
    String classname = conversion.convert(webMethod.getDeclaringEndpointInterface());
    String methodName = webMethod.getSimpleName();
    String methodKey = String.format("%s.%s", classname, methodName);

    SerializableSOAPBindingAnnotation sbAnnotation = new SerializableSOAPBindingAnnotation();
    sbAnnotation.setStyle(webMethod.getSoapBindingStyle() == SOAPBinding.Style.DOCUMENT ? SOAPBindingAnnotation.STYLE_DOCUMENT : SOAPBindingAnnotation.STYLE_RPC);
    sbAnnotation.setParameterStyle(webMethod.getSoapParameterStyle() == SOAPBinding.ParameterStyle.BARE ? SOAPBindingAnnotation.PARAMETER_STYLE_BARE : SOAPBindingAnnotation.PARAMETER_STYLE_WRAPPED);
    sbAnnotation.setUse(webMethod.getSoapUse() == SOAPBinding.Use.ENCODED ? SOAPBindingAnnotation.USE_ENCODED : SOAPBindingAnnotation.USE_LITERAL);
    this.generatedAnnotations.method2SOAPBinding.put(methodKey, sbAnnotation);

    SerializableWebMethodAnnotation wmAnnotation = new SerializableWebMethodAnnotation();
    wmAnnotation.setOperationName(webMethod.getOperationName());
    wmAnnotation.setAction(webMethod.getAction());
    this.generatedAnnotations.method2WebMethod.put(methodKey, wmAnnotation);

    WebResult webResult = webMethod.getWebResult();
    SerializableWebResultAnnotation wrAnnotation = new SerializableWebResultAnnotation();
    wrAnnotation.setHeader(webResult.isHeader());
    wrAnnotation.setName(webResult.getName());
    wrAnnotation.setPartName(webResult.getPartName());
    wrAnnotation.setTargetNamespace(webResult.getTargetNamespace());

    this.generatedAnnotations.method2WebResult.put(methodKey, wrAnnotation);
    if (webMethod.isOneWay()) {
      this.generatedAnnotations.oneWayMethods.add(methodKey);
    }

    for (WebMessage webMessage : webMethod.getMessages()) {
      if (webMessage instanceof RequestWrapper) {
        RequestWrapper requestWrapper = (RequestWrapper) webMessage;
        String beanName = getBeanName(conversion, requestWrapper.getRequestBeanName());
        RequestWrapperAnnotation annotation = new RequestWrapperAnnotation(requestWrapper.getElementName(), requestWrapper.getElementNamespace(), beanName);
        this.generatedAnnotations.method2RequestWrapper.put(methodKey, annotation);
        Collection<ImplicitChildElement> childElements = requestWrapper.getChildElements();
        String[] propertyOrder = new String[childElements.size()];
        int i = 0;
        for (ImplicitChildElement childElement : childElements) {
          propertyOrder[i] = childElement.getElementName();
          i++;
        }
        this.generatedAnnotations.class2PropertyOrder.put(beanName, propertyOrder);
      }
      else if (webMessage instanceof ResponseWrapper) {
        ResponseWrapper responseWrapper = (ResponseWrapper) webMessage;
        String beanName = getBeanName(conversion, responseWrapper.getResponseBeanName());
        ResponseWrapperAnnotation annotation = new ResponseWrapperAnnotation(responseWrapper.getElementName(), responseWrapper.getElementNamespace(), beanName);
        this.generatedAnnotations.method2ResponseWrapper.put(methodKey, annotation);
        Collection<ImplicitChildElement> childElements = responseWrapper.getChildElements();
        String[] propertyOrder = new String[childElements.size()];
        int i = 0;
        for (ImplicitChildElement childElement : childElements) {
          propertyOrder[i] = childElement.getElementName();
          i++;
        }
        this.generatedAnnotations.class2PropertyOrder.put(beanName, propertyOrder);
      }
      else if (webMessage instanceof RPCInputMessage) {
        RPCInputMessage rpcInputMessage = ((RPCInputMessage) webMessage);
        String beanName = getBeanName(conversion, rpcInputMessage.getRequestBeanName());
        Collection<ImplicitChildElement> childElements = new RPCInputRequestBeanAdapter(rpcInputMessage).getChildElements();
        String[] propertyOrder = new String[childElements.size()];
        int i = 0;
        for (ImplicitChildElement childElement : childElements) {
          propertyOrder[i] = childElement.getElementName();
          i++;
        }
        this.generatedAnnotations.class2PropertyOrder.put(beanName, propertyOrder);
      }
      else if (webMessage instanceof RPCOutputMessage) {
        RPCOutputMessage outputMessage = ((RPCOutputMessage) webMessage);
        String beanName = getBeanName(conversion, outputMessage.getResponseBeanName());
        Collection<ImplicitChildElement> childElements = new RPCOutputResponseBeanAdapter(outputMessage).getChildElements();
        String[] propertyOrder = new String[childElements.size()];
        int i = 0;
        for (ImplicitChildElement childElement : childElements) {
          propertyOrder[i] = childElement.getElementName();
          i++;
        }
        this.generatedAnnotations.class2PropertyOrder.put(beanName, propertyOrder);
      }
    }

    int i = 0;
    for (WebParam webParam : webMethod.getWebParameters()) {
      SerializableWebParamAnnotation wpAnnotation = new SerializableWebParamAnnotation();
      wpAnnotation.setHeader(webParam.isHeader());
      wpAnnotation.setMode(webParam.getMode() == javax.jws.WebParam.Mode.INOUT ? WebParamAnnotation.MODE_INOUT : webParam.getMode() == javax.jws.WebParam.Mode.OUT ? WebParamAnnotation.MODE_OUT : WebParamAnnotation.MODE_IN);
      wpAnnotation.setName(webParam.getElementName());
      wpAnnotation.setPartName(webParam.getPartName());
      this.generatedAnnotations.method2WebParam.put(String.format("%s.%s", methodKey, i), wpAnnotation);
      i++;
    }
  }

  /**
   * Adds explicit elements for the specified root element.
   *
   * @param rootElement The root element.
   * @param conversion The conversion to use.
   */
  protected void addExplicitAnnotations(RootElementDeclaration rootElement, ClientClassnameForMethod conversion) {
    String classname = conversion.convert(rootElement);
    this.generatedAnnotations.class2XmlRootElement.put(classname, new XmlRootElementAnnotation(rootElement.getNamespace(), rootElement.getName()));
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
    File typesDir = getCommonJdkGenerateDir();
    Collection<String> typeFiles = enunciate.getJavaFiles(typesDir);

    //Compile the jdk14 files.
    Collection<String> jdk14Files = enunciate.getJavaFiles(getJdk14GenerateDir());
    jdk14Files.addAll(typeFiles);
    enunciate.invokeJavac(enunciate.getDefaultClasspath(), getJdk14CompileDir(), Arrays.asList("-source", "1.4", "-g"), jdk14Files.toArray(new String[jdk14Files.size()]));
    writeTypesFile(new File(getJdk14CompileDir(), uuid + ".types"));
    writeAnnotationsFile(new File(getJdk14CompileDir(), uuid + ".annotations"));

    //Compile the jdk15 files.
    Collection<String> jdk15Files = enunciate.getJavaFiles(getJdk15GenerateDir());
    jdk15Files.addAll(typeFiles);
    enunciate.invokeJavac(enunciate.getDefaultClasspath(), getJdk15CompileDir(), Arrays.asList("-g"), jdk15Files.toArray(new String[jdk15Files.size()]));
    writeTypesFile(new File(getJdk15CompileDir(), uuid + ".types"));
    writeAnnotationsFile(new File(getJdk15CompileDir(), uuid + ".annotations"));
  }

  /**
   * Write the serializeable annotations to the specified file.
   *
   * @param annotationsFile The to which to write the generated annotations.
   */
  protected void writeAnnotationsFile(File annotationsFile) throws EnunciateException, IOException {
    info("Writing annotations to %s.", annotationsFile);
    if (this.generatedAnnotations == null) {
      throw new EnunciateException("No annotations to write.");
    }

    FileOutputStream fos = new FileOutputStream(annotationsFile);
    try {
      this.generatedAnnotations.writeTo(fos);
    }
    catch (Exception e) {
      throw new EnunciateException(e);
    }
    finally {
      fos.close();
    }
  }

  /**
   * Write the generated types list to the specified file.
   *
   * @param typesFile The file to write the type list to.
   */
  protected void writeTypesFile(File typesFile) throws EnunciateException, FileNotFoundException {
    info("Writing client type list to %s.", typesFile);
    if (this.generatedTypeList == null) {
      throw new EnunciateException("No type list to write.");
    }

    PrintWriter writer = new PrintWriter(typesFile);
    for (String type : this.generatedTypeList) {
      writer.println(type);
    }
    writer.close();

    if (this.generatedAnnotations == null) {
      throw new EnunciateException("The client annotations weren't generated.");
    }
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

    File jdk14Sources = new File(getBuildDir(), jarName.replaceFirst("\\.jar", "-src.jar"));
    enunciate.zip(getJdk14GenerateDir(), jdk14Sources);
    enunciate.setProperty("client.jdk14.sources", jdk14Sources);

    //todo: generate the javadocs?

    ClientLibraryArtifact jdk14Artifact = new ClientLibraryArtifact(getName(), "client.jdk14.library", "Java 1.4+ Client Library");
    jdk14Artifact.setPlatform("Java (Version 1.4+)");
    //read in the description from file:
    jdk14Artifact.setDescription(readResource("library_description_14.html"));
    NamedFileArtifact jdk14BinariesJar = new NamedFileArtifact(getName(), "client.jdk14.library.binaries", jdk14Jar);
    jdk14BinariesJar.setDescription("The binaries for the JDK 1.4 client library.");
    jdk14Artifact.addArtifact(jdk14BinariesJar);
    NamedFileArtifact jdk14SourcesJar = new NamedFileArtifact(getName(), "client.jdk14.library.sources", jdk14Sources);
    jdk14SourcesJar.setDescription("The sources for the JDK 1.4 client library.");
    jdk14Artifact.addArtifact(jdk14SourcesJar);
    enunciate.addArtifact(jdk14Artifact);

    File jdk15Jar = new File(getBuildDir(), jarName);
    enunciate.zip(getJdk15CompileDir(), jdk15Jar);
    enunciate.setProperty("client.jdk15.jar", jdk15Jar);

    File jdk15Sources = new File(getBuildDir(), jarName.replaceFirst("\\.jar", "-src.jar"));
    enunciate.zip(getJdk15GenerateDir(), jdk15Sources);
    enunciate.setProperty("client.jdk15.sources", jdk15Sources);

    //todo: generate the javadocs?

    ClientLibraryArtifact jdk15Artifact = new ClientLibraryArtifact(getName(), "client.jdk15.library", "Java 5 Client Library");
    jdk15Artifact.setPlatform("Java (Version 5+)");
    //read in the description from file:
    jdk15Artifact.setDescription(readResource("library_description_15.html"));
    NamedFileArtifact jdk15BinariesJar = new NamedFileArtifact(getName(), "client.jdk15.library.binaries", jdk15Jar);
    jdk15BinariesJar.setDescription("The binaries for the JDK 1.5 client library.");
    jdk15Artifact.addArtifact(jdk15BinariesJar);
    NamedFileArtifact jdk15SourcesJar = new NamedFileArtifact(getName(), "client.jdk15.library.sources", jdk15Sources);
    jdk15SourcesJar.setDescription("The sources for the JDK 1.5 client library.");
    jdk15Artifact.addArtifact(jdk15SourcesJar);
    enunciate.addArtifact(jdk15Artifact);
  }

  /**
   * Reads a resource into string form.
   *
   * @param resource The resource to read.
   * @return The string form of the resource.
   */
  protected String readResource(String resource) throws IOException {
    InputStream resourceIn = XFireClientDeploymentModule.class.getResourceAsStream(resource);
    if (resourceIn != null) {
      BufferedReader in = new BufferedReader(new InputStreamReader(resourceIn));
      StringWriter writer = new StringWriter();
      PrintWriter out = new PrintWriter(writer);
      String line;
      while ((line = in.readLine()) != null) {
        out.println(line);
      }
      out.flush();
      out.close();
      writer.close();
      return writer.toString();
    }
    else {
      return null;
    }
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
   * The directory for the jdk14 client files.
   *
   * @return The directory for the jdk14 client files.
   */
  protected File getJdk14GenerateDir() {
    return new File(getGenerateDir(), "jdk14");
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
   * The directory for the jdk15 client files.
   *
   * @return The directory for the jdk15 client files.
   */
  protected File getJdk15GenerateDir() {
    return new File(getGenerateDir(), "jdk15");
  }

  /**
   * The directory for compiling the jdk 15 compatible classes.
   *
   * @return The directory for compiling the jdk 15 compatible classes.
   */
  protected File getJdk15CompileDir() {
    return new File(getCompileDir(), "jdk15");
  }

  /**
   * The directory for the java files common to both jdk 14 and jdk 15.
   *
   * @return The directory for the java files common to both jdk 14 and jdk 15.
   */
  protected File getCommonJdkGenerateDir() {
    return new File(getGenerateDir(), "common");
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

  /**
   * The annotations that were generated during the generate step.
   *
   * @return The annotations that were generated during the generate step.
   */
  public ExplicitWebAnnotations getGeneratedAnnotations() {
    return generatedAnnotations;
  }

  /**
   * The type list that was generated during the generate step.
   *
   * @return The type list that was generated during the generate step.
   */
  public List<String> getGeneratedTypeList() {
    return generatedTypeList;
  }
}
