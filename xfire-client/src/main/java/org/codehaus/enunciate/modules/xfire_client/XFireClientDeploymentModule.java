/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.modules.xfire_client;

import com.sun.mirror.declaration.ClassDeclaration;
import freemarker.template.*;
import net.sf.jelly.apt.decorations.JavaDoc;
import net.sf.jelly.apt.freemarker.FreemarkerJavaDoc;
import org.apache.commons.digester.RuleSet;
import org.codehaus.enunciate.EnunciateException;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.config.SchemaInfo;
import org.codehaus.enunciate.config.WsdlInfo;
import org.codehaus.enunciate.contract.jaxb.ImplicitChildElement;
import org.codehaus.enunciate.contract.jaxb.RootElementDeclaration;
import org.codehaus.enunciate.contract.jaxb.TypeDefinition;
import org.codehaus.enunciate.contract.jaxws.*;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.main.*;
import org.codehaus.enunciate.modules.FreemarkerDeploymentModule;
import org.codehaus.enunciate.modules.ProjectExtensionModule;
import org.codehaus.enunciate.modules.xfire_client.annotations.*;
import org.codehaus.enunciate.modules.xfire_client.config.ClientPackageConversion;
import org.codehaus.enunciate.modules.xfire_client.config.XFireClientRuleSet;
import org.codehaus.enunciate.template.freemarker.*;
import org.codehaus.xfire.annotations.HandlerChainAnnotation;
import org.codehaus.xfire.annotations.WebParamAnnotation;
import org.codehaus.xfire.annotations.soap.SOAPBindingAnnotation;

import javax.jws.soap.SOAPBinding;
import javax.xml.namespace.QName;
import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * <h1>XFire Client Module</h1>
 *
 * <p>The XFire client deployment module generates the client-side libraries that will access the
 * deployed web app using <a href="http://xfire.codehaus.org">XFire</a>.</p>
 *
 * <p>The order of the XFire client deployment module is 50, so as to allow the XFire module to apply
 * metadata to the endpoints before processing the client.</p>
 *
 * <ul>
 * <li><a href="#steps">steps</a></li>
 * <li><a href="#config">configuration</a></li>
 * <li><a href="#artifacts">artifacts</a></li>
 * </ul>
 *
 * <h1><a name="steps">Steps</a></h1>
 *
 * <h3>generate</h3>
 *
 * <p>The "generate" step is by far the most intensive and complex step in the execution of the XFire client
 * module.  The "generate" step generates all source code for accessing the deployed API.</p>
 *
 * <p>There XFire client deployment module currently generates code compatible with the JDK 1.4 and above
 * as well as code for Java 5 and above.  The only difference between the two libraries is that the Java 5
 * libraries take advantage of the newest Java 5 constructs, including typesafe enums and generics.  The
 * logic of serialization/deserialization is the same between the two libraries.</p>
 *
 * <p>For more information about the XFire client libraries, see the Javadoc API for the XFire client tools.</p>
 *
 * <h3>compile</h3>
 *
 * <p>During the "compile" step, the XFire client module compiles the code that was generated.</p>
 *
 * <h3>build</h3>
 *
 * <p>The "build" step assembles the classes that were assembled into a jar.  It also creates a source jar for
 * the libraries.</p>
 *
 * <h1><a name="config">Configuration</a></h1>
 *
 * <p>The XFire client module is configured by the "xfire-client" element under the "modules" element of the
 * enunciate configuration file.  It supports the following attributes:</p>
 *
 * <ul>
 * <li>The "label" attribute is used to determine the name of the client-side artifact files. The default is the Enunciate project label.</li>
 * <li>The "jarName" attribute specifies the name of the jar file(s) that are to be created.  If no jar name is specified,
 * the name will be calculated from the enunciate label, or a default will be supplied.</li>
 * <li>The "disable14Client" attributes disables the generation of the JDK 1.4 client.</li>
 * <li>The "disable15Client" attributes disables the generation of the JDK 5 client.</li>
 * </ul>
 *
 * <h3>The "package-conversions" element</h3>
 *
 * <p>The "package-conversions" subelement of the "xfire-client" element is used to map packages from
 * the original API packages to different package names.  This element supports an arbitrary number of
 * "convert" child elements that are used to specify the conversions.  These "convert" elements support
 * the following attributes:</p>
 *
 * <ul>
 * <li>The "from" attribute specifies the package that is to be converted.  This package will match
 * all classes in the package as well as any subpackages of the package.  This means that if "org.enunciate"
 * were specified, it would match "org.enunciate", "org.enunciate.api", and "org.enunciate.api.impl".</li>
 * <li>The "to" attribute specifies what the package is to be converted to.  Only the part of the package
 * that matches the "from" attribute will be converted.</li>
 * </ul>
 *
 * <h1><a name="artifacts">Artifacts</a></h1>
 *
 * <p>The XFire client deployment module exports the following artifacts:</p>
 *
 * <ul>
 * <li>The JDK 1.4 libraries and sources are exported under the id "client.jdk14.library".  (Note that this is a
 * bundle, so if exporting to a directory multiple files will be exported.  If exporting to a file, the bundle will
 * be zipped first.)</li>
 * <li>The JDK 1.5 libraries and sources are exported under the id "client.jdk15.library".  (Note that this is a
 * bundle, so if exporting to a directory multiple files will be exported.  If exporting to a file, the bundle will
 * be zipped first.)</li>
 * </ul>
 *
 * @author Ryan Heaton
 * @docFileName module_xfire_client.html
 */
