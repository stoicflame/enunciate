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

package org.codehaus.enunciate.modules.java_client;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import freemarker.template.*;
import net.sf.jelly.apt.decorations.JavaDoc;
import net.sf.jelly.apt.freemarker.FreemarkerJavaDoc;
import org.apache.commons.digester.RuleSet;
import org.codehaus.enunciate.EnunciateException;
import org.codehaus.enunciate.apt.EnunciateClasspathListener;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.config.SchemaInfo;
import org.codehaus.enunciate.config.WsdlInfo;
import org.codehaus.enunciate.contract.jaxb.TypeDefinition;
import org.codehaus.enunciate.contract.jaxb.Registry;
import org.codehaus.enunciate.contract.jaxrs.ResourceMethod;
import org.codehaus.enunciate.contract.jaxrs.RootResource;
import org.codehaus.enunciate.contract.jaxws.*;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.main.ClientLibraryArtifact;
import org.codehaus.enunciate.main.Enunciate;
import org.codehaus.enunciate.main.NamedFileArtifact;
import org.codehaus.enunciate.main.ArtifactType;
import org.codehaus.enunciate.modules.FreemarkerDeploymentModule;
import org.codehaus.enunciate.modules.DeploymentModule;
import org.codehaus.enunciate.modules.ProjectExtensionModule;
import org.codehaus.enunciate.modules.java_client.config.ClientPackageConversion;
import org.codehaus.enunciate.modules.java_client.config.JavaClientRuleSet;
import org.codehaus.enunciate.modules.xml.XMLDeploymentModule;
import org.codehaus.enunciate.template.freemarker.*;
import org.codehaus.enunciate.util.AntPatternMatcher;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * <h1>Java Client Module</h1>
 *
 * <p>The Java client module generates the client-side libraries that will access the Web service API. For SOAP endpoints,
 * a client-side service interface will be generated that uses <a href="https://jax-ws.dev.java.net/">JAX-WS</a>. For REST endpoints,
 * the JAXB data model classes will be generated to access the XML endpoints. If there are any JSON endpoints, a set of data model
 * classes will be generated that can be used in conjunction with the <a href="http://jackson.codehaus.org/">Jackson</a> library to
 * access them.</p>
 *
 * <p>The order of the Java client module is 50, so as to allow the Java module to apply
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
 * <p>The "generate" step is by far the most intensive and complex step in the execution of the Java client
 * module.  The "generate" step generates all source code for accessing the web service API.</p>
 *
 * <h3>compile</h3>
 *
 * <p>During the "compile" step, the Java client module compiles the code that was generated.</p>
 *
 * <h3>build</h3>
 *
 * <p>The "build" step assembles the classes that were assembled into a jar. If there are any JSON endpoints, the JSON client library
 * will be assembled into a separate jar. It also creates a source jar for each library.</p>
 *
 * <h1><a name="config">Configuration</a></h1>
 *
 * <p>The Java client module is configured by the "java-client" element under the "modules" element of the
 * enunciate configuration file.  It supports the following attributes:</p>
 *
 * <ul>
 * <li>The "label" attribute is used to determine the name of the client-side artifact files. The default is the Enunciate project label.</li>
 * <li>The "jarName" attribute specifies the name of the jar file(s) that are to be created.  If no jar name is specified,
 * the name will be calculated from the enunciate label, or a default will be supplied.</li>
 * <li>The "jsonJarName" attribute specifies the name of the jar file(s) that are to be created for the JSON client.  If no jar name is specified,
 * the name will be calculated from the enunciate label, or a default will be supplied.</li>
 * </ul>
 *
 * <h3>The "package-conversions" element</h3>
 *
 * <p>The "package-conversions" subelement of the "java-client" element is used to map packages from
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
 * <h3>The "json-package-conversions" element</h3>
 *
 * <p>The "json-package-conversions" element has the same purpose and syntax as the "package-conversions" element above, but is instead applied
 * to the JSON java client. By default, ths JSON conversions will be the same as the "package-conversions" with the "json" subpackage appended.</p>
 *
 * <h3>The "server-side-type" element</h3>
 *
 * <p>An arbitrary number of "server-side-type" elements are allowed as child elements of the "java-client" element.  The "server-side-type" element
 * can be used to specify a server-side type that is to be ported directly over to the client-side library (as opposed to <i>generating</i> the client-side type
 * from the server-side type). This can be useful to provide more useful client-side capabilities, but requires that there be no package conversions for types
 * and web faults.</p>
 *
 * <p>The "server-side-type" element supports one attribute, "pattern" that defines an ant-style pattern of the type(s) that are to be included (using a '.'
 * for separating the package name).</p>
 *
 * <h1><a name="artifacts">Artifacts</a></h1>
 *
 * <p>The Java client module exports the following artifacts:</p>
 *
 * <ul>
 * <li>The libraries and sources are exported under the id "java.client.library".  (Note that this is a
 * bundle, so if exporting to a directory multiple files will be exported.  If exporting to a file, the bundle will
 * be zipped first.)</li>
 * <li>The libraries and sources for the json client library are exported under the id "java.json.client.library".  (Note that this is a
 * bundle, so if exporting to a directory multiple files will be exported.  If exporting to a file, the bundle will
 * be zipped first.)</li>
 * </ul>
 *
 * @author Ryan Heaton
 * @docFileName module_java_client.html
 */
