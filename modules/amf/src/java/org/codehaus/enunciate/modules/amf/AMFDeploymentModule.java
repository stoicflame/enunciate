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
import org.codehaus.enunciate.template.freemarker.ClientPackageForMethod;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.config.SchemaInfo;
import org.codehaus.enunciate.contract.jaxb.TypeDefinition;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.main.*;
import org.codehaus.enunciate.modules.FreemarkerDeploymentModule;
import org.codehaus.enunciate.modules.amf.config.AMFApp;
import org.codehaus.enunciate.modules.amf.config.AMFRuleSet;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

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

  private final List<AMFApp> flexApps = new ArrayList<AMFApp>();
  private final AMFRuleSet configurationRules = new AMFRuleSet();
  private final List<String> flexArgs = new ArrayList<String>();
  private String flexSDKHome = System.getProperty("flex.home") == null ? System.getenv("FLEX_HOME") : System.getProperty("flex.home");
  private String flexCompileCommand = "com.google.gwt.dev.GWTCompiler";
  private String actionscriptBundleName = null;
  private boolean actionscriptBundleDownloadable = true;

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

    for (AMFApp amfApp : flexApps) {
      String srcPath = amfApp.getSrcDir();

      if (srcPath == null) {
        throw new EnunciateException("A source directory for the AMF app "
          + ("".equals(amfApp.getName()) ? "" : "'" + amfApp.getName() + "' ")
          + "must be supplied with the 'srcDir' attribute.");
      }

      File srcDir = enunciate.resolvePath(srcPath);
      if (!srcDir.exists()) {
        throw new EnunciateException("Source directory '" + srcDir.getAbsolutePath() + "' doesn't exist for the AMF app"
          + ("".equals(amfApp.getName()) ? "." : " '" + amfApp.getName() + "'."));
      }
    }
  }

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException, EnunciateException {
    //load the references to the templates....
    URL externalizerTemplate = getTemplateURL("amf-type-externalizer.fmt");
    URL graniteConfigTemplate = getTemplateURL("granite-config-xml.fmt");
    URL servicesConfigTemplate = getTemplateURL("services-config-xml.fmt");

    URL typeTemplate = getTemplateURL("as3-type.fmt");
    URL enumTypeTemplate = getTemplateURL("as3-enum-type.fmt");

    EnunciateFreemarkerModel model = getModel();
    model.setFileOutputDirectory(getClientSideGenerateDir());
    HashMap<String, String> conversions = new HashMap<String, String>();
    //todo: accept client-side package mappings?
    model.put("packageFor", new ClientPackageForMethod(conversions));
    model.put("classnameFor", new ClientClassnameForMethod(conversions));

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

    model.setFileOutputDirectory(getServerSideGenerateDir());

    info("Generating the AMF externalizers...");
    for (SchemaInfo schemaInfo : model.getNamespacesToSchemas().values()) {
      for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
        if ((!typeDefinition.isEnum()) && (!isAMFTransient(typeDefinition))) {
          model.put("type", typeDefinition);
          processTemplate(externalizerTemplate, model);
        }
      }
    }

    model.setFileOutputDirectory(getXMLGenerateDir());
    info("Generating the configuration files.");
    processTemplate(graniteConfigTemplate, model);
    processTemplate(servicesConfigTemplate, model);

    enunciate.setProperty("amf.xml.dir", getXMLGenerateDir());
    enunciate.setProperty("amf.client.src.dir", getClientSideGenerateDir());
    enunciate.addArtifact(new FileArtifact(getName(), "amf.client.src.dir", getClientSideGenerateDir()));
    enunciate.setProperty("amf.server.src.dir", getServerSideGenerateDir());
    enunciate.addArtifact(new FileArtifact(getName(), "amf.server.src.dir", getServerSideGenerateDir()));
  }

  /**
   * Invokes GWTCompile on the apps specified in the configuration file.
   */
  protected void doAMFCompile() throws EnunciateException, IOException {
    if (this.flexSDKHome == null) {
      throw new EnunciateException("To compile a flex app you must specify the GWT home directory, either in configuration, by setting the FLEX_HOME environment variable, or setting the 'flex.home' system property.");
    }

    File flexHomeDir = new File(this.flexSDKHome);
    if (!flexHomeDir.exists()) {
      throw new EnunciateException("Flex home not found ('" + flexHomeDir.getAbsolutePath() + "').");
    }

//    File gwtUserJar = new File(flexHomeDir, "gwt-user.jar");
//    if (!gwtUserJar.exists()) {
//      warn("Unable to find %s. There may be flex compile errors.", gwtUserJar.getAbsolutePath());
//    }
//
//    //now we have to find gwt-dev.jar.
//    //start by assuming linux...
//    File linuxDevJar = new File(flexHomeDir, "gwt-dev-linux.jar");
//    File gwtDevJar = linuxDevJar;
//    if (!gwtDevJar.exists()) {
//      //linux not found. try mac...
//      File macDevJar = new File(flexHomeDir, "gwt-dev-mac.jar");
//      gwtDevJar = macDevJar;
//
//      if (!gwtDevJar.exists()) {
//        //okay, we'll try windows if we have to...
//        File windowsDevJar = new File(flexHomeDir, "gwt-dev-windows.jar");
//        gwtDevJar = windowsDevJar;
//
//        if (!gwtDevJar.exists()) {
//          throw new EnunciateException(String.format("Unable to find GWT dev jar. Looked for %s, %s, and %s.", linuxDevJar.getAbsolutePath(), macDevJar.getAbsolutePath(), windowsDevJar.getAbsolutePath()));
//        }
//      }
//    }
//
//    File javaBinDir = new File(System.getProperty("java.home"), "bin");
//    File javaExecutable = new File(javaBinDir, "java");
//    if (!javaExecutable.exists()) {
//      //append the "exe" for windows users.
//      javaExecutable = new File(javaBinDir, "java.exe");
//    }
//
//    String javaCommand = javaExecutable.getAbsolutePath();
//    if (!javaExecutable.exists()) {
//      warn("No java executable found in %s.  We'll just hope the environment is set up to execute 'java'...", javaBinDir.getAbsolutePath());
//      javaCommand = "java";
//    }
//
//    StringBuilder classpath = new StringBuilder(enunciate.getEnunciateClasspath());
//    //append the client-side gwt directory.
//    classpath.append(File.pathSeparatorChar).append(getClientSideGenerateDir().getAbsolutePath());
//    //append the gwt-user jar.
//    classpath.append(File.pathSeparatorChar).append(gwtUserJar.getAbsolutePath());
//    //append the gwt-dev jar.
//    classpath.append(File.pathSeparatorChar).append(gwtDevJar.getAbsolutePath());
//
//    //so here's the command:
//    //java [extra jvm args] -cp [classpath] [compilerClass] -gen [gwt-gen-dir] -style [style] -out [out] [moduleName]
//    List<String> jvmargs = getFlexArgs();
//    String[] commandArray = new String[jvmargs.size() + 11];
//    int argIndex = 0;
//    commandArray[argIndex++] = javaCommand;
//    while (argIndex - 1 < jvmargs.size()) {
//      String arg = jvmargs.get(argIndex - 1);
//      commandArray[argIndex++] = arg;
//    }
//    commandArray[argIndex++] = "-cp";
//    int classpathArgIndex = argIndex; //app-specific arg.
//    commandArray[argIndex++] = null;
//    commandArray[argIndex++] = getFlexCompileCommand();
//    commandArray[argIndex++] = "-gen";
//    commandArray[argIndex++] = getGwtGenDir().getAbsolutePath();
//    commandArray[argIndex++] = "-style";
//    int styleArgIndex = argIndex;
//    commandArray[argIndex++] = null; //app-specific arg.
//    commandArray[argIndex++] = "-out";
//    int outArgIndex = argIndex;
//    commandArray[argIndex++] = null; //app-specific arg.
//    int moduleNameIndex = argIndex;
//    commandArray[argIndex] = null; //module-specific arg.
//
//    for (GWTApp gwtApp : amfApps) {
//      String appName = gwtApp.getName();
//      File appSource = enunciate.resolvePath(gwtApp.getSrcDir());
//      commandArray[classpathArgIndex] = classpath.toString() + File.pathSeparatorChar + appSource.getAbsolutePath();
//      String style = gwtApp.getJavascriptStyle().toString();
//      commandArray[styleArgIndex] = style;
//      File appDir = getAppGenerateDir(appName);
//      String out = appDir.getAbsolutePath();
//      commandArray[outArgIndex] = out;
//
//      for (GWTAppModule appModule : gwtApp.getModules()) {
//        String moduleName = appModule.getName();
//        commandArray[moduleNameIndex] = moduleName;
//        info("Executing GWTCompile for module '%s'...", moduleName);
//        if (enunciate.isDebug()) {
//          StringBuilder command = new StringBuilder();
//          for (String commandPiece : commandArray) {
//            command.append(' ').append(commandPiece);
//          }
//          debug("Executing GWTCompile for module %s with the command: %s", moduleName, command);
//        }
//        ProcessBuilder processBuilder = new ProcessBuilder(commandArray);
//        processBuilder.directory(getGenerateDir());
//        processBuilder.redirectErrorStream(true);
//        Process process = processBuilder.start();
//        BufferedReader procReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//        String line = procReader.readLine();
//        while (line != null) {
//          info(line);
//          line = procReader.readLine();
//        }
//        int procCode;
//        try {
//          procCode = process.waitFor();
//        }
//        catch (InterruptedException e1) {
//          throw new EnunciateException("Unexpected inturruption of the GWT compile process.");
//        }
//
//        if (procCode != 0) {
//          throw new EnunciateException("GWT compile failed for module " + moduleName);
//        }
//
//        String outputPath = appModule.getOutputPath();
//        File moduleOutputDir = appDir;
//        if ((outputPath != null) && (!"".equals(outputPath.trim()))) {
//          moduleOutputDir = new File(appDir, outputPath);
//        }
//        File moduleGenDir = new File(appDir, moduleName);
//        if (!moduleOutputDir.equals(moduleGenDir)) {
//          moduleOutputDir.mkdirs();
//          enunciate.copyDir(moduleGenDir, moduleOutputDir);
//          deleteDir(moduleGenDir);
//        }
//      }
//    }
  }