public class XFireClientDeploymentModule extends FreemarkerDeploymentModule implements ProjectExtensionModule {

  private String jarName = null;
  private final Map<String, String> clientPackageConversions;
  private final XFireClientRuleSet configurationRules;
  private String uuid;
  private ExplicitWebAnnotations generatedAnnotations = null;
  private List<String> generatedTypeList = null;
  private boolean disable14Client = false;
  private boolean disable15Client = true; //we've got the JAX-WS client module now.  we'll disable this by default.
  private String label = null;

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

  /**
   * @return 50
   */
  @Override
  public int getOrder() {
    return 50;
  }

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException, EnunciateException {
    File commonJdkGenerateDir = getCommonJdkGenerateDir();
    File jdk14GenerateDir = getJdk14GenerateDir();
    File jdk15GenerateDir = getJdk15GenerateDir();

    boolean upToDate = isUpToDate(commonJdkGenerateDir, jdk14GenerateDir, jdk15GenerateDir);
    if (!upToDate) {
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
      CollectionTypeForMethod collectionTypeFor = new CollectionTypeForMethod(conversions);
      model.put("packageFor", new ClientPackageForMethod(conversions));
      model.put("classnameFor", classnameFor);
      model.put("simpleNameFor", new SimpleNameWithParamsMethod(classnameFor));
      model.put("componentTypeFor", componentTypeFor);
      model.put("collectionTypeFor", collectionTypeFor);

      String uuid = this.uuid;
      model.put("uuid", uuid);

      // First, generate everything that is common to both jdk 14 and jdk 15
      // This includes all request/response beans and all xfire types.
      // Also, we're going to gather the annotation information, the type list,
      // and the list of unique web faults.
      debug("Generating the XFire client classes that are common to both jdk 1.4 and jdk 1.5.");
      model.setFileOutputDirectory(commonJdkGenerateDir);
      generatedAnnotations = new ExplicitWebAnnotations();
      generatedTypeList = new ArrayList<String>();
      HashMap<String, WebFault> allFaults = new HashMap<String, WebFault>();

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
                WebFault fault = (WebFault) webMessage;
                allFaults.put(fault.getQualifiedName(), fault);
              }
            }