public class JavaClientDeploymentModule extends FreemarkerDeploymentModule implements ProjectExtensionModule, EnunciateClasspathListener {

  private String jarName = null;
  private String jsonJarName = null;
  private final Map<String, String> clientPackageConversions;
  private final Map<String, String> jsonClientPackageConversions;
  private final JavaClientRuleSet configurationRules;
  private final Set<String> serverSideTypesToUse;
  private String label = null;

  private boolean forceGenerateJsonJar = false;
  private boolean disableJsonJar = false;
  private boolean jacksonXcAvailable = false;

  public JavaClientDeploymentModule() {
    this.clientPackageConversions = new LinkedHashMap<String, String>();
    this.jsonClientPackageConversions = new LinkedHashMap<String, String>();
    this.configurationRules = new JavaClientRuleSet();
    this.serverSideTypesToUse = new TreeSet<String>();
    getAliases().add("jaxws-client");
  }

  /**
   * @return "java-client"
   */
  @Override
  public String getName() {
    return "java-client";
  }

  /**
   * @return 50
   */
  @Override
  public int getOrder() {
    return 50;
  }

  @Override
  public void init(Enunciate enunciate) throws EnunciateException {
    super.init(enunciate);

    if (!isDisabled() && !enunciate.isModuleEnabled("xml")) {
      throw new EnunciateException("The Java client module requires you to enable the XML module.");
    }
  }

