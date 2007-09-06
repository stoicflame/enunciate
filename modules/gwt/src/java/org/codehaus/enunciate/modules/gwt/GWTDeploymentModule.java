/*
 * Copyright 2006 Web Cohesion
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

package org.codehaus.enunciate.modules.gwt;

import freemarker.template.*;
import net.sf.jelly.apt.decorations.JavaDoc;
import net.sf.jelly.apt.freemarker.FreemarkerJavaDoc;
import org.apache.commons.digester.RuleSet;
import org.codehaus.enunciate.EnunciateException;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.config.SchemaInfo;
import org.codehaus.enunciate.config.WsdlInfo;
import org.codehaus.enunciate.contract.jaxb.TypeDefinition;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.jaxws.WebFault;
import org.codehaus.enunciate.contract.jaxws.WebMethod;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.main.*;
import org.codehaus.enunciate.modules.FreemarkerDeploymentModule;
import org.codehaus.enunciate.modules.xfire_client.CollectionTypeForMethod;
import org.codehaus.enunciate.modules.xfire_client.ComponentTypeForMethod;
import org.codehaus.enunciate.modules.xfire_client.ClientPackageForMethod;
import org.codehaus.enunciate.modules.xfire_client.ClientLibraryArtifact;
import org.codehaus.enunciate.modules.gwt.config.GWTRuleSet;
import org.codehaus.enunciate.util.ClassDeclarationComparator;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * <h1>GWT Module</h1>
 *
 * <p>The GWT deployment module generates the server-side and client-side libraries used to support a
 * <a href="http://code.google.com/webtoolkit/">GWT RPC</a> API.</p>
 *
 * <p>The order of the GWT deployment module is 0, as it doesn't depend on any artifacts exported
 * by any other module.</p>
 *
 * <ul>
 *   <li><a href="#steps">steps</a></li>
 *   <li><a href="#config">configuration</a></li>
 *   <li><a href="#artifacts">artifacts</a></li>
 * </ul>
 *
 * <h1><a name="steps">Steps</a></h1>
 *
 * <h3>generate</h3>
 *
 * <p>The "generate" step is by far the most intensive and complex step in the execution of the GWT
 * module.  The "generate" step generates all source code for the GWT API.</p>
 *
 * <h3>compile</h3>
 *
 * <p>During the "compile" step, the GWT module compiles the code that was generated.</p>
 *
 * <h3>build</h3>
 *
 * <p>The "build" step assembles the client-side GWT jar.</p>
 *
 * <h1><a name="config">Configuration</a></h1>
 *
 * <p>The GWT module is configured by the "gwt" element under the "modules" element of the
 * enunciate configuration file.  <b>The GWT module is disabled by default because of the
 * added constraints applied to the service endpoints.</b>  To enable GWT, be sure to specify
 * <i>disabled="false"</i> on the "gwt" element.  It also supports the following attributes:</p>
 *
 * <ul>
 *   <li>The "gwtModuleName" attribute <b>must</b> be supplied.  The GWT module name will also be used to
 *       determine the layout of the created module.  The module name must be of the form "com.mycompany.MyModuleName".
 *       In this example, "com.mycompany" will be the <i>module namespace</i> and all client code will be generated into
 *       a package named of the form [module namespace].client (e.g. "com.mycompany.client").  <i>Note: in order to provide
 *       a sensible mapping from service code to GWT client-side code, all service endpoints, faults, and JAXB beans must
 *       exist in a package that matches the module namespace, or a subpackage thereof</i></li>
 *   <li>The "clientJarName" attribute specifies the name of the client-side jar file that is to be created.
 *       If no jar name is specified, the name will be calculated from the enunciate label, or a default will
 *       be supplied.</li>
 * </ul>
 *
 * <h1><a name="artifacts">Artifacts</a></h1>
 *
 * <p>The "gwt.client.jar" artifact is the packaged client-side GWT jar.</p>
 *
 * @author Ryan Heaton
 */
public class GWTDeploymentModule extends FreemarkerDeploymentModule {

  private String gwtModuleNamespace = null;
  private String gwtModuleName = null;
  private String clientJarName = null;
  private final GWTRuleSet configurationRules;

  public GWTDeploymentModule() {
    this.configurationRules = new GWTRuleSet();

    setDisabled(true);//disable the GWT module by default because it adds unnecessary contraints on the API.
  }

  /**
   * @return "gwt"
   */
  @Override
  public String getName() {
    return "gwt";
  }