            addExplicitAnnotations(webMethod, classnameFor);
          }
        }
      }

      //gather the annotation information and process the possible beans for each web fault.
      for (WebFault webFault : allFaults.values()) {
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

      if (!isDisable14Client()) {
        //Now, generate the jdk14-compatable client-side stubs.
        debug("Generating the XFire client classes for jdk 1.4.");
        model.setFileOutputDirectory(jdk14GenerateDir);
        for (WsdlInfo wsdlInfo : model.getNamespacesToWSDLs().values()) {
          for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
            model.put("endpointInterface", ei);

            processTemplate(eiTemplate, model);
            processTemplate(soapImplTemplate, model);
          }
        }

        for (WebFault webFault : allFaults.values()) {
          ClassDeclaration superFault = webFault.getSuperclass().getDeclaration();
          if (superFault != null && allFaults.containsKey(superFault.getQualifiedName()) && allFaults.get(superFault.getQualifiedName()).isImplicitSchemaElement()) {
            model.put("superFault", allFaults.get(superFault.getQualifiedName()));
          }
          else {
            model.remove("superFault");
          }

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
      }
      else {
        debug("Java 1.4 client generation has been disabled.  Skipping generation of 1.4 client classes.");
      }

      if (!isDisable15Client()) {
        //Now enable jdk-15 compatability and generate those client-side stubs.
        debug("Generating the XFire client classes for jdk 1.5.");
        model.setFileOutputDirectory(jdk15GenerateDir);
        classnameFor.setJdk15(true);
        componentTypeFor.setJdk15(true);
        collectionTypeFor.setJdk15(true);
        for (WsdlInfo wsdlInfo : model.getNamespacesToWSDLs().values()) {
          for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
            model.put("endpointInterface", ei);

            processTemplate(eiTemplate, model);
            processTemplate(soapImplTemplate, model);
          }
        }

        for (WebFault webFault : allFaults.values()) {
          ClassDeclaration superFault = webFault.getSuperclass().getDeclaration();
          if (superFault != null && allFaults.containsKey(superFault.getQualifiedName()) && allFaults.get(superFault.getQualifiedName()).isImplicitSchemaElement()) {
            model.put("superFault", allFaults.get(superFault.getQualifiedName()));
          }
          else {
            model.remove("superFault");
          }

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
      else {
        debug("Java 5 client generation has been disabled.  Skipping generation of Java 5 client classes.");
      }

      writeTypesFile(new File(getCommonJdkGenerateDir(), uuid + ".types"));
      writeAnnotationsFile(new File(getCommonJdkGenerateDir(), uuid + ".annotations"));
    }
    else {
      info("Skipping generation of XFire Client sources as everything appears up-to-date...");
    }
  }

  /**
   * Whether the specified directories are up to date.
   *
   * @param commonJdkGenerateDir The common jdk generate directory.
   * @param jdk14GenerateDir     The jdk14 generate directory.
   * @param jdk15GenerateDir     The jdk15 generate directory.
   * @return Whether the directories are up-to-date.
   */
  protected boolean isUpToDate(File commonJdkGenerateDir, File jdk14GenerateDir, File jdk15GenerateDir) {
    return enunciate.isUpToDateWithSources(commonJdkGenerateDir) &&
      (isDisable14Client() || enunciate.isUpToDateWithSources(jdk14GenerateDir)) &&
      (isDisable15Client() || enunciate.isUpToDateWithSources(jdk15GenerateDir));
  }

  protected void addExplicitAnnotations(EndpointInterface ei, ClientClassnameForMethod conversion) throws TemplateModelException {
    String clazz = conversion.convert(ei);

    SerializableWebServiceAnnotation wsAnnotation = new SerializableWebServiceAnnotation();
    wsAnnotation.setName(ei.getPortTypeName());
    //according to JSR 181, the port name can't go on the Endpoint Interface...
    //wsAnnotation.setPortName(ei.getSimpleName() + "SOAPPort");
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

  protected void addExplicitAnnotations(WebMethod webMethod, ClientClassnameForMethod conversion) throws TemplateModelException {
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
      wpAnnotation.setTargetNamespace(webParam.getTargetNamespace());
      wpAnnotation.setPartName(webParam.getPartName());
      this.generatedAnnotations.method2WebParam.put(String.format("%s.%s", methodKey, i), wpAnnotation);
      i++;
    }
  }

  /**
   * Adds explicit elements for the specified root element.
   *
   * @param rootElement The root element.
   * @param conversion  The conversion to use.
   */
  protected void addExplicitAnnotations(RootElementDeclaration rootElement, ClientClassnameForMethod conversion) throws TemplateModelException {
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
    if (!isDisable14Client()) {
      File jdk14CompileDir = getJdk14CompileDir();
      if (!enunciate.isUpToDateWithSources(jdk14CompileDir)) {
        Collection<String> jdk14Files = enunciate.getJavaFiles(getJdk14GenerateDir());
        jdk14Files.addAll(typeFiles);
        String clientClasspath = enunciate.getEnunciateBuildClasspath(); //we use the build classpath for client-side jars so you don't have to include client-side dependencies on the server-side.
        enunciate.invokeJavac(clientClasspath, "1.4", jdk14CompileDir, new ArrayList<String>(), jdk14Files.toArray(new String[jdk14Files.size()]));
        enunciate.copyFile(new File(getCommonJdkGenerateDir(), uuid + ".types"), new File(jdk14CompileDir, uuid + ".types"));
        enunciate.copyFile(new File(getCommonJdkGenerateDir(), uuid + ".annotations"), new File(jdk14CompileDir, uuid + ".annotations"));
      }
      else {
        info("Skipping compilation of JDK 1.4 client classes as everything appears up-to-date...");
      }
    }
    else {
      debug("1.4 client code generation has been disabled.  Skipping compilation of 1.4 sources.");
    }

    if (!isDisable15Client()) {
      //Compile the jdk15 files.
      File jdk15CompileDir = getJdk15CompileDir();
      if (!enunciate.isUpToDateWithSources(jdk15CompileDir)) {
        Collection<String> jdk15Files = enunciate.getJavaFiles(getJdk15GenerateDir());
        jdk15Files.addAll(typeFiles);
        String clientClasspath = enunciate.getEnunciateBuildClasspath(); //we use the build classpath for client-side jars so you don't have to include client-side dependencies on the server-side.
        enunciate.invokeJavac(clientClasspath, "1.5", jdk15CompileDir, new ArrayList<String>(), jdk15Files.toArray(new String[jdk15Files.size()]));
        enunciate.copyFile(new File(getCommonJdkGenerateDir(), uuid + ".types"), new File(jdk15CompileDir, uuid + ".types"));
        enunciate.copyFile(new File(getCommonJdkGenerateDir(), uuid + ".annotations"), new File(jdk15CompileDir, uuid + ".annotations"));
      }
      else {
        info("Skipping compilation of JDK 1.5 client classes as everything appears up-to-date...");
      }
    }
    else {
      debug("Java 5 client code generation has been disabled.  Skipping compilation of Java 5 sources.");
    }
  }

  /**
   * Write the serializeable annotations to the specified file.
   *
   * @param annotationsFile The to which to write the generated annotations.
   */
  protected void writeAnnotationsFile(File annotationsFile) throws EnunciateException, IOException {
    debug("Writing annotations to %s.", annotationsFile);
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
    debug("Writing client type list to %s.", typesFile);
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
      if (getLabel() != null) {
          label = getLabel();
      }
      else if ((enunciate.getConfig() != null) && (enunciate.getConfig().getLabel() != null)) {
        label = enunciate.getConfig().getLabel();
      }

      jarName = label + "-client.jar";
    }

    List<ArtifactDependency> clientDeps = new ArrayList<ArtifactDependency>();
    MavenDependency xfireClientDependency = new MavenDependency();
    xfireClientDependency.setId("enunciate-xfire-client-tools");
    xfireClientDependency.setArtifactType("jar");
    xfireClientDependency.setDescription("Support classes for invoking the client.");
    xfireClientDependency.setGroupId("org.codehaus.enunciate");
    xfireClientDependency.setURL("http://enunciate.codehaus.org/");
    xfireClientDependency.setVersion(enunciate.getVersion());
    clientDeps.add(xfireClientDependency);

    BaseArtifactDependency dep = new BaseArtifactDependency();
    dep.setId("xfire");
    dep.setArtifactType("jar");
    dep.setDescription("The XFire engine.");
    dep.setURL("http://xfire.codehaus.org/");
    dep.setVersion("1.2.2");
    clientDeps.add(dep);

    dep = new BaseArtifactDependency();
    dep.setId("stax-api");
    dep.setArtifactType("jar");
    dep.setVersion("1.0.1");
    dep.setDescription("The stax APIs.");
    clientDeps.add(dep);

    dep = new BaseArtifactDependency();
    dep.setId("jaxws-api");
    dep.setArtifactType("jar");
    dep.setVersion("2.0");
    dep.setDescription("The JAX-WS API.");
    clientDeps.add(dep);

    dep = new BaseArtifactDependency();
    dep.setId("wsdl4j");
    dep.setArtifactType("jar");
    dep.setVersion("1.5.2");
    clientDeps.add(dep);

    dep = new BaseArtifactDependency();
    dep.setId("woodstox");
    dep.setArtifactType("jar");
    dep.setVersion("2.9.3");
    dep.setDescription("Woodstox stax implementation");
    clientDeps.add(dep);

    dep = new BaseArtifactDependency();
    dep.setId("commons-codec");
    dep.setArtifactType("jar");
    dep.setVersion("1.3");
    clientDeps.add(dep);

    dep = new BaseArtifactDependency();
    dep.setId("commons-logging");
    dep.setArtifactType("jar");
    dep.setVersion("1.1");
    clientDeps.add(dep);

    dep = new BaseArtifactDependency();
    dep.setId("commons-httpclient");
    dep.setArtifactType("jar");
    dep.setVersion("3.0");
    clientDeps.add(dep);

    dep = new BaseArtifactDependency();
    dep.setId("jdom");
    dep.setArtifactType("jar");
    dep.setVersion("1.0");
    clientDeps.add(dep);

    dep = new BaseArtifactDependency();
    dep.setId("mail");
    dep.setArtifactType("jar");
    dep.setVersion("1.4");
    clientDeps.add(dep);

    dep = new BaseArtifactDependency();
    dep.setId("activation");
    dep.setArtifactType("jar");
    dep.setVersion("1.1");
    clientDeps.add(dep);

    if (!isDisable14Client()) {
      File jdk14Jar = new File(getBuildDir(), jarName.replaceFirst("\\.jar", "-1.4.jar"));
      if (!enunciate.isUpToDate(getJdk14CompileDir(), jdk14Jar)) {
        enunciate.zip(jdk14Jar, getJdk14CompileDir());
        enunciate.setProperty("client.jdk14.jar", jdk14Jar);
      }
      else {
        info("Skipping creation of JDK 1.4 client jar as everything appears up-to-date...");
      }

      File jdk14Sources = new File(getBuildDir(), jarName.replaceFirst("\\.jar", "-1.4-sources.jar"));
      if (!enunciate.isUpToDate(getJdk14GenerateDir(), jdk14Sources)) {
        enunciate.zip(jdk14Sources, getJdk14GenerateDir());
        enunciate.setProperty("client.jdk14.sources", jdk14Sources);
      }
      else {
        info("Skipping creation of the JDK 1.4 client source jar as everything appears up-to-date...");
      }

      //todo: generate the javadocs?

      ClientLibraryArtifact jdk14ArtifactBundle = new ClientLibraryArtifact(getName(), "client.jdk14.library", "XFire Client Library (Java 1.4 Compatible)");
      jdk14ArtifactBundle.setPlatform("Java (Version 1.4+)");
      //read in the description from file:
      jdk14ArtifactBundle.setDescription(readResource("library_description_14.fmt"));
      NamedFileArtifact jdk14BinariesJar = new NamedFileArtifact(getName(), "client.jdk14.library.binaries", jdk14Jar);
      jdk14BinariesJar.setDescription("The binaries for the JDK 1.4 client library.");
      jdk14BinariesJar.setPublic(false);
      jdk14BinariesJar.setArtifactType(ArtifactType.binaries);
      jdk14ArtifactBundle.addArtifact(jdk14BinariesJar);
      NamedFileArtifact jdk14SourcesJar = new NamedFileArtifact(getName(), "client.jdk14.library.sources", jdk14Sources);
      jdk14SourcesJar.setDescription("The sources for the JDK 1.4 client library.");
      jdk14SourcesJar.setPublic(false);
      jdk14SourcesJar.setArtifactType(ArtifactType.sources);
      jdk14ArtifactBundle.addArtifact(jdk14SourcesJar);
      jdk14ArtifactBundle.setDependencies(clientDeps);
      enunciate.addArtifact(jdk14BinariesJar);
      enunciate.addArtifact(jdk14SourcesJar);
      enunciate.addArtifact(jdk14ArtifactBundle);
    }
    else {
      debug("No artifact generated for the Java 1.4 client because it was disabled.");
    }

    if (!isDisable15Client()) {
      File jdk15Jar = new File(getBuildDir(), jarName.replaceFirst("\\.jar", "-1.5.jar"));
      if (!enunciate.isUpToDate(getJdk15CompileDir(), jdk15Jar)) {
        enunciate.zip(jdk15Jar, getJdk15CompileDir());
        enunciate.setProperty("client.jdk15.jar", jdk15Jar);
      }
      else {
        info("Skipping creation of JDK 1.5 client jar as everything appears up-to-date...");
      }

      File jdk15Sources = new File(getBuildDir(), jarName.replaceFirst("\\.jar", "-1.5-sources.jar"));
      if (!enunciate.isUpToDate(getJdk15GenerateDir(), jdk15Sources)) {
        enunciate.zip(jdk15Sources, getJdk15GenerateDir());
        enunciate.setProperty("client.jdk15.sources", jdk15Sources);
      }
      else {
        info("Skipping creation of the JDK 1.5 client source jar as everything appears up-to-date...");
      }

      //todo: generate the javadocs?

      ClientLibraryArtifact jdk15ArtifactBundle = new ClientLibraryArtifact(getName(), "client.jdk15.library", "XFire Client Library (Java 5+)");
      jdk15ArtifactBundle.setPlatform("Java (Version 5+)");
      //read in the description from file:
      jdk15ArtifactBundle.setDescription(readResource("library_description_15.fmt"));
      NamedFileArtifact jdk15BinariesJar = new NamedFileArtifact(getName(), "client.jdk15.library.binaries", jdk15Jar);
      jdk15BinariesJar.setDescription("The binaries for the JDK 1.5 client library.");
      jdk15BinariesJar.setPublic(false);
      jdk15BinariesJar.setArtifactType(ArtifactType.binaries);
      jdk15ArtifactBundle.addArtifact(jdk15BinariesJar);
      NamedFileArtifact jdk15SourcesJar = new NamedFileArtifact(getName(), "client.jdk15.library.sources", jdk15Sources);
      jdk15SourcesJar.setDescription("The sources for the JDK 1.5 client library.");
      jdk15SourcesJar.setPublic(false);
      jdk15SourcesJar.setArtifactType(ArtifactType.sources);
      jdk15ArtifactBundle.addArtifact(jdk15SourcesJar);
      jdk15ArtifactBundle.setDependencies(clientDeps);
      enunciate.addArtifact(jdk15BinariesJar);
      enunciate.addArtifact(jdk15SourcesJar);
      enunciate.addArtifact(jdk15ArtifactBundle);
    }
    else {
      debug("No artifact generated for the Java 5 client because it was disabled.");
    }
  }

  /**
   * Reads a resource into string form.
   *
   * @param resource The resource to read.
   * @return The string form of the resource.
   */
  protected String readResource(String resource) throws IOException, EnunciateException {
    HashMap<String, Object> model = new HashMap<String, Object>();
    model.put("sample_service_method", getModelInternal().findExampleWebMethod());
    model.put("sample_resource", getModelInternal().findExampleResource());

    URL res = XFireClientDeploymentModule.class.getResource(resource);
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bytes);
    try {
      processTemplate(res, model, out);
      out.flush();
      bytes.flush();
      return bytes.toString("utf-8");
    }
    catch (TemplateException e) {
      throw new EnunciateException(e);
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
  public File getJdk14GenerateDir() {
    return new File(getGenerateDir(), "jdk14");
  }

  /**
   * The directory for compiling the jdk 14 compatible classes.
   *
   * @return The directory for compiling the jdk 14 compatible classes.
   */
  public File getJdk14CompileDir() {
    return new File(getCompileDir(), "jdk14");
  }

  /**
   * The directory for the jdk15 client files.
   *
   * @return The directory for the jdk15 client files.
   */
  public File getJdk15GenerateDir() {
    return new File(getGenerateDir(), "jdk15");
  }

  /**
   * The directory for compiling the jdk 15 compatible classes.
   *
   * @return The directory for compiling the jdk 15 compatible classes.
   */
  public File getJdk15CompileDir() {
    return new File(getCompileDir(), "jdk15");
  }

  /**
   * The directory for the java files common to both jdk 14 and jdk 15.
   *
   * @return The directory for the java files common to both jdk 14 and jdk 15.
   */
  public File getCommonJdkGenerateDir() {
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
   * Whether to disable the Java 1.4 client.
   *
   * @return Whether to disable the Java 1.4 client.
   */
  public boolean isDisable14Client() {
    return disable14Client;
  }

  /**
   * Whether to disable the Java 1.4 client.
   *
   * @param disable14Client Whether to disable the Java 1.4 client.
   */
  public void setDisable14Client(boolean disable14Client) {
    this.disable14Client = disable14Client;
  }

  /**
   * Whether to disable the Java 5 client.
   *
   * @return Whether to disable the Java 5 client.
   */
  public boolean isDisable15Client() {
    return disable15Client;
  }

  /**
   * Whether to disable the Java 5 client.
   *
   * @param disable15Client Whether to disable the Java 5 client.
   */
  public void setDisable15Client(boolean disable15Client) {
    this.disable15Client = disable15Client;
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
   * The label for the xfire-client API.
   *
   * @return The label for the xfire-client API.
   */
  public String getLabel() {
    return label;
  }

  /**
   * The label for the xfire-client API.
   *
   * @param label The label for the xfire-client API.
   */
  public void setLabel(String label) {
    this.label = label;
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

  // Inherited.
  @Override
  public boolean isDisabled() {
    if (super.isDisabled()) {
      return true;
    }
    else if (getModelInternal() != null && getModelInternal().getNamespacesToWSDLs().isEmpty()) {
      debug("XFire client module is disabled because there are no endpoint interfaces.");
      return true;
    }
    else if (isDisable14Client() && isDisable15Client()) {
      debug("XFire client module is disabled because both Java 5 and Java 1.4 is disabled.");
      return true;
    }

    return false;
  }

  public List<File> getProjectSources() {
    return Collections.emptyList();
  }

  public List<File> getProjectTestSources() {
    return isDisable15Client() ? (isDisable14Client() ? Collections.<File>emptyList() : Arrays.asList(getCommonJdkGenerateDir(), getJdk14GenerateDir())) : Arrays.asList(getCommonJdkGenerateDir(), getJdk15GenerateDir());
  }

  public List<File> getProjectResourceDirectories() {
    return Collections.emptyList();
  }

  public List<File> getProjectTestResourceDirectories() {
    return (isDisable15Client() && isDisable14Client()) ? Collections.<File>emptyList() : Arrays.asList(getCommonJdkGenerateDir());
  }
}
