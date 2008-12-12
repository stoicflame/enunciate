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

package org.codehaus.enunciate.modules.jaxws_client;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import freemarker.template.*;
import net.sf.jelly.apt.decorations.JavaDoc;
import net.sf.jelly.apt.freemarker.FreemarkerJavaDoc;
import org.apache.commons.digester.RuleSet;
import org.codehaus.enunciate.EnunciateException;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.config.SchemaInfo;
import org.codehaus.enunciate.config.WsdlInfo;
import org.codehaus.enunciate.contract.jaxb.TypeDefinition;
import org.codehaus.enunciate.contract.jaxws.*;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.main.ClientLibraryArtifact;
import org.codehaus.enunciate.main.Enunciate;
import org.codehaus.enunciate.main.NamedFileArtifact;
import org.codehaus.enunciate.modules.FreemarkerDeploymentModule;
import org.codehaus.enunciate.modules.DeploymentModule;
import org.codehaus.enunciate.modules.xml.XMLDeploymentModule;
import org.codehaus.enunciate.modules.jaxws_client.config.ClientPackageConversion;
import org.codehaus.enunciate.modules.jaxws_client.config.JAXWSClientRuleSet;
import org.codehaus.enunciate.template.freemarker.*;
import org.codehaus.enunciate.util.AntPatternMatcher;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * <h1>JAX-WS Client Module</h1>
 *
 * <p>The JAX-WS client deployment module generates the client-side libraries that will access the
 * deployed web app using <a href="https://jax-ws.dev.java.net/">JAX-WS</a>. (As of Java 6, JAX-WS ships with the JDK.)</p>
 *
 * <p>A useful by-product of this module is the generated Java types that can be used via JAXB to serialize the client-side
 * objects to/from XML. These can be used, for example, to use Java objects to interface via REST endpoints.</p>
 *
 * <p>The order of the JAX-WS client deployment module is 50, so as to allow the JAX-WS module to apply
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
 * <p>The "generate" step is by far the most intensive and complex step in the execution of the JAX-WS client
 * module.  The "generate" step generates all source code for accessing the deployed API.</p>
 *
 * <h3>compile</h3>
 *
 * <p>During the "compile" step, the JAX-WS client module compiles the code that was generated.</p>
 *
 * <h3>build</h3>
 *
 * <p>The "build" step assembles the classes that were assembled into a jar.  It also creates a source jar for
 * the libraries.</p>
 *
 * <h1><a name="config">Configuration</a></h1>
 *
 * <p>The JAX-WS client module is configured by the "jaxws-client" element under the "modules" element of the
 * enunciate configuration file.  It supports the following attributes:</p>
 *
 * <ul>
 * <li>The "jarName" attribute specifies the name of the jar file(s) that are to be created.  If no jar name is specified,
 * the name will be calculated from the enunciate label, or a default will be supplied.</li>
 * </ul>
 *
 * <h3>The "package-conversions" element</h3>
 *
 * <p>The "package-conversions" subelement of the "jaxws-client" element is used to map packages from
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
 * <h3>The "server-side-type" element</h3>
 *
 * <p>An arbitrary number of "server-side-type" elements are allowed as child elements of the "jaxws-client" element.  The "server-side-type" element
 * can be used to specify a server-side type that is to be ported directly over to the client-side library (as opposed to <i>generating</i> the client-side type
 * from the server-side type). This can be useful to provide more useful client-side capabilities, but requires that there be no package conversions for types
 * and web faults.</p>
 *
 * <p>The "server-side-type" element supports one attribute, "pattern" that defines an ant-style pattern of the type(s) that are to be included (using a '.'
 * for separating the package name).</p>
 *
 * <h1><a name="artifacts">Artifacts</a></h1>
 *
 * <p>The JAX-WS client deployment module exports the following artifacts:</p>
 *
 * <ul>
 * <li>The libraries and sources are exported under the id "jaxws.client.library".  (Note that this is a
 * bundle, so if exporting to a directory multiple files will be exported.  If exporting to a file, the bundle will
 * be zipped first.)</li>
 * </ul>
 *
 * @author Ryan Heaton
 * @docFileName module_jaxws_client.html
 */
public class JAXWSClientDeploymentModule extends FreemarkerDeploymentModule {

  private String jarName = null;
  private final Map<String, String> clientPackageConversions;
  private final JAXWSClientRuleSet configurationRules;
  private final Set<String> serverSideTypesToUse;

  public JAXWSClientDeploymentModule() {
    this.clientPackageConversions = new HashMap<String, String>();
    this.configurationRules = new JAXWSClientRuleSet();
    this.serverSideTypesToUse = new TreeSet<String>();
    setDisabled(true); //disable by default, for now.
  }

  /**
   * @return "jaxws-client"
   */
  @Override
  public String getName() {
    return "jaxws-client";
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
      throw new EnunciateException("The JAX-WS Client module requires you to enable the XML module.");
    }
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

