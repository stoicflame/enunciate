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

package org.codehaus.enunciate.modules.amf;

import com.sun.mirror.declaration.Declaration;
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
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.main.*;
import org.codehaus.enunciate.modules.FreemarkerDeploymentModule;
import org.codehaus.enunciate.modules.amf.config.AMFRuleSet;
import org.codehaus.enunciate.modules.amf.config.FlexApp;
import org.codehaus.enunciate.modules.amf.config.FlexCompilerConfig;
import org.codehaus.enunciate.modules.amf.config.License;
import org.codehaus.enunciate.template.freemarker.ClientPackageForMethod;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * <h1>GWT Module</h1>
 *
 * <p>The GWT deployment module generates the server-side and client-side libraries used to support a
 * <a href="http://code.google.com/webtoolkit/">GWT RPC</a> API. There is also support for invoking the
 * GWTCompiler to compile a set a GWT applications that can be included in the generated Enunciate web
 * application.</p>
 *
 * <p>The order of the GWT deployment module is 0, as it doesn't depend on any artifacts exported
 * by any other module.</p>
 *
 * <p>This documentation is an overview of how to use Enunciate to build your GWT-RPC API and (optional)
 * associated GWT application. The reader is redirected to the
 * <a href="http://code.google.com/webtoolkit/">documentation for the GWT</a> for instructions on how to use GWT.
 * You may also find the petclinic sample application useful as an illustration.  The sample petclinic application
 * is included with the Enunciate distribution.</p>
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
 * <p>The "generate" step generates all source code for the GWT-RPC API.</p>
 *
 * <h3>compile</h3>
 *
 * <p>During the "compile" step, the GWT module compiles the code that was generated. It is also during the "compile" step that
 * the GWTCompiler is invoked on any GWT applications that were specified in the configuration.</p>
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
 * <i>disabled="false"</i> on the "gwt" element.</p>
 *
 * <p>The "gwt" element supports the following attributes:</p>
 *
 * <ul>
 * <li>The "rpcModuleName" attribute <b>must</b> be supplied.  The RPC module name will also be used to
 * determine the layout of the created module.  The module name must be of the form "com.mycompany.MyModuleName".
 * In this example, "com.mycompany" will be the <i>module namespace</i> and all client code will be generated into
 * a package named of the form [module namespace].client (e.g. "com.mycompany.client").  By default, in order to provide
 * a sensible mapping from service code to GWT client-side code, all service endpoints, faults, and JAXB beans must
 * exist in a package that matches the module namespace, or a subpackage thereof.  Use the "enforceNamespaceConformance"
 * attribute to loosen this requirement.</li>
 * <li>The "enforceNamespaceConformance" attribute allows you to lift the requirement that all classes must exist in a package
 * that matches the module namespace.  If this is set to "false", the classes that do not match the module namespace will
 * be subpackaged by the client namespace.  <i>NOTE: You may not like this because the package mapping might be ugly.</i>  For example,
 * if your module namespace is "com.mycompany" and you have a class "org.othercompany.OtherClass", it will be mapped to a client-side GWT class
 * named "com.mycompany.client.org.othercompany.OtherClass".</li>
 * <li>The "clientJarName" attribute specifies the name of the client-side jar file that is to be created.
 * If no jar name is specified, the name will be calculated from the enunciate label, or a default will
 * be supplied.</li>
 * <li>The "clientJarDownloadable" attribute specifies whether the GWT client-side jar should be included as a
 * download.  Default: <code>false</code>.</li>
 * <li>The "gwtHome" attribute specifies the filesystem path to the Google Web Toolkit home directory.</li>
 * <li>The "gwtCompilerClass" attribute specifies the FQN of the GWTCompiler.  Default: "com.google.gwt.dev.GWTCompiler".</li>
 * </ul>
 *
 * <h3>The "app" element</h3>
 *
 * <p>The GWT module supports the development of GWT AJAX apps.  Each app is comprised of a set of GWT modules that will be compiled into JavaScript.
 * The "app" element supports the folowing attributes:</p>
 *
 * <ul>
 * <li>The "name" attribute is the name of the GWT app.  Each app will be deployed into a subdirectory that matches its name.  By default,
 * the name of the application is the empty string ("").  This means that the application will be deployed into the root directory.</li>
 * <li>The "srcDir" attribute specifies the source directory for the application. This attribute is required.</li>
 * <li>The "javascriptStyle" attribute specified the JavaScript style that is to be applied by the GWTCompiler.  Valid values are "OBF", "PRETTY",
 * and "DETAILED". The default value is "OBF".</li>
 * </ul>
 *
 * <p>Each "app" element may contain an arbitrary number of "module" child elements that specify the modules that are included in the app.
 * The "module" element supports the following attributes:</p>
 *
 * <ul>
 * <li>The "name" attribute specifies the name of the module. This is usually of the form "com.mycompany.MyModule" and it always has a corresponding
 * ".gwt.xml" module file.</li>
 * <li>The "outputDir" attribute specifies where the compiled module will be placed, relative to the application directory.  By default, the
 * outputDir is the empty string (""), which means the compiled module will be placed at the root of the application directory.</li>
 * </ul>
 *
 * <h3>The "gwtCompileJVMArg" element</h3>
 *
 * <p>The "gwtCompileJVMArg" element is used to specify additional JVM parameters that will be used when invoking GWTCompile.  It supports a single
 * "value" attribute.</p>
 *
 * <h3>Example Configuration</h3>
 *
 * <p>As an example, consider the following configuration:</p>
 *
 * <code class="console">
 * &lt;enunciate&gt;
 * &nbsp;&nbsp;&lt;modules&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;gwt disabled="false"
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;rpcModuleName="com.mycompany.MyGWTRPCModule"
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;gwtHome="/home/myusername/tools/gwt-linux-1.4.60"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;app srcDir="src/main/mainapp"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;module name="com.mycompany.apps.main.MyRootModule"/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;module name="com.mycompany.apps.main.MyModuleTwo" outputPath="two"/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/app&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;app srcDir="src/main/anotherapp" name="another"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;module name="com.mycompany.apps.another.AnotherRootModule"/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;module name="com.mycompany.apps.another.MyModuleThree" outputPath="three"/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/app&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/gwt&gt;
 * &nbsp;&nbsp;&lt;/modules&gt;
 * &lt;/enunciate&gt;
 * </code>
 *
 * <p>The configuration enables the GWT Enunciate module and will publish the web service endpoints under the module name
 * "com.mycompany.MyGWTRPCModule".</p>
 *
 * <p>There are also two GWT applications defined. The first is located at "src/main/mainapp". Since there is
 * no "name" applied to this application, it will be generated into the root of the applications directory.  This
 * first application has two GWT modules defined, the first named "com.mycompany.apps.main.MyRootModule" and the second
 * named "com.mycompany.apps.main.MyModuleTwo".  "MyRootModule", since it has to output path defined, will be generated
 * into the root of its application directory (which is the root of the main applications directory).  "MyModuleTwo", however,
 * will be generated into the subdirectory "two".</p>
 *
 * <p>The second application, rooted at "src/main/anotherapp", is named "another", so it will be generated into the "another"
 * subdirectory of the main applications directory.  It also has two modules, one named "com.mycompany.apps.another.AnotherRootModule",
 * and another named "com.mycompany.apps.another.MyModuleThree".  "AnotherRootModule" will be generated into the root of its application
 * directory ("another") and "MyModuleThree" will be generated into "another/three".</p>
 *
 * <p>All modules are defined by their associated ".gwt.xml" module definition files.  After the "compile" step of the GWT module, the
 * main applications directory will look like this:</p>
 *
 * <code class="console">
 * |--[output of com.mycompany.apps.main.MyRootModule]
 * |--two
 * |----[output of com.mycompany.apps.main.MyModuleTwo]
 * |--another
 * |----[output of com.mycompany.apps.another.AnotherRootModule]
 * |----three
 * |------[output of com.mycompany.apps.another.MyModuleThree]
 * </code>
 *
 * <p>For a less contrived example, see the "petclinic" sample Enunciate project bundled with the Enunciate distribution.</p>
 *
 * <h1><a name="artifacts">Artifacts</a></h1>
 *
 * <ul>
 * <li>The "gwt.client.jar" artifact is the packaged client-side GWT jar.</li>
 * <li>The "gwt.client.src.dir" artifact is the directory where the client-side source code is generated.</li>
 * <li>The "gwt.server.src.dir" artifact is the directory where the server-side source code is generated.</li>
 * <li>The "gwt.app.dir" artifact is the directory to which the GWT AJAX apps are compiled.</li>
 * </ul>
 *
 * @author Ryan Heaton
 */