  public void onClassesFound(Set<String> classes) {
    jacksonXcAvailable |= classes.contains("org.codehaus.jackson.xc.JaxbAnnotationIntrospector");
  }

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException, EnunciateException {
    File generateDir = getGenerateDir();

    boolean upToDate = getEnunciate().isUpToDateWithSources(generateDir);
    if (!upToDate) {
      //load the references to the templates....
      URL eiTemplate = getTemplateURL("client-endpoint-interface.fmt");
      URL soapImplTemplate = getTemplateURL("client-soap-endpoint-impl.fmt");
      URL faultTemplate = getTemplateURL("client-web-fault.fmt");
      URL faultBeanTemplate = getTemplateURL("client-fault-bean.fmt");
      URL requestBeanTemplate = getTemplateURL("client-request-bean.fmt");
      URL responseBeanTemplate = getTemplateURL("client-response-bean.fmt");
      URL simpleTypeTemplate = getTemplateURL("client-simple-type.fmt");
      URL complexTypeTemplate = getTemplateURL("client-complex-type.fmt");
      URL enumTypeTemplate = getTemplateURL("client-enum-type.fmt");
      URL registryTemplate = getTemplateURL("client-registry.fmt");

      URL jsonComplexTypeTemplate = getTemplateURL("json-complex-type.fmt");
      URL jsonSimpleTypeTemplate = getTemplateURL("json-simple-type.fmt");
      URL jsonEnumTypeTemplate = getTemplateURL("json-enum-type.fmt");

      EnunciateFreemarkerModel model = getModel();
      Map<String, String> conversions = getClientPackageConversions();
      ClientClassnameForMethod classnameFor = new ClientClassnameForMethod(conversions);
      classnameFor.setJdk15(true);
      model.put("packageFor", new ClientPackageForMethod(conversions));
      model.put("classnameFor", classnameFor);
      model.put("simpleNameFor", new SimpleNameWithParamsMethod(classnameFor));

      debug("Generating the Java client classes...");
      model.setFileOutputDirectory(getClientGenerateDir());
      HashMap<String, WebFault> allFaults = new HashMap<String, WebFault>();

      Set<String> seeAlsos = new TreeSet<String>();
      // Process the annotations, the request/response beans, and gather the set of web faults
      // for each endpoint interface.
      for (WsdlInfo wsdlInfo : model.getNamespacesToWSDLs().values()) {
        for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
          for (WebMethod webMethod : ei.getWebMethods()) {
            for (WebMessage webMessage : webMethod.getMessages()) {
              if (webMessage instanceof RequestWrapper) {
                model.put("message", webMessage);
                processTemplate(requestBeanTemplate, model);
                seeAlsos.add(getBeanName(classnameFor, ((RequestWrapper) webMessage).getRequestBeanName()));
              }
              else if (webMessage instanceof ResponseWrapper) {
                model.put("message", webMessage);
                processTemplate(responseBeanTemplate, model);
                seeAlsos.add(getBeanName(classnameFor, ((ResponseWrapper) webMessage).getResponseBeanName()));
              }
              else if (webMessage instanceof WebFault) {
                WebFault fault = (WebFault) webMessage;
                allFaults.put(fault.getQualifiedName(), fault);
              }
            }
          }
        }
      }

      //gather the annotation information and process the possible beans for each web fault.
      for (WebFault webFault : allFaults.values()) {
        boolean implicit = webFault.isImplicitSchemaElement();
        String faultBean = implicit ? getBeanName(classnameFor, webFault.getImplicitFaultBeanQualifiedName()) : classnameFor.convert(webFault.getExplicitFaultBeanType());
        seeAlsos.add(faultBean);

        if (implicit) {
          model.put("fault", webFault);
          processTemplate(faultBeanTemplate, model);
        }
      }

      model.put("seeAlsoBeans", seeAlsos);
      for (WsdlInfo wsdlInfo : model.getNamespacesToWSDLs().values()) {
        if (wsdlInfo.getProperty("filename") == null) {
          throw new EnunciateException("WSDL " + wsdlInfo.getId() + " doesn't have a filename.");
        }
        model.put("wsdlFileName", wsdlInfo.getProperty("filename"));

        for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
          model.put("endpointInterface", ei);

          processTemplate(eiTemplate, model);
          processTemplate(soapImplTemplate, model);
        }
      }

      AntPatternMatcher matcher = new AntPatternMatcher();
      matcher.setPathSeparator(".");
      for (WebFault webFault : allFaults.values()) {
        if (useServerSide(webFault, matcher)) {
          File sourceFile = webFault.getPosition().file();
          getEnunciate().copyFile(sourceFile, getServerSideDestFile(sourceFile, webFault));
        }
        else {
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
      }

      final Set<String> uniquePackages = new TreeSet<String>();
      for (SchemaInfo schemaInfo : model.getNamespacesToSchemas().values()) {
        for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
          if (useServerSide(typeDefinition, matcher)) {
            File sourceFile = typeDefinition.getPosition().file();
            getEnunciate().copyFile(sourceFile, getServerSideDestFile(sourceFile, typeDefinition));
          }
          else {
            model.put("rootEl", model.findRootElementDeclaration(typeDefinition));
            model.put("type", typeDefinition);
            URL template = typeDefinition.isEnum() ? enumTypeTemplate : typeDefinition.isSimple() ? simpleTypeTemplate : complexTypeTemplate;
            processTemplate(template, model);
          }

          if (typeDefinition.getPackage() != null) {
            uniquePackages.add(typeDefinition.getPackage().getQualifiedName());
          }
        }
        for (Registry registry : schemaInfo.getRegistries()) {
          model.put("registry", registry);
          processTemplate(registryTemplate, model);
        }
      }