  @Override
  public void init(Enunciate enunciate) throws EnunciateException {
    super.init(enunciate);

    if (gwtModuleName == null) {
      throw new EnunciateException("You must specify a \"gwtModuleName\" for the GWT module.");
    }
    if (gwtModuleNamespace == null) {
      throw new EnunciateException("You must specify a \"gwtModuleName\" for the GWT module.");
    }
  }

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException {
    //load the references to the templates....
    URL complexMapperTemplate = getTemplateURL("gwt-complex-type-mapper.fmt");
    URL simpleMapperTemplate = complexMapperTemplate;
    URL faultMapperTemplate = getTemplateURL("gwt-fault-mapper.fmt");
    URL moduleXmlTemplate = getTemplateURL("gwt-module-xml.fmt");

    URL eiTemplate = getTemplateURL("gwt-endpoint-interface.fmt");
    URL endpointImplTemplate = getTemplateURL("gwt-endpoint-impl.fmt");
    URL faultTemplate = getTemplateURL("gwt-fault.fmt");
    URL complexTypeTemplate = getTemplateURL("gwt-complex-type.fmt");
    URL simpleTypeTemplate = complexTypeTemplate;
    URL enumTypeTemplate = getTemplateURL("gwt-enum-type.fmt");

    //set up the model, first allowing for jdk 14 compatability.
    EnunciateFreemarkerModel model = getModel();
    Map<String, String> conversions = getClientPackageConversions();
    ClientClassnameForMethod classnameFor = new ClientClassnameForMethod(conversions);
    ComponentTypeForMethod componentTypeFor = new ComponentTypeForMethod(conversions);
    CollectionTypeForMethod collectionTypeFor = new CollectionTypeForMethod(conversions);
    model.put("packageFor", new ClientPackageForMethod(conversions));
    model.put("classnameFor", classnameFor);
    model.put("componentTypeFor", componentTypeFor);
    model.put("collectionTypeFor", collectionTypeFor);

    model.setFileOutputDirectory(getClientSideGenerateDir());

    TreeSet<WebFault> allFaults = new TreeSet<WebFault>(new ClassDeclarationComparator());
    info("Generating the GWT endpoints...");
    for (WsdlInfo wsdlInfo : model.getNamespacesToWSDLs().values()) {
      for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
        model.put("endpointInterface", ei);
        processTemplate(eiTemplate, model);

        for (WebMethod webMethod : ei.getWebMethods()) {
          for (WebFault webFault : webMethod.getWebFaults()) {
            allFaults.add(webFault);
          }
        }
      }
    }

    info("Generating the GWT faults...");
    for (WebFault webFault : allFaults) {
      model.put("fault", webFault);
      processTemplate(faultTemplate, model);
    }