public class AMFDeploymentModule extends FreemarkerDeploymentModule {

  private final List<FlexApp> flexApps = new ArrayList<FlexApp>();
  private final AMFRuleSet configurationRules = new AMFRuleSet();

  private String flexSDKHome = System.getProperty("flex.home") == null ? System.getenv("FLEX_HOME") : System.getProperty("flex.home");
  private FlexCompilerConfig compilerConfig = new FlexCompilerConfig();
  private String swcName;
  private boolean swcDownloadable = false;

  public AMFDeploymentModule() {
    setDisabled(true);//disable the AMF module by default because it adds unnecessary contraints on the API.
  }

  /**
   * @return "amf"
   */
  @Override
  public String getName() {
    return "amf";
  }

  @Override
  public void init(Enunciate enunciate) throws EnunciateException {
    super.init(enunciate);

    for (FlexApp flexApp : flexApps) {
      if (flexApp.getName() == null) {
        throw new EnunciateException("A flex app must have a name.");
      }

      String srcPath = flexApp.getSrcDir();
      if (srcPath == null) {
        throw new EnunciateException("A source directory for the flex app '" + flexApp.getName() + "' must be supplied with the 'srcDir' attribute.");
      }

      File srcDir = enunciate.resolvePath(srcPath);
      if (!srcDir.exists()) {
        throw new EnunciateException("Source directory for the flex app '" + flexApp.getName() + "' doesn't exist.");
      }
    }
  }

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException, EnunciateException {
    //load the references to the templates....
    URL amfEndpointTemplate = getTemplateURL("amf-endpoint.fmt");
    URL amfTypeTemplate = getTemplateURL("amf-type.fmt");
    URL amfTypeMapperTemplate = getTemplateURL("amf-type-mapper.fmt");

    EnunciateFreemarkerModel model = getModel();
    model.setFileOutputDirectory(getServerSideGenerateDir());

    HashMap<String, String> amfTypePackageConversions = new HashMap<String, String>();
    for (SchemaInfo schemaInfo : model.getNamespacesToSchemas().values()) {
      for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
        if (!isAMFTransient(typeDefinition)) {
          amfTypePackageConversions.put(typeDefinition.getPackage().getQualifiedName(), typeDefinition.getPackage().getQualifiedName() + ".amf");
        }
      }
    }

