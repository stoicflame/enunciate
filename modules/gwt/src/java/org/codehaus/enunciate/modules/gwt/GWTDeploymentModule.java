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
import org.codehaus.enunciate.template.freemarker.ClientPackageForMethod;
import org.codehaus.enunciate.template.freemarker.ComponentTypeForMethod;
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
import org.codehaus.enunciate.modules.gwt.config.GWTRuleSet;
import org.codehaus.enunciate.modules.gwt.config.GWTApp;
import org.codehaus.enunciate.modules.gwt.config.GWTAppModule;
import org.codehaus.enunciate.template.freemarker.CollectionTypeForMethod;
import org.codehaus.enunciate.util.ClassDeclarationComparator;

import java.io.*;
import java.net.URL;
import java.util.*;

import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.Declaration;

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
 * <li>The "enforceNoFieldAccessors" attribute specifies whether to enforce that a field accessor cannot be used for GWT mapping.
 * <i>Note: whether this option is enabled or disabled, there currently MUST be a getter and setter for each accessor.  This option only
 * disables the compile-time validation check.</i></li>
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
public class GWTDeploymentModule extends FreemarkerDeploymentModule {

  private boolean enforceNamespaceConformance = true;
  private boolean enforceNoFieldAccessors = true;
  private String rpcModuleNamespace = null;
  private String rpcModuleName = null;
  private String clientJarName = null;
  private boolean clientJarDownloadable = false;
  private final List<GWTApp> gwtApps = new ArrayList<GWTApp>();
  private final GWTRuleSet configurationRules = new GWTRuleSet();
  private String gwtHome = System.getProperty("gwt.home") == null ? System.getenv("GWT_HOME") : System.getProperty("gwt.home");
  private final List<String> gwtCompileJVMArgs = new ArrayList<String>();
  private String gwtCompilerClass = "com.google.gwt.dev.GWTCompiler";