    info("Generating the GWT types...");
    for (SchemaInfo schemaInfo : model.getNamespacesToSchemas().values()) {
      for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
        model.put("type", typeDefinition);
        URL template = typeDefinition.isEnum() ? enumTypeTemplate : typeDefinition.isSimple() ? simpleTypeTemplate : complexTypeTemplate;
        processTemplate(template, model);
      }
    }

    model.put("gwtModuleName", this.gwtModuleName);
    processTemplate(moduleXmlTemplate, model);

    model.setFileOutputDirectory(getServerSideGenerateDir());

    info("Generating the GWT endpoint implementations...");
    for (WsdlInfo wsdlInfo : model.getNamespacesToWSDLs().values()) {
      for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
        model.put("endpointInterface", ei);
        processTemplate(endpointImplTemplate, model);
      }
    }

    info("Generating the GWT type mappers...");
    for (SchemaInfo schemaInfo : model.getNamespacesToSchemas().values()) {
      for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
        if (!typeDefinition.isEnum()) {
          model.put("type", typeDefinition);
          URL template = typeDefinition.isSimple() ? simpleMapperTemplate : complexMapperTemplate;
          processTemplate(template, model);
        }
      }
    }

    info("Generating the GWT fault mappers...");
    for (WebFault webFault : allFaults) {
      model.put("fault", webFault);
      processTemplate(faultMapperTemplate, model);
    }

    enunciate.setProperty("gwt.server.src.dir", getServerSideGenerateDir());
    enunciate.addArtifact(new FileArtifact(getName(), "gwt.server.src.dir", getServerSideGenerateDir()));
  }

  @Override
  protected void doCompile() throws EnunciateException, IOException {
    Enunciate enunciate = getEnunciate();

    info("Compiling the GWT client-side files...");
    Collection<String> clientSideFiles = enunciate.getJavaFiles(getClientSideGenerateDir());
    enunciate.invokeJavac(enunciate.getDefaultClasspath(), getClientSideCompileDir(), Arrays.asList("-source", "1.4", "-g"), clientSideFiles.toArray(new String[clientSideFiles.size()]));
    enunciate.setProperty("gwt.client.compile.dir", getClientSideCompileDir());
  }

  @Override
  protected void doBuild() throws EnunciateException, IOException {
    Enunciate enunciate = getEnunciate();
    String clientJarName = getClientJarName();

    if (clientJarName == null) {
      String label = "enunciate";
      if ((enunciate.getConfig() != null) && (enunciate.getConfig().getLabel() != null)) {
        label = enunciate.getConfig().getLabel();
      }

      clientJarName = label + "-gwt-client.jar";
    }

    File clientJar = new File(getBuildDir(), clientJarName);
    enunciate.zip(clientJar, getClientSideGenerateDir(), getClientSideCompileDir());
    enunciate.setProperty("gwt.client.jar", clientJar);

    List<ArtifactDependency> clientDeps = new ArrayList<ArtifactDependency>();
    MavenDependency gwtUserDependency = new MavenDependency();
    gwtUserDependency.setId("gwt-user");
    gwtUserDependency.setArtifactType("jar");
    gwtUserDependency.setDescription("Base GWT classes.");
    gwtUserDependency.setGroupId("com.google.gwt");
    gwtUserDependency.setURL("http://code.google.com/webtoolkit/");
    gwtUserDependency.setVersion("1.4.59");
    clientDeps.add(gwtUserDependency);

    MavenDependency gwtWidgetsDependency = new MavenDependency();
    gwtWidgetsDependency.setId("gwt-widgets");
    gwtWidgetsDependency.setArtifactType("jar");
    gwtWidgetsDependency.setDescription("GWT widget library.");
    gwtWidgetsDependency.setGroupId("org.gwtwidgets");
    gwtWidgetsDependency.setURL("http://gwt-widget.sourceforge.net/");
    gwtWidgetsDependency.setVersion("1.5.0");
    clientDeps.add(gwtWidgetsDependency);

    ClientLibraryArtifact gwtClientArtifact = new ClientLibraryArtifact(getName(), "gwt.client.library", "GWT Client Library");
    gwtClientArtifact.setPlatform("JavaScript/GWT (Version 1.4.59)");
    //read in the description from file:
    gwtClientArtifact.setDescription(readResource("client_library_description.html"));
    NamedFileArtifact clientArtifact = new NamedFileArtifact(getName(), "gwt.client.jar", clientJar);
    clientArtifact.setDescription("The binaries and sources for the GWT client library.");
    clientArtifact.setBundled(true);
    gwtClientArtifact.addArtifact(clientArtifact);
    gwtClientArtifact.setDependencies(clientDeps);
    enunciate.addArtifact(clientArtifact);
    enunciate.addArtifact(gwtClientArtifact);
  }

  /**
   * Reads a resource into string form.
   *
   * @param resource The resource to read.
   * @return The string form of the resource.
   */
  protected String readResource(String resource) throws IOException {
    InputStream resourceIn = GWTDeploymentModule.class.getResourceAsStream(resource);
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
    return GWTDeploymentModule.class.getResource(template);
  }

  /**
   * Get the generate directory for server-side GWT classes.
   *
   * @return The generate directory for server-side GWT classes.
   */
  protected File getServerSideGenerateDir() {
    return new File(getGenerateDir(), "server");
  }

  /**
   * Get the generate directory for client-side GWT classes.
   *
   * @return The generate directory for client-side GWT classes.
   */
  protected File getClientSideGenerateDir() {
    return new File(getGenerateDir(), "client");
  }

  /**
   * Get the compile directory for client-side GWT classes.
   *
   * @return The compile directory for client-side GWT classes.
   */
  protected File getClientSideCompileDir() {
    return new File(getCompileDir(), "client");
  }

  /**
   * The name of the client jar.
   *
   * @return The name of the client jar.
   */
  public String getClientJarName() {
    return clientJarName;
  }

  /**
   * The name of the client jar.
   *
   * @param clientJarName The name of the client jar.
   */
  public void setClientJarName(String clientJarName) {
    this.clientJarName = clientJarName;
  }

  /**
   * GWT configuration rule set.
   *
   * @return GWT configuration rule set.
   */
  @Override
  public RuleSet getConfigurationRules() {
    return this.configurationRules;
  }

  /**
   * GWT validator.
   *
   * @return GWT validator.
   */
  @Override
  public Validator getValidator() {
    return new GWTValidator(this.gwtModuleNamespace);
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
    final String clientPackage = this.gwtModuleNamespace + ".client";
    return new AbstractMap<String, String>() {
      public Set<Entry<String, String>> entrySet() {
        HashSet<Entry<String, String>> entrySet = new HashSet<Entry<String, String>>();
        entrySet.add(new Entry<String, String>() {
          public String getKey() {
            return gwtModuleNamespace;
          }

          public String getValue() {
            return clientPackage;
          }

          public String setValue(String value) {
            throw new UnsupportedOperationException();
          }
        });
        return entrySet;
      }
    };
  }

  public void setGwtModuleName(String gwtModuleName) {
    this.gwtModuleName = gwtModuleName;
    int lastDot = gwtModuleName.lastIndexOf('.');
    if (lastDot < 0) {
      throw new IllegalArgumentException("The gwt module name must be of the form 'gwt.module.ns.ModuleName'");
    }
    this.gwtModuleNamespace = gwtModuleName.substring(0, lastDot);
  }
}