      boolean generateJsonJar = isGenerateJsonJar();
      model.put("generateJson", generateJsonJar);
      if (generateJsonJar) {
        //first set up the json client package conversions.
        Map<String, String> jsonConversions = getJsonPackageConversions(uniquePackages);
        model.setFileOutputDirectory(getJsonClientGenerateDir());
        ClientClassnameForMethod jsonClassnameFor = new ClientClassnameForMethod(jsonConversions);
        jsonClassnameFor.setJdk15(true);
        model.put("packageFor", new ClientPackageForMethod(jsonConversions));
        model.put("classnameFor", jsonClassnameFor);
        model.put("simpleNameFor", new SimpleNameWithParamsMethod(jsonClassnameFor));

        debug("Generating the Java JSON client classes...");
        for (SchemaInfo schemaInfo : model.getNamespacesToSchemas().values()) {
          for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
            model.put("type", typeDefinition);
            URL template = typeDefinition.isEnum() ? jsonEnumTypeTemplate : typeDefinition.isSimple() ? jsonSimpleTypeTemplate : jsonComplexTypeTemplate;
            processTemplate(template, model);
          }
        }
      }
    }
    else {
      info("Skipping generation of Java client sources as everything appears up-to-date...");
    }
  }

  /**
   * Get the list of json package conversions given the specified list of unique packages.
   *
   * @param uniquePackages The unique packages.
   * @return The package conversions.
   */
  protected Map<String, String> getJsonPackageConversions(Set<String> uniquePackages) {
    HashMap<String, String> conversions = new HashMap<String, String>();
    for (String serverSidePackage : uniquePackages) {
      boolean conversionFound = false;
      if (getJsonClientPackageConversions().containsKey(serverSidePackage)) {
        conversions.put(serverSidePackage, getJsonClientPackageConversions().get(serverSidePackage));
        conversionFound = true;
      }
      else {
        for (String pkg : getJsonClientPackageConversions().keySet()) {
          if (serverSidePackage.startsWith(pkg)) {
            String conversion = getJsonClientPackageConversions().get(pkg);
            conversions.put(serverSidePackage, conversion + serverSidePackage.substring(pkg.length()));
            conversionFound = true;
            break;
          }
        }
      }

      if (!conversionFound) {
        if (getClientPackageConversions().containsKey(serverSidePackage)) {
          conversions.put(serverSidePackage, getClientPackageConversions().get(serverSidePackage) + ".json");
          conversionFound = true;
        }
        else {
          for (String pkg : getClientPackageConversions().keySet()) {
            if (serverSidePackage.startsWith(pkg)) {
              String conversion = getClientPackageConversions().get(pkg) + ".json";
              conversions.put(serverSidePackage, conversion + serverSidePackage.substring(pkg.length()));
              conversionFound = true;
              break;
            }
          }
        }
      }

      if (!conversionFound) {
        conversions.put(serverSidePackage, serverSidePackage + ".json");
      }
    }
    return conversions;
  }

  /**
   * The generate directory for the java client classes.
   *
   * @return The generate directory for the java client classes.
   */
  protected File getClientGenerateDir() {
    return new File(getGenerateDir(), "java");
  }

  /**
   * The generate directory for the java json client classes.
   *
   * @return The generate directory for the java json client classes.
   */
  protected File getJsonClientGenerateDir() {
    return new File(getGenerateDir(), "json");
  }

  /**
   * Get the destination for the specified declaration if the server-side type is to be used.
   *
   * @param sourceFile  The source file.
   * @param declaration The declaration.
   * @return The destination file.
   */
  protected File getServerSideDestFile(File sourceFile, TypeDeclaration declaration) {
    File destDir = getGenerateDir();
    String packageName = declaration.getPackage().getQualifiedName();
    for (StringTokenizer packagePaths = new StringTokenizer(packageName, "."); packagePaths.hasMoreTokens();) {
      String packagePath = packagePaths.nextToken();
      destDir = new File(destDir, packagePath);
    }
    destDir.mkdirs();
    return new File(destDir, sourceFile.getName());
  }

  /**
   * Whether to use the server-side declaration for this declaration.
   *
   * @param declaration The declaration.
   * @param matcher     The matcher.
   * @return Whether to use the server-side declaration for this declaration.
   */
  protected boolean useServerSide(TypeDeclaration declaration, AntPatternMatcher matcher) {
    boolean useServerSide = false;

    for (String pattern : serverSideTypesToUse) {
      if (matcher.match(pattern, declaration.getQualifiedName())) {
        useServerSide = true;
        break;
      }
    }
    return useServerSide;
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

    //Compile the java files.
    if (!enunciate.isUpToDateWithSources(getCompileDir())) {
      Collection<String> javaSourceFiles = enunciate.getJavaFiles(getClientGenerateDir());
      String clientClasspath = enunciate.getEnunciateBuildClasspath(); //we use the build classpath for client-side jars so you don't have to include client-side dependencies on the server-side.
      enunciate.invokeJavac(clientClasspath, "1.5", getClientCompileDir(), new ArrayList<String>(), javaSourceFiles.toArray(new String[javaSourceFiles.size()]));

      for (DeploymentModule module : enunciate.getConfig().getEnabledModules()) {
        if (module instanceof XMLDeploymentModule) {
          XMLDeploymentModule xmlModule = (XMLDeploymentModule) module;
          enunciate.copyDir(xmlModule.getGenerateDir(), getClientCompileDir());
        }
      }

      if (isGenerateJsonJar()) {
        Collection<String> jsonSourceFiles = enunciate.getJavaFiles(getJsonClientGenerateDir());
        clientClasspath = enunciate.getEnunciateBuildClasspath(); //we use the build classpath for client-side jars so you don't have to include client-side dependencies on the server-side.
        enunciate.invokeJavac(clientClasspath, "1.5", getJsonClientCompileDir(), new ArrayList<String>(), jsonSourceFiles.toArray(new String[jsonSourceFiles.size()]));
      }
    }
    else {
      info("Skipping compilation of Java client classes as everything appears up-to-date...");
    }
  }

  /**
   * The generate directory for the java client classes.
   *
   * @return The generate directory for the java client classes.
   */
  protected File getClientCompileDir() {
    return new File(getCompileDir(), "java");
  }

  /**
   * The generate directory for the java json client classes.
   *
   * @return The generate directory for the java json client classes.
   */
  protected File getJsonClientCompileDir() {
    return new File(getCompileDir(), "json");
  }

  @Override
  protected void doBuild() throws EnunciateException, IOException {
    Enunciate enunciate = getEnunciate();
    String jarName = getJarName();
    String jsonJarName = getJsonJarName();

    String label = "enunciate";
    if (getLabel() != null) {
      label = getLabel();
    }
    else if ((enunciate.getConfig() != null) && (enunciate.getConfig().getLabel() != null)) {
      label = enunciate.getConfig().getLabel();
    }

    if (jarName == null) {
      jarName = label + "-client.jar";
    }

    if (jsonJarName == null) {
      jsonJarName = label + "-json-client.jar";
    }

    File clientJarFile = new File(getBuildDir(), jarName);
    if (!enunciate.isUpToDate(getClientCompileDir(), clientJarFile)) {
      enunciate.zip(clientJarFile, getClientCompileDir());
    }
    else {
      info("Skipping creation of Java client jar as everything appears up-to-date...");
    }

    File clientSourcesJarFile = new File(getBuildDir(), jarName.replaceFirst("\\.jar", "-sources.jar"));
    if (!enunciate.isUpToDate(getClientGenerateDir(), clientSourcesJarFile)) {
      enunciate.zip(clientSourcesJarFile, getClientGenerateDir());
    }
    else {
      info("Skipping creation of the Java client source jar as everything appears up-to-date...");
    }

    ClientLibraryArtifact artifactBundle = new ClientLibraryArtifact(getName(), "java.client.library", "Java Client Library");
    artifactBundle.setPlatform("Java (Version 5+)");
    artifactBundle.addAlias("jaxws.client.library");
    //read in the description from file:
    artifactBundle.setDescription(readResource("library_description.fmt"));
    NamedFileArtifact binariesJar = new NamedFileArtifact(getName(), "java.client.library.binaries", clientJarFile);
    binariesJar.addAlias("jaxws.client.library.binaries");
    binariesJar.setDescription("The binaries for the Java client library.");
    binariesJar.setPublic(false);
    binariesJar.setArtifactType(ArtifactType.binaries);
    artifactBundle.addArtifact(binariesJar);
    NamedFileArtifact sourcesJar = new NamedFileArtifact(getName(), "java.client.library.sources", clientSourcesJarFile);
    sourcesJar.addAlias("jaxws.client.library.sources");
    sourcesJar.setDescription("The sources for the Java client library.");
    sourcesJar.setPublic(false);
    sourcesJar.setArtifactType(ArtifactType.sources);
    artifactBundle.addArtifact(sourcesJar);
    enunciate.addArtifact(binariesJar);
    enunciate.addArtifact(sourcesJar);
    enunciate.addArtifact(artifactBundle);

    if (isGenerateJsonJar()) {
      File jsonClientJarFile = new File(getBuildDir(), jsonJarName);
      if (!enunciate.isUpToDate(getJsonClientCompileDir(), jsonClientJarFile)) {
        enunciate.zip(jsonClientJarFile, getJsonClientCompileDir());
      }
      else {
        info("Skipping creation of Java JSON client jar as everything appears up-to-date...");
      }

      File jsonClientSourcesJarFile = new File(getBuildDir(), jsonJarName.replaceFirst("\\.jar", "-sources.jar"));
      if (!enunciate.isUpToDate(getJsonClientGenerateDir(), jsonClientSourcesJarFile)) {
        enunciate.zip(jsonClientSourcesJarFile, getJsonClientGenerateDir());
      }
      else {
        info("Skipping creation of the Java JSON client source jar as everything appears up-to-date...");
      }

      artifactBundle = new ClientLibraryArtifact(getName(), "java.json.client.library", "Java JSON Client Library");
      artifactBundle.setPlatform("Java (Version 5+)");
      //read in the description from file:
      artifactBundle.setDescription(readResource("json_library_description.fmt"));
      binariesJar = new NamedFileArtifact(getName(), "java.json.client.library.binaries", clientJarFile);
      binariesJar.setDescription("The binaries for the Java JSON client library.");
      binariesJar.setPublic(false);
      binariesJar.setArtifactType(ArtifactType.binaries);
      artifactBundle.addArtifact(binariesJar);
      sourcesJar = new NamedFileArtifact(getName(), "java.json.client.library.sources", clientSourcesJarFile);
      sourcesJar.setDescription("The sources for the Java JSON client library.");
      sourcesJar.setPublic(false);
      sourcesJar.setArtifactType(ArtifactType.sources);
      artifactBundle.addArtifact(sourcesJar);
      enunciate.addArtifact(binariesJar);
      enunciate.addArtifact(sourcesJar);
      enunciate.addArtifact(artifactBundle);
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

    URL res = JavaClientDeploymentModule.class.getResource(resource);
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
    return JavaClientDeploymentModule.class.getResource(template);
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
   * The name of the json client jar.
   *
   * @return The name of the json client jar.
   */
  public String getJsonJarName() {
    return jsonJarName;
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
   * The name of the json client jar.
   *
   * @param jarName The name of the json client jar.
   */
  public void setJsonJarName(String jarName) {
    this.jsonJarName = jarName;
  }

  /**
   * An Java configuration rule set.
   *
   * @return An Java configuration rule set.
   */
  @Override
  public RuleSet getConfigurationRules() {
    return this.configurationRules;
  }

  /**
   * An java-client validator.
   *
   * @return An java-client validator.
   */
  @Override
  public Validator getValidator() {
    return new JavaClientValidator(getServerSideTypesToUse(), getClientPackageConversions());
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
   * The json client package conversions.
   *
   * @return The json client package conversions.
   */
  public Map<String, String> getJsonClientPackageConversions() {
    return jsonClientPackageConversions;
  }

  /**
   * Add a client package conversion.
   *
   * @param conversion The conversion to add.
   */
  public void addJsonClientPackageConversion(ClientPackageConversion conversion) {
    String from = conversion.getFrom();
    String to = conversion.getTo();

    if (from == null) {
      throw new IllegalArgumentException("A 'from' attribute must be specified on a clientPackageConversion element.");
    }

    if (to == null) {
      throw new IllegalArgumentException("A 'to' attribute must be specified on a clientPackageConversion element.");
    }

    this.jsonClientPackageConversions.put(from, to);
  }

  /**
   * The server-side types that are to be used for the client-side libraries.
   *
   * @return The server-side types that are to be used for the client-side libraries.
   */
  public Set<String> getServerSideTypesToUse() {
    return serverSideTypesToUse;
  }

  /**
   * Add a server-side type to use for the client-side library.
   *
   * @param serverSideTypeToUse The server-side type to use.
   */
  public void addServerSideTypeToUse(String serverSideTypeToUse) {
    this.serverSideTypesToUse.add(serverSideTypeToUse);
  }

  /**
   * The label for the JAX-WS Client API.
   *
   * @return The label for the  JAX-WS Client API.
   */
  public String getLabel() {
    return label;
  }

  /**
   * The label for the  JAX-WS Client API.
   *
   * @param label The label for the  JAX-WS Client API.
   */
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * Whether to generate the JSON client jar.
   *
   * @return Whether to generate the JSON client jar.
   */
  public boolean isGenerateJsonJar() {
    return forceGenerateJsonJar || (!disableJsonJar && jacksonXcAvailable && existsAnyJsonResourceMethod(getModelInternal().getRootResources()));
  }

  /**
   * Whether any root resources exist that produce json.
   *
   * @param rootResources The root resources.
   * @return Whether any root resources exist that produce json.
   */
  protected boolean existsAnyJsonResourceMethod(List<RootResource> rootResources) {
    for (RootResource rootResource : rootResources) {
      for (ResourceMethod resourceMethod : rootResource.getResourceMethods(true)) {
        for (String mime : resourceMethod.getProducesMime()) {
          if ("*/*".equals(mime)) {
            return true;
          }
          else if (mime.toLowerCase().contains("json")) {
            return true;
          }
        }
        for (String mime : resourceMethod.getConsumesMime()) {
          if ("*/*".equals(mime)) {
            return true;
          }
          else if (mime.toLowerCase().contains("json")) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Whether to generate the JSON client jar.
   *
   * @param generateJsonJar Whether to generate the JSON jar.
   */
  public void setGenerateJsonJar(boolean generateJsonJar) {
    this.forceGenerateJsonJar = generateJsonJar;
    this.disableJsonJar = !generateJsonJar;
  }

  // Inherited.
  @Override
  public boolean isDisabled() {
    if (super.isDisabled()) {
      return true;
    }
    else if (getModelInternal() != null && getModelInternal().getNamespacesToWSDLs().isEmpty() && getModelInternal().getNamespacesToSchemas().isEmpty()) {
      debug("Java client module is disabled because there are no endpoint interfaces, nor any root schema types.");
      return true;
    }

    return false;
  }

  public List<File> getProjectSources() {
    return Collections.emptyList();
  }

  public List<File> getProjectTestSources() {
    return Arrays.asList(getJsonClientGenerateDir(), getClientGenerateDir());
  }

  public List<File> getProjectResourceDirectories() {
    return Collections.emptyList();
  }

  public List<File> getProjectTestResourceDirectories() {
    ArrayList<File> testResources = new ArrayList<File>();

    //the java-client requires the wsdl and schemas on the classpath.
    for (DeploymentModule enabledModule : getEnunciate().getConfig().getEnabledModules()) {
      if (enabledModule instanceof XMLDeploymentModule) {
        testResources.add(((XMLDeploymentModule) enabledModule).getGenerateDir());
      }
    }

    return testResources;
  }
}