  public GWTDeploymentModule() {
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

    if (rpcModuleName == null) {
      throw new EnunciateException("You must specify a \"gwtModuleName\" for the GWT module.");
    }
    if (rpcModuleNamespace == null) {
      throw new EnunciateException("You must specify a \"gwtModuleName\" for the GWT module.");
    }

    for (GWTApp gwtApp : gwtApps) {
      String srcPath = gwtApp.getSrcDir();

      if (srcPath == null) {
        throw new EnunciateException("A source directory for the GWT app "
          + ("".equals(gwtApp.getName()) ? "" : "'" + gwtApp.getName() + "' ")
          + "must be supplied with the 'srcDir' attribute.");
      }

      File srcDir = enunciate.resolvePath(srcPath);
      if (!srcDir.exists()) {
        throw new EnunciateException("Source directory '" + srcDir.getAbsolutePath() + "' doesn't exist for the GWT app"
          + ("".equals(gwtApp.getName()) ? "." : " '" + gwtApp.getName() + "'."));
      }
    }

  }

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException, EnunciateException {
    //load the references to the templates....
    URL typeMapperTemplate = getTemplateURL("gwt-type-mapper.fmt");
    URL faultMapperTemplate = getTemplateURL("gwt-fault-mapper.fmt");
    URL moduleXmlTemplate = getTemplateURL("gwt-module-xml.fmt");

    URL eiTemplate = getTemplateURL("gwt-endpoint-interface.fmt");
    URL endpointImplTemplate = getTemplateURL("gwt-endpoint-impl.fmt");
    URL faultTemplate = getTemplateURL("gwt-fault.fmt");
    URL typeTemplate = getTemplateURL("gwt-type.fmt");
    URL enumTypeTemplate = getTemplateURL("gwt-enum-type.fmt");

    //set up the model, first allowing for jdk 14 compatability.
    EnunciateFreemarkerModel model = getModel();
    Map<String, String> conversions = new HashMap<String, String>();
    String clientNamespace = this.rpcModuleNamespace + ".client";
    conversions.put(this.rpcModuleNamespace, clientNamespace);
    if (!this.enforceNamespaceConformance) {
      TreeSet<WebFault> allFaults = new TreeSet<WebFault>(new ClassDeclarationComparator());
      for (WsdlInfo wsdlInfo : model.getNamespacesToWSDLs().values()) {
        for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
          if (!isGWTTransient(ei)) {
            String pckg = ei.getPackage().getQualifiedName();
            if (!conversions.containsKey(pckg)) {
              conversions.put(pckg, pckg + "." + clientNamespace);
            }
            for (WebMethod webMethod : ei.getWebMethods()) {
              for (WebFault webFault : webMethod.getWebFaults()) {
                allFaults.add(webFault);
              }
            }
          }
        }
      }
      for (WebFault webFault : allFaults) {
        if (!isGWTTransient(webFault)) {
          String pckg = webFault.getPackage().getQualifiedName();
          if (!conversions.containsKey(pckg)) {
            conversions.put(pckg, pckg + "." + clientNamespace);
          }
        }
      }
      for (SchemaInfo schemaInfo : model.getNamespacesToSchemas().values()) {
        for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
          if (!isGWTTransient(typeDefinition)) {
            String pckg = typeDefinition.getPackage().getQualifiedName();
            if (!conversions.containsKey(pckg)) {
              conversions.put(pckg, pckg + "." + clientNamespace);
            }
          }
        }
      }
    }

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
        if (!isGWTTransient(ei)) {
          model.put("endpointInterface", ei);
          processTemplate(eiTemplate, model);

          for (WebMethod webMethod : ei.getWebMethods()) {
            for (WebFault webFault : webMethod.getWebFaults()) {
              allFaults.add(webFault);
            }
          }
        }
      }
    }

    info("Generating the GWT faults...");
    for (WebFault webFault : allFaults) {
      if (!isGWTTransient(webFault)) {
        model.put("fault", webFault);
        processTemplate(faultTemplate, model);
      }
    }

    info("Generating the GWT types...");
    for (SchemaInfo schemaInfo : model.getNamespacesToSchemas().values()) {
      for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
        if (!isGWTTransient(typeDefinition)) {
          model.put("type", typeDefinition);
          URL template = typeDefinition.isEnum() ? enumTypeTemplate : typeTemplate;
          processTemplate(template, model);
        }
      }
    }

    model.put("gwtModuleName", this.rpcModuleName);
    processTemplate(moduleXmlTemplate, model);

    model.setFileOutputDirectory(getServerSideGenerateDir());

    info("Generating the GWT endpoint implementations...");
    for (WsdlInfo wsdlInfo : model.getNamespacesToWSDLs().values()) {
      for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
        if (!isGWTTransient(ei)) {
          model.put("endpointInterface", ei);
          processTemplate(endpointImplTemplate, model);
        }
      }
    }

    info("Generating the GWT type mappers...");
    for (SchemaInfo schemaInfo : model.getNamespacesToSchemas().values()) {
      for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
        if ((!typeDefinition.isEnum()) && (!isGWTTransient(typeDefinition))) {
          model.put("type", typeDefinition);
          processTemplate(typeMapperTemplate, model);
        }
      }
    }

    info("Generating the GWT fault mappers...");
    for (WebFault webFault : allFaults) {
      if (!isGWTTransient(webFault)) {
        model.put("fault", webFault);
        processTemplate(faultMapperTemplate, model);
      }
    }

    enunciate.setProperty("gwt.client.src.dir", getClientSideGenerateDir());
    enunciate.addArtifact(new FileArtifact(getName(), "gwt.client.src.dir", getClientSideGenerateDir()));
    enunciate.setProperty("gwt.server.src.dir", getServerSideGenerateDir());
    enunciate.addArtifact(new FileArtifact(getName(), "gwt.server.src.dir", getServerSideGenerateDir()));
  }

  /**
   * Invokes GWTCompile on the apps specified in the configuration file.
   */
  protected void doGWTCompile() throws EnunciateException, IOException {
    if (this.gwtHome == null) {
      throw new EnunciateException("To compile a GWT app you must specify the GWT home directory, either in configuration, by setting the GWT_HOME environment variable, or setting the 'gwt.home' system property.");
    }

    File gwtHomeDir = new File(this.gwtHome);
    if (!gwtHomeDir.exists()) {
      throw new EnunciateException("GWT home not found ('" + gwtHomeDir.getAbsolutePath() + "').");
    }

    File gwtUserJar = new File(gwtHomeDir, "gwt-user.jar");
    if (!gwtUserJar.exists()) {
      warn("Unable to find %s. You may be GWT compile errors.", gwtUserJar.getAbsolutePath());
    }

    //now we have to find gwt-dev.jar.
    //start by assuming linux...
    File linuxDevJar = new File(gwtHomeDir, "gwt-dev-linux.jar");
    File gwtDevJar = linuxDevJar;
    if (!gwtDevJar.exists()) {
      //linux not found. try mac...
      File macDevJar = new File(gwtHomeDir, "gwt-dev-mac.jar");
      gwtDevJar = macDevJar;

      if (!gwtDevJar.exists()) {
        //okay, we'll try windows if we have to...
        File windowsDevJar = new File(gwtHomeDir, "gwt-dev-windows.jar");
        gwtDevJar = windowsDevJar;

        if (!gwtDevJar.exists()) {
          throw new EnunciateException(String.format("Unable to find GWT dev jar. Looked for %s, %s, and %s.", linuxDevJar.getAbsolutePath(), macDevJar.getAbsolutePath(), windowsDevJar.getAbsolutePath()));
        }
      }
    }

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

    StringBuilder classpath = new StringBuilder(enunciate.getEnunciateClasspath());
    //append the client-side gwt directory.
    classpath.append(File.pathSeparatorChar).append(getClientSideGenerateDir().getAbsolutePath());
    //append the gwt-user jar.
    classpath.append(File.pathSeparatorChar).append(gwtUserJar.getAbsolutePath());
    //append the gwt-dev jar.
    classpath.append(File.pathSeparatorChar).append(gwtDevJar.getAbsolutePath());

    //so here's the command:
    //java [extra jvm args] -cp [classpath] [compilerClass] -gen [gwt-gen-dir] -style [style] -out [out] [moduleName]
    List<String> jvmargs = getGwtCompileJVMArgs();
    String[] commandArray = new String[jvmargs.size() + 11];
    int argIndex = 0;
    commandArray[argIndex++] = javaCommand;
    while (argIndex - 1 < jvmargs.size()) {
      String arg = jvmargs.get(argIndex - 1);
      commandArray[argIndex++] = arg;
    }
    commandArray[argIndex++] = "-cp";
    int classpathArgIndex = argIndex; //app-specific arg.
    commandArray[argIndex++] = null;
    commandArray[argIndex++] = getGwtCompilerClass();
    commandArray[argIndex++] = "-gen";
    commandArray[argIndex++] = getGwtGenDir().getAbsolutePath();
    commandArray[argIndex++] = "-style";
    int styleArgIndex = argIndex;
    commandArray[argIndex++] = null; //app-specific arg.
    commandArray[argIndex++] = "-out";
    int outArgIndex = argIndex;
    commandArray[argIndex++] = null; //app-specific arg.
    int moduleNameIndex = argIndex;
    commandArray[argIndex] = null; //module-specific arg.

    for (GWTApp gwtApp : gwtApps) {
      String appName = gwtApp.getName();
      File appSource = enunciate.resolvePath(gwtApp.getSrcDir());
      commandArray[classpathArgIndex] = classpath.toString() + File.pathSeparatorChar + appSource.getAbsolutePath();
      String style = gwtApp.getJavascriptStyle().toString();
      commandArray[styleArgIndex] = style;
      File appDir = getAppGenerateDir(appName);
      String out = appDir.getAbsolutePath();
      commandArray[outArgIndex] = out;

      for (GWTAppModule appModule : gwtApp.getModules()) {
        String moduleName = appModule.getName();
        commandArray[moduleNameIndex] = moduleName;
        info("Executing GWTCompile for module '%s'...", moduleName);
        if (enunciate.isDebug()) {
          StringBuilder command = new StringBuilder();
          for (String commandPiece : commandArray) {
            command.append(' ').append(commandPiece);
          }
          debug("Executing GWTCompile for module %s with the command: %s", moduleName, command);
        }
        ProcessBuilder processBuilder = new ProcessBuilder(commandArray);
        processBuilder.directory(getGenerateDir());
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
          throw new EnunciateException("Unexpected inturruption of the GWT compile process.");
        }

        if (procCode != 0) {
          throw new EnunciateException("GWT compile failed for module " + moduleName);
        }

        String outputPath = appModule.getOutputPath();
        File moduleOutputDir = appDir;
        if ((outputPath != null) && (!"".equals(outputPath.trim()))) {
          moduleOutputDir = new File(appDir, outputPath);
        }
        File moduleGenDir = new File(appDir, moduleName);
        if (!moduleOutputDir.equals(moduleGenDir)) {
          moduleOutputDir.mkdirs();
          enunciate.copyDir(moduleGenDir, moduleOutputDir);
          deleteDir(moduleGenDir);
        }
      }
    }
  }

  /**
   * Delete a directory on the filesystem.
   *
   * @param dir The directory to delete.
   * @return Whether the directory was successfully deleted.
   */
  private boolean deleteDir(File dir) {
    if (dir.exists()) {
      File[] files = dir.listFiles();
      for (File file : files) {
        if (file.isDirectory()) {
          deleteDir(file);
        }
        else {
          file.delete();
        }
      }
    }
    return dir.delete();
  }

  @Override
  protected void doCompile() throws EnunciateException, IOException {
    Enunciate enunciate = getEnunciate();

    if (this.gwtApps.size() > 0) {
      doGWTCompile();
      enunciate.setProperty("gwt.app.dir", getAppGenerateDir());
      enunciate.addArtifact(new FileArtifact(getName(), "gwt.app.dir", getAppGenerateDir()));
    }

    info("Compiling the GWT client-side files...");
    Collection<String> clientSideFiles = enunciate.getJavaFiles(getClientSideGenerateDir());
    enunciate.invokeJavac(enunciate.getEnunciateClasspath(), getClientSideCompileDir(), Arrays.asList("-source", "1.4", "-g"), clientSideFiles.toArray(new String[clientSideFiles.size()]));
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
    enunciate.copyDir(getClientSideGenerateDir(), getClientSideCompileDir());
    enunciate.zip(clientJar, getClientSideCompileDir());
    enunciate.setProperty("gwt.client.jar", clientJar);

    List<ArtifactDependency> clientDeps = new ArrayList<ArtifactDependency>();
    MavenDependency gwtUserDependency = new MavenDependency();
    gwtUserDependency.setId("gwt-user");
    gwtUserDependency.setArtifactType("jar");
    gwtUserDependency.setDescription("Base GWT classes.");
    gwtUserDependency.setGroupId("com.google.gwt");
    gwtUserDependency.setURL("http://code.google.com/webtoolkit/");
    gwtUserDependency.setVersion("1.4.60");
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
    clientArtifact.setPublic(clientJarDownloadable);
    gwtClientArtifact.addArtifact(clientArtifact);
    gwtClientArtifact.setDependencies(clientDeps);
    enunciate.addArtifact(clientArtifact);
    if (clientJarDownloadable) {
      enunciate.addArtifact(gwtClientArtifact);
    }
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
   * Whether the given type declaration is GWT-transient.
   *
   * @param declaration The type declaration.
   * @return Whether the given tyep declaration is GWT-transient.
   */
  protected boolean isGWTTransient(TypeDeclaration declaration) {
    return isGWTTransient((Declaration) declaration) || isGWTTransient(declaration.getPackage());
  }

  /**
   * Whether the given type declaration is GWT-transient.
   *
   * @param declaration The type declaration.
   * @return Whether the given tyep declaration is GWT-transient.
   */
  protected boolean isGWTTransient(Declaration declaration) {
    return declaration != null && declaration.getAnnotation(GWTTransient.class) != null;
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
   * The GWT gen directory.  (I still don't know what this is used for, exactly.)
   *
   * @return The GWT gen directory.
   */
  public File getGwtGenDir() {
    return new File(getGenerateDir(), ".gwt-gen");
  }

  /**
   * Get the compile directory for client-side GWT classes.
   *
   * @return The compile directory for client-side GWT classes.
   */
  public File getClientSideCompileDir() {
    return new File(getCompileDir(), "client");
  }

  /**
   * The base generate dir for the gwt applications.
   *
   * @return The base generate dir for the gwt applications.
   */
  public File getAppGenerateDir() {
    return new File(getCompileDir(), "gwtapps");
  }

  /**
   * The generate dir for the specified app.
   *
   * @param appName The app name.
   * @return The generate dir for the specified app.
   */
  protected File getAppGenerateDir(String appName) {
    File appsDir = getAppGenerateDir();
    if ("".equals(appName)) {
      return appsDir;
    }
    else {
      return new File(appsDir, appName);
    }
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
    return new GWTValidator(this.rpcModuleNamespace, this.enforceNamespaceConformance, this.enforceNoFieldAccessors);
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
   * The generated RPC module name.
   *
   * @return The generated RPC module name.
   */
  public String getRpcModuleName() {
    return rpcModuleName;
  }

  /**
   * The generated RPC module namespace.
   *
   * @return The generated RPC module namespace.
   */
  public String getRpcModuleNamespace() {
    return rpcModuleNamespace;
  }

  /**
   * The generated RPC module name.
   *
   * @param gwtModuleName The gwt module name.
   * @deprecated Use {@link #setRpcModuleName(String)}
   */
  public void setGwtModuleName(String gwtModuleName) {
    setRpcModuleName(gwtModuleName);
  }

  /**
   * The generated RPC module name.
   *
   * @param rpcModuleName The generated RPC module name.
   */
  public void setRpcModuleName(String rpcModuleName) {
    this.rpcModuleName = rpcModuleName;
    int lastDot = rpcModuleName.lastIndexOf('.');
    if (lastDot < 0) {
      throw new IllegalArgumentException("The gwt module name must be of the form 'gwt.module.ns.ModuleName'");
    }
    this.rpcModuleNamespace = rpcModuleName.substring(0, lastDot);
  }

  /**
   * Whether the client jar is downloadable.
   *
   * @return Whether the client jar is downloadable.
   */
  public boolean isClientJarDownloadable() {
    return clientJarDownloadable;
  }

  /**
   * Whether the client jar is downloadable.
   *
   * @param clientJarDownloadable Whether the client jar is downloadable.
   */
  public void setClientJarDownloadable(boolean clientJarDownloadable) {
    this.clientJarDownloadable = clientJarDownloadable;
  }

  /**
   * Whether to enforce namespace conformace on the server-side classes.
   *
   * @return Whether to enforce namespace conformace on the server-side classes.
   */
  public boolean isEnforceNamespaceConformance() {
    return enforceNamespaceConformance;
  }

  /**
   * Whether to enforce namespace conformace on the server-side classes.
   *
   * @param enforceNamespaceConformance Whether to enforce namespace conformace on the server-side classes.
   */
  public void setEnforceNamespaceConformance(boolean enforceNamespaceConformance) {
    this.enforceNamespaceConformance = enforceNamespaceConformance;
  }

  /**
   * Whether to enforce that field accessors can't be used.
   *
   * @return Whether to enforce that field accessors can't be used.
   */
  public boolean isEnforceNoFieldAccessors() {
    return enforceNoFieldAccessors;
  }

  /**
   * Whether to enforce that field accessors can't be used.
   *
   * @param enforceNoFieldAccessors Whether to enforce that field accessors can't be used.
   */
  public void setEnforceNoFieldAccessors(boolean enforceNoFieldAccessors) {
    this.enforceNoFieldAccessors = enforceNoFieldAccessors;
  }

  /**
   * The gwt home directory
   *
   * @return The gwt home directory
   */
  public String getGwtHome() {
    return gwtHome;
  }

  /**
   * Set the path to the GWT home directory.
   *
   * @param gwtHome The gwt home directory
   */
  public void setGwtHome(String gwtHome) {
    this.gwtHome = gwtHome;
  }

  /**
   * Extra JVM args for the GWT compile.
   *
   * @return Extra JVM args for the GWT compile.
   */
  public List<String> getGwtCompileJVMArgs() {
    return gwtCompileJVMArgs;
  }

  /**
   * Extra JVM args for the GWT compile.
   *
   * @param arg Extra JVM args for the GWT compile.
   */
  public void addGwtCompileJVMArg(String arg) {
    this.gwtCompileJVMArgs.add(arg);
  }

  /**
   * The GWT compiler class.
   *
   * @return The GWT compiler class.
   */
  public String getGwtCompilerClass() {
    return gwtCompilerClass;
  }

  /**
   * The GWT compiler class.
   *
   * @param gwtCompilerClass The GWT compiler class.
   */
  public void setGwtCompilerClass(String gwtCompilerClass) {
    this.gwtCompilerClass = gwtCompilerClass;
  }

  /**
   * The gwt apps to compile.
   *
   * @return The gwt apps to compile.
   */
  public List<GWTApp> getGwtApps() {
    return gwtApps;
  }

  /**
   * Adds a gwt app to be compiled.
   *
   * @param gwtApp The gwt app to be compiled.
   */
  public void addGWTApp(GWTApp gwtApp) {
    this.gwtApps.add(gwtApp);
  }
}