    info("Generating the AMF externalizable types and their associated mappers...");
    model.put("classnameFor", new AMFClassnameForMethod(amfTypePackageConversions));
    for (SchemaInfo schemaInfo : model.getNamespacesToSchemas().values()) {
      for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
        if (!isAMFTransient(typeDefinition)) {
          model.put("type", typeDefinition);
          processTemplate(amfTypeTemplate, model);
          processTemplate(amfTypeMapperTemplate, model);
        }
      }
    }

    info("Generating the AMF endpoint beans...");
    for (WsdlInfo wsdlInfo : model.getNamespacesToWSDLs().values()) {
      for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
        if (!isAMFTransient(ei)) {
          model.put("endpointInterface", ei);
          processTemplate(amfEndpointTemplate, model);
        }
      }
    }

    URL endpointTemplate = getTemplateURL("as3-endpoint.fmt");
    URL typeTemplate = getTemplateURL("as3-type.fmt");
    URL enumTypeTemplate = getTemplateURL("as3-enum-type.fmt");

    model.setFileOutputDirectory(getClientSideGenerateDir());
    HashMap<String, String> conversions = new HashMap<String, String>();
    //todo: accept client-side package mappings?
    model.put("packageFor", new ClientPackageForMethod(conversions));
    UnqualifiedClassnameForMethod classnameFor = new UnqualifiedClassnameForMethod(conversions);
    model.put("classnameFor", classnameFor);
    model.put("forEachAMFImport", new ForEachAMFImportTransform(null, classnameFor));

    info("Generating the ActionScript types...");
    for (SchemaInfo schemaInfo : model.getNamespacesToSchemas().values()) {
      for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
        if (!isAMFTransient(typeDefinition)) {
          model.put("type", typeDefinition);
          URL template = typeDefinition.isEnum() ? enumTypeTemplate : typeTemplate;
          processTemplate(template, model);
        }
      }
    }

    for (WsdlInfo wsdlInfo : model.getNamespacesToWSDLs().values()) {
      for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
        if (!isAMFTransient(ei)) {
          model.put("endpointInterface", ei);
          processTemplate(endpointTemplate, model);
        }
      }
    }

    URL servicesConfigTemplate = getTemplateURL("services-config-xml.fmt");

    model.setFileOutputDirectory(getXMLGenerateDir());
    info("Generating the configuration files.");
    processTemplate(servicesConfigTemplate, model);

    enunciate.setProperty("amf.xml.dir", getXMLGenerateDir());
    enunciate.setProperty("amf.client.src.dir", getClientSideGenerateDir());
    enunciate.addArtifact(new FileArtifact(getName(), "amf.client.src.dir", getClientSideGenerateDir()));
    enunciate.setProperty("amf.server.src.dir", getServerSideGenerateDir());
    enunciate.addArtifact(new FileArtifact(getName(), "amf.server.src.dir", getServerSideGenerateDir()));
  }

  /**
   * Invokes the flex compiler on the apps specified in the configuration file.
   */
  protected void doFlexCompile() throws EnunciateException, IOException {
    if (this.flexSDKHome == null) {
      throw new EnunciateException("To compile a flex app you must specify the Flex SDK home directory, either in configuration, by setting the FLEX_HOME environment variable, or setting the 'flex.home' system property.");
    }

    File flexHomeDir = new File(this.flexSDKHome);
    if (!flexHomeDir.exists()) {
      throw new EnunciateException("Flex home not found ('" + flexHomeDir.getAbsolutePath() + "').");
    }

    Enunciate enunciate = getEnunciate();

    File javaBinDir = new File(System.getProperty("java.home"), "bin");
    File javaExecutable = new File(javaBinDir, "java");
    if (!javaExecutable.exists()) {
      //append the "exe" for windows users.
      javaExecutable = new File(javaBinDir, "java.exe");
    }

    String javaCommand = javaExecutable.getAbsolutePath();
    if (!javaExecutable.exists()) {
      warn("No java executable found in %s.  We'll just hope the environment is set up to execute 'java'...", javaBinDir.getAbsolutePath());
      javaCommand = "java";
    }

    int compileCommandIndex;
    int outputFileIndex;
    int sourcePathIndex;
    int mainMxmlPathIndex;
    List<String> commandLine = new ArrayList<String>();
    int argIndex = 0;
    commandLine.add(argIndex++, javaCommand);
    for (String jvmarg : this.compilerConfig.getJVMArgs()) {
      commandLine.add(argIndex++, jvmarg);
    }
    commandLine.add(argIndex++, "-cp");
    File flexHomeLib = new File(flexHomeDir, "lib");
    if (!flexHomeLib.exists()) {
      throw new EnunciateException("File not found: " + flexHomeLib);
    }
    else {
      StringBuilder builder = new StringBuilder();
      Iterator<File> flexLibIt = Arrays.asList(flexHomeLib.listFiles()).iterator();
      while (flexLibIt.hasNext()) {
        File flexJar = flexLibIt.next();
        if (flexJar.getAbsolutePath().endsWith("jar")) {
          builder.append(flexJar.getAbsolutePath());
          if (flexLibIt.hasNext()) {
            builder.append(File.pathSeparatorChar);
          }
        }
        else {
          debug("File %s will not be included on the classpath because it's not a jar.", flexJar);
        }
      }
      commandLine.add(argIndex++, builder.toString());
    }

    compileCommandIndex = argIndex;
    commandLine.add(argIndex++, null);

    commandLine.add(argIndex++, "-output");
    outputFileIndex = argIndex;
    commandLine.add(argIndex++, null);

    if (compilerConfig.getFlexConfig() == null) {
      compilerConfig.setFlexConfig(new File(new File(flexSDKHome, "frameworks"), "flex-config.xml"));
    }

    if (compilerConfig.getFlexConfig().exists()) {
      commandLine.add(argIndex++, "-load-config");
      commandLine.add(argIndex++, compilerConfig.getFlexConfig().getAbsolutePath());
    }
    else {
      warn("Configured flex configuration file %s doesn't exist.  Ignoring...", compilerConfig.getFlexConfig());
    }

    if (compilerConfig.getContextRoot() == null) {
      if (getEnunciate().getConfig().getLabel() != null) {
        compilerConfig.setContextRoot("/" + getEnunciate().getConfig().getLabel());
      }
      else {
        compilerConfig.setContextRoot("/enunciate");
      }
    }

    commandLine.add(argIndex++, "-compiler.context-root");
    commandLine.add(argIndex++, compilerConfig.getContextRoot());

    if (compilerConfig.getLocale() != null) {
      commandLine.add(argIndex++, "-compiler.locale");
      commandLine.add(argIndex++, compilerConfig.getLocale());
    }

    if (compilerConfig.getLicenses().size() > 0) {
      commandLine.add(argIndex++, "-licenses.license");
      for (License license : compilerConfig.getLicenses()) {
        commandLine.add(argIndex++, license.getProduct());
        commandLine.add(argIndex++, license.getSerialNumber());
      }
    }

    if (compilerConfig.getOptimize() != null && compilerConfig.getOptimize()) {
      commandLine.add(argIndex++, "-compiler.optimize");
    }

    if (compilerConfig.getDebug() != null && compilerConfig.getDebug()) {
      commandLine.add(argIndex++, "-compiler.debug=true");
    }

    if (compilerConfig.getProfile() != null && compilerConfig.getProfile()) {
      commandLine.add(argIndex++, "-compiler.profile");
    }

    if (compilerConfig.getStrict() != null && compilerConfig.getStrict()) {
      commandLine.add(argIndex++, "-compiler.strict");
    }

    if (compilerConfig.getUseNetwork() != null && compilerConfig.getUseNetwork()) {
      commandLine.add(argIndex++, "-use-network");
    }

    if (compilerConfig.getWarnings() != null && compilerConfig.getWarnings()) {
      commandLine.add(argIndex++, "-warnings");
    }

    if (compilerConfig.getIncremental() != null && compilerConfig.getIncremental()) {
      commandLine.add(argIndex++, "-compiler.incremental");
    }

    if (compilerConfig.getShowActionscriptWarnings() != null && compilerConfig.getShowActionscriptWarnings()) {
      commandLine.add(argIndex++, "-show-actionscript-warnings");
    }

    if (compilerConfig.getShowBindingWarnings() != null && compilerConfig.getShowBindingWarnings()) {
      commandLine.add(argIndex++, "-show-binding-warnings");
    }

    if (compilerConfig.getShowDeprecationWarnings() != null && compilerConfig.getShowDeprecationWarnings()) {
      commandLine.add(argIndex++, "-show-deprecation-warnings");
    }

    commandLine.add(argIndex++, "-compiler.services");
    commandLine.add(argIndex++, new File(getXMLGenerateDir(), "services-config.xml").getAbsolutePath());

    commandLine.add(argIndex++, "-source-path");
    commandLine.add(argIndex++, getClientSideGenerateDir().getAbsolutePath());

    String swcName = getSwcName();

    if (swcName == null) {
      String label = "enunciate";
      if ((enunciate.getConfig() != null) && (enunciate.getConfig().getLabel() != null)) {
        label = enunciate.getConfig().getLabel();
      }

      swcName = label + "-as3-client.swc";
    }

    File as3Bundle = new File(getSwcCompileDir(), swcName);
    commandLine.set(compileCommandIndex, compilerConfig.getSwcCompileCommand());
    commandLine.set(outputFileIndex, as3Bundle.getAbsolutePath());
    info("Compiling %s for the client-side ActionScript classes...", as3Bundle.getAbsolutePath());
    if (enunciate.isDebug()) {
      StringBuilder command = new StringBuilder();
      for (String commandPiece : commandLine) {
        command.append(' ').append(commandPiece);
      }
      debug("Executing SWC compile for client-side actionscript with the command: %s", command);
    }

    enunciate.setProperty("as3.client.swc", as3Bundle);

    List<ArtifactDependency> clientDeps = new ArrayList<ArtifactDependency>();
    BaseArtifactDependency as3Dependency = new BaseArtifactDependency();
    as3Dependency.setId("flex-sdk");
    as3Dependency.setArtifactType("zip");
    as3Dependency.setDescription("The flex SDK.");
    as3Dependency.setURL("http://www.adobe.com/products/flex/");
    as3Dependency.setVersion("2.0.1");
    clientDeps.add(as3Dependency);

    ClientLibraryArtifact as3ClientArtifact = new ClientLibraryArtifact(getName(), "as3.client.library", "ActionScript 3 Client SWC");
    as3ClientArtifact.setPlatform("Adobe Flex");
    //read in the description from file:
    as3ClientArtifact.setDescription(readResource("client_library_description.html"));
    NamedFileArtifact clientArtifact = new NamedFileArtifact(getName(), "as3.client.swc", as3Bundle);
    clientArtifact.setDescription("The ActionScript source files.");
    clientArtifact.setPublic(isSwcDownloadable());
    as3ClientArtifact.addArtifact(clientArtifact);
    as3ClientArtifact.setDependencies(clientDeps);
    enunciate.addArtifact(clientArtifact);
    if (isSwcDownloadable()) {
      enunciate.addArtifact(as3ClientArtifact);
    }

    commandLine.add(argIndex++, "-source-path");
    sourcePathIndex = argIndex;
    commandLine.add(argIndex++, null);

    commandLine.add(argIndex++, "--");
    mainMxmlPathIndex = argIndex;
    commandLine.add(argIndex++, null);

    commandLine.set(compileCommandIndex, compilerConfig.getFlexCompileCommand());

    File outputDirectory = getSwfCompileDir();
    debug("Creating output directory: " + outputDirectory);
    outputDirectory.mkdirs();

    for (FlexApp flexApp : flexApps) {
      String mainMxmlPath = flexApp.getMainMxmlFile();
      if (mainMxmlPath == null) {
        throw new EnunciateException("A main MXML file for the flex app '" + flexApp.getName() + "' must be supplied with the 'mainMxmlFile' attribute.");
      }

      File mainMxmlFile = enunciate.resolvePath(mainMxmlPath);
      if (!mainMxmlFile.exists()) {
        throw new EnunciateException("Main MXML file for the flex app '" + flexApp.getName() + "' doesn't exist.");
      }

      String swfFile = new File(outputDirectory, flexApp.getName() + ".swf").getAbsolutePath();
      commandLine.set(outputFileIndex, swfFile);
      commandLine.set(sourcePathIndex, enunciate.resolvePath(flexApp.getSrcDir()).getAbsolutePath());
      commandLine.set(mainMxmlPathIndex, enunciate.resolvePath(flexApp.getMainMxmlFile()).getAbsolutePath());

      info("Compiling %s ...", swfFile);
      if (enunciate.isDebug()) {
        StringBuilder command = new StringBuilder();
        for (String commandPiece : commandLine) {
          command.append(' ').append(commandPiece);
        }
        debug("Executing flex compile for module %s with the command: %s", flexApp.getName(), command);
      }

      ProcessBuilder processBuilder = new ProcessBuilder(commandLine.toArray(new String[commandLine.size()]));
      processBuilder.directory(getSwfCompileDir());
      processBuilder.redirectErrorStream(true);
      Process process = processBuilder.start();
      BufferedReader procReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line = procReader.readLine();
      while (line != null) {
        info(line);
        line = procReader.readLine();
      }
      int procCode;
      try {
        procCode = process.waitFor();
      }
      catch (InterruptedException e1) {
        throw new EnunciateException("Unexpected inturruption of the Flex compile process.");
      }

      if (procCode != 0) {
        throw new EnunciateException("Flex compile failed for module " + flexApp.getName());
      }
    }
  }

  @Override
  protected void doCompile() throws EnunciateException, IOException {
    Enunciate enunciate = getEnunciate();

    if (this.flexApps.size() > 0) {
      doFlexCompile();
      enunciate.setProperty("flex.app.dir", getSwfCompileDir());
      enunciate.addArtifact(new FileArtifact(getName(), "flex.app.dir", getSwfCompileDir()));
    }
  }

  /**
   * Reads a resource into string form.
   *
   * @param resource The resource to read.
   * @return The string form of the resource.
   */
  protected String readResource(String resource) throws IOException {
    InputStream resourceIn = AMFDeploymentModule.class.getResourceAsStream(resource);
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
   * Whether the given type declaration is GWT-transient.
   *
   * @param declaration The type declaration.
   * @return Whether the given tyep declaration is GWT-transient.
   */
  protected boolean isAMFTransient(TypeDeclaration declaration) {
    return isAMFTransient((Declaration) declaration) || isAMFTransient(declaration.getPackage());
  }

  /**
   * Whether the given type declaration is GWT-transient.
   *
   * @param declaration The type declaration.
   * @return Whether the given tyep declaration is GWT-transient.
   */
  protected boolean isAMFTransient(Declaration declaration) {
    return declaration != null && declaration.getAnnotation(AMFTransient.class) != null;
  }

  /**
   * Get a template URL for the template of the given name.
   *
   * @param template The specified template.
   * @return The URL to the specified template.
   */
  protected URL getTemplateURL(String template) {
    return AMFDeploymentModule.class.getResource(template);
  }

  /**
   * Get the generate directory for server-side GWT classes.
   *
   * @return The generate directory for server-side GWT classes.
   */
  public File getServerSideGenerateDir() {
    return new File(getGenerateDir(), "server");
  }

  /**
   * Get the generate directory for client-side GWT classes.
   *
   * @return The generate directory for client-side GWT classes.
   */
  public File getClientSideGenerateDir() {
    return new File(getGenerateDir(), "client");
  }

  /**
   * Get the generate directory for XML configuration.
   *
   * @return The generate directory for the XML configuration.
   */
  public File getXMLGenerateDir() {
    return new File(getGenerateDir(), "xml");
  }

  /**
   * The directory for the destination for the SWC.
   *
   * @return The directory for the destination for the SWC.
   */
  public File getSwcCompileDir() {
    return new File(getCompileDir(), "swc");
  }

  /**
   * The directory for the destination for the SWF.
   *
   * @return The directory for the destination for the SWF.
   */
  public File getSwfCompileDir() {
    return new File(getCompileDir(), "swf");
  }

  /**
   * AMF configuration rule set.
   *
   * @return AMF configuration rule set.
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
    return new AMFValidator();
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
   * The gwt home directory
   *
   * @return The gwt home directory
   */
  public String getFlexSDKHome() {
    return flexSDKHome;
  }

  /**
   * Set the path to the GWT home directory.
   *
   * @param flexSDKHome The gwt home directory
   */
  public void setFlexSDKHome(String flexSDKHome) {
    this.flexSDKHome = flexSDKHome;
  }

  /**
   * The gwt apps to compile.
   *
   * @return The gwt apps to compile.
   */
  public List<FlexApp> getFlexApps() {
    return flexApps;
  }

  /**
   * Adds a flex app to be compiled.
   *
   * @param flexApp The flex app to be compiled.
   */
  public void addFlexApp(FlexApp flexApp) {
    this.flexApps.add(flexApp);
  }

  /**
   * The compiler configuration.
   *
   * @return The compiler configuration.
   */
  public FlexCompilerConfig getCompilerConfig() {
    return compilerConfig;
  }

  /**
   * The compiler configuration.
   *
   * @param compilerConfig The compiler configuration.
   */
  public void setCompilerConfig(FlexCompilerConfig compilerConfig) {
    this.compilerConfig = compilerConfig;
  }

  /**
   * The name of the swc file.
   *
   * @return The name of the swc file.
   */
  public String getSwcName() {
    return swcName;
  }

  /**
   * The name of the swc file.
   *
   * @param swcName The name of the swc file.
   */
  public void setSwcName(String swcName) {
    this.swcName = swcName;
  }

  /**
   * Whether the swc is downloadable.
   *
   * @return Whether the swc is downloadable.
   */
  public boolean isSwcDownloadable() {
    return swcDownloadable;
  }

  /**
   * Whether the swc is downloadable.
   *
   * @param swcDownloadable Whether the swc is downloadable.
   */
  public void setSwcDownloadable(boolean swcDownloadable) {
    this.swcDownloadable = swcDownloadable;
  }
}