//  /**
//   * Delete a directory on the filesystem.
//   *
//   * @param dir The directory to delete.
//   * @return Whether the directory was successfully deleted.
//   */
//  private boolean deleteDir(File dir) {
//    if (dir.exists()) {
//      File[] files = dir.listFiles();
//      for (File file : files) {
//        if (file.isDirectory()) {
//          deleteDir(file);
//        }
//        else {
//          file.delete();
//        }
//      }
//    }
//    return dir.delete();
//  }

  @Override
  protected void doCompile() throws EnunciateException, IOException {
    Enunciate enunciate = getEnunciate();

    if (this.flexApps.size() > 0) {
      doAMFCompile();
      enunciate.setProperty("flex.app.dir", getAppGenerateDir());
      enunciate.addArtifact(new FileArtifact(getName(), "flex.app.dir", getAppGenerateDir()));
    }
  }

  @Override
  protected void doBuild() throws EnunciateException, IOException {
    Enunciate enunciate = getEnunciate();
    String as3BundleName = getActionscriptBundleName();

    if (as3BundleName == null) {
      String label = "enunciate";
      if ((enunciate.getConfig() != null) && (enunciate.getConfig().getLabel() != null)) {
        label = enunciate.getConfig().getLabel();
      }

      as3BundleName = label + "-actionscript-client.jar";
    }

    File as3Bundle = new File(getBuildDir(), as3BundleName);
    enunciate.zip(as3Bundle, getClientSideGenerateDir());
    enunciate.setProperty("as3.client.jar", as3Bundle);

    List<ArtifactDependency> clientDeps = new ArrayList<ArtifactDependency>();
    BaseArtifactDependency as3Dependency = new BaseArtifactDependency();
    as3Dependency.setId("flex-sdk");
    as3Dependency.setArtifactType("zip");
    as3Dependency.setDescription("The flex SDK.");
    as3Dependency.setURL("http://www.adobe.com/products/flex/");
    as3Dependency.setVersion("2.0.1");
    clientDeps.add(as3Dependency);

    ClientLibraryArtifact as3ClientArtifact = new ClientLibraryArtifact(getName(), "as3.client.library", "ActionScript 3 Client Bundle");
    as3ClientArtifact.setPlatform("Adobe Flex");
    //read in the description from file:
    as3ClientArtifact.setDescription(readResource("client_library_description.html"));
    NamedFileArtifact clientArtifact = new NamedFileArtifact(getName(), "as3.client.bundle", as3Bundle);
    clientArtifact.setDescription("The ActionScript source files.");
    clientArtifact.setPublic(false);
    as3ClientArtifact.addArtifact(clientArtifact);
    as3ClientArtifact.setDependencies(clientDeps);
    enunciate.addArtifact(clientArtifact);
    if (isActionscriptBundleDownloadable()) {
      enunciate.addArtifact(as3ClientArtifact);
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
    return isAMFTransient(declaration) || isAMFTransient(declaration.getPackage());
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
   * The base generate dir for the gwt applications.
   *
   * @return The base generate dir for the gwt applications.
   */
  public File getAppGenerateDir() {
    return new File(getCompileDir(), "flexapps");
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
   * Extra JVM args for the GWT compile.
   *
   * @return Extra JVM args for the GWT compile.
   */
  public List<String> getFlexArgs() {
    return flexArgs;
  }

  /**
   * Extra JVM args for the GWT compile.
   *
   * @param arg Extra JVM args for the GWT compile.
   */
  public void addFlexCompileArg(String arg) {
    this.flexArgs.add(arg);
  }

  /**
   * The GWT compiler class.
   *
   * @return The GWT compiler class.
   */
  public String getFlexCompileCommand() {
    return flexCompileCommand;
  }

  /**
   * The GWT compiler class.
   *
   * @param flexCompileCommand The GWT compiler class.
   */
  public void setFlexCompileCommand(String flexCompileCommand) {
    this.flexCompileCommand = flexCompileCommand;
  }

  /**
   * The gwt apps to compile.
   *
   * @return The gwt apps to compile.
   */
  public List<AMFApp> getFlexApps() {
    return flexApps;
  }

  /**
   * Adds a gwt app to be compiled.
   *
   * @param gwtApp The gwt app to be compiled.
   */
  public void addGWTApp(AMFApp gwtApp) {
    this.flexApps.add(gwtApp);
  }

  /**
   * The name of the zip bundle for the generated ActionScript classes.
   *
   * @return The name of the zip bundle for the generated ActionScript classes.
   */
  public String getActionscriptBundleName() {
    return actionscriptBundleName;
  }

  /**
   * The name of the zip bundle for the generated ActionScript classes.
   *
   * @param actionscriptBundleName The name of the zip bundle for the generated ActionScript classes.
   */
  public void setActionscriptBundleName(String actionscriptBundleName) {
    this.actionscriptBundleName = actionscriptBundleName;
  }

  /**
   * Whether the actionscript bundle is downloadable.
   *
   * @return Whether the actionscript bundle is downloadable.
   */
  public boolean isActionscriptBundleDownloadable() {
    return actionscriptBundleDownloadable;
  }

  /**
   * Whether the actionscript bundle is downloadable.
   *
   * @param actionscriptBundleDownloadable Whether the actionscript bundle is downloadable.
   */
  public void setActionscriptBundleDownloadable(boolean actionscriptBundleDownloadable) {
    this.actionscriptBundleDownloadable = actionscriptBundleDownloadable;
  }
}