      //set up the model, first allowing for jdk 14 compatability.
      EnunciateFreemarkerModel model = getModel();
      Map<String, String> conversions = getClientPackageConversions();
      ClientClassnameForMethod classnameFor = new ClientClassnameForMethod(conversions);
      ComponentTypeForMethod componentTypeFor = new ComponentTypeForMethod(conversions);
      CollectionTypeForMethod collectionTypeFor = new CollectionTypeForMethod(conversions);
      classnameFor.setJdk15(true);
      componentTypeFor.setJdk15(true);
      collectionTypeFor.setJdk15(true);
      model.put("packageFor", new ClientPackageForMethod(conversions));
      model.put("classnameFor", classnameFor);
      model.put("simpleNameFor", new SimpleNameWithParamsMethod(classnameFor));
      model.put("componentTypeFor", componentTypeFor);
      model.put("collectionTypeFor", collectionTypeFor);

      info("Generating the JAX-WS client classes...");
      model.setFileOutputDirectory(generateDir);
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
        String faultBean = implicit ? getBeanName(classnameFor, webFault.getImplicitFaultBeanQualifiedName()) : classnameFor.convert(webFault.getExplicitFaultBean());
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
        }
      }
    }
    else {
      info("Skipping generation of JAX-WS Client sources as everything appears up-to-date...");
    }
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
    File generateDir = getGenerateDir();
    Collection<String> typeFiles = enunciate.getJavaFiles(generateDir);

    //Compile the jdk15 files.
    File compileDir = getCompileDir();
    if (!enunciate.isUpToDateWithSources(compileDir)) {
      Collection<String> jdk15Files = enunciate.getJavaFiles(generateDir);
      jdk15Files.addAll(typeFiles);
      enunciate.invokeJavac(enunciate.getEnunciateClasspath(), "1.5", compileDir, new ArrayList<String>(), jdk15Files.toArray(new String[jdk15Files.size()]));

      for (DeploymentModule module : enunciate.getConfig().getEnabledModules()) {
        if (module instanceof XMLDeploymentModule) {
          XMLDeploymentModule xmlModule = (XMLDeploymentModule) module;
          enunciate.copyDir(xmlModule.getGenerateDir(), compileDir);
        }
      }
    }
    else {
      info("Skipping compilation of JAX-WS client classes as everything appears up-to-date...");
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

    File jdk15Jar = new File(getBuildDir(), jarName);
    if (!enunciate.isUpToDate(getCompileDir(), jdk15Jar)) {
      enunciate.zip(jdk15Jar, getCompileDir());
      enunciate.setProperty("jaxws.client.jar", jdk15Jar);
    }
    else {
      info("Skipping creation of JAX-WS client jar as everything appears up-to-date...");
    }

    File jdk15Sources = new File(getBuildDir(), jarName.replaceFirst("\\.jar", "-src.jar"));
    if (!enunciate.isUpToDate(getGenerateDir(), jdk15Sources)) {
      enunciate.zip(jdk15Sources, getGenerateDir());
      enunciate.setProperty("jaxws.client.sources", jdk15Sources);
    }
    else {
      info("Skipping creation of the JAX-WS client source jar as everything appears up-to-date...");
    }

    //todo: generate the javadocs?

    ClientLibraryArtifact artifactBundle = new ClientLibraryArtifact(getName(), "jaxws.client.library", "JAX-WS Client Library");
    artifactBundle.setPlatform("Java (Version 5+)");
    //read in the description from file:
    artifactBundle.setDescription(readResource("library_description.html"));
    NamedFileArtifact binariesJar = new NamedFileArtifact(getName(), "jaxws.client.library.binaries", jdk15Jar);
    binariesJar.setDescription("The binaries for the JAX-WS client library.");
    binariesJar.setPublic(false);
    artifactBundle.addArtifact(binariesJar);
    NamedFileArtifact sourcesJar = new NamedFileArtifact(getName(), "jaxws.client.library.sources", jdk15Sources);
    sourcesJar.setDescription("The sources for the JAX-WS client library.");
    sourcesJar.setPublic(false);
    artifactBundle.addArtifact(sourcesJar);
    enunciate.addArtifact(binariesJar);
    enunciate.addArtifact(sourcesJar);
    enunciate.addArtifact(artifactBundle);
  }

  /**
   * Reads a resource into string form.
   *
   * @param resource The resource to read.
   * @return The string form of the resource.
   */
  protected String readResource(String resource) throws IOException {
    InputStream resourceIn = JAXWSClientDeploymentModule.class.getResourceAsStream(resource);
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
    return JAXWSClientDeploymentModule.class.getResource(template);
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
   * An JAX-WS configuration rule set.
   *
   * @return An JAX-WS configuration rule set.
   */
  @Override
  public RuleSet getConfigurationRules() {
    return this.configurationRules;
  }

  /**
   * An jaxws-client validator.
   *
   * @return An jaxws-client validator.
   */
  @Override
  public Validator getValidator() {
    return new JAXWSClientValidator(getServerSideTypesToUse(), getClientPackageConversions());
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

  // Inherited.
  @Override
  public boolean isDisabled() {
    if (super.isDisabled()) {
      return true;
    }
    else if (getModelInternal() != null && getModelInternal().getNamespacesToWSDLs().isEmpty() && getModelInternal().getNamespacesToSchemas().isEmpty()) {
      debug("JAX-WS client module is disabled because there are no endpoint interfaces, nor any XML types.");
      return true;
    }

    return false;
  }
}
