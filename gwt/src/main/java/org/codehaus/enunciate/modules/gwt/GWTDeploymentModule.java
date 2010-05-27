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

package org.codehaus.enunciate.modules.gwt;

import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.TypeDeclaration;
import freemarker.template.*;
import net.sf.jelly.apt.decorations.JavaDoc;
import net.sf.jelly.apt.freemarker.FreemarkerJavaDoc;
import org.apache.commons.digester.RuleSet;
import org.codehaus.enunciate.EnunciateException;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.apt.EnunciateClasspathListener;
import org.codehaus.enunciate.config.SchemaInfo;
import org.codehaus.enunciate.config.WsdlInfo;
import org.codehaus.enunciate.contract.jaxb.TypeDefinition;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.jaxws.WebFault;
import org.codehaus.enunciate.contract.jaxws.WebMethod;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.contract.jaxrs.RootResource;
import org.codehaus.enunciate.contract.jaxrs.ResourceMethod;
import org.codehaus.enunciate.main.*;
import org.codehaus.enunciate.main.webapp.BaseWebAppFragment;
import org.codehaus.enunciate.main.webapp.WebAppComponent;
import org.codehaus.enunciate.modules.FreemarkerDeploymentModule;
import org.codehaus.enunciate.modules.ProjectExtensionModule;
import org.codehaus.enunciate.modules.GWTHomeAwareModule;
import org.codehaus.enunciate.modules.gwt.config.GWTApp;
import org.codehaus.enunciate.modules.gwt.config.GWTAppModule;
import org.codehaus.enunciate.modules.gwt.config.GWTRuleSet;
import org.codehaus.enunciate.template.freemarker.ClientPackageForMethod;
import org.codehaus.enunciate.template.freemarker.SimpleNameWithParamsMethod;
import org.codehaus.enunciate.template.freemarker.AccessorOverridesAnotherMethod;
import org.codehaus.enunciate.util.TypeDeclarationComparator;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * <li>The "label" attribute is used to determine the name of the client-side artifact files. The default is the Enunciate project label.</li>
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
 * <li>The "useWrappedServices" attribute specifies whether to use wrapped GWT client services. This is an artifact from when GWT 1.4 was supported
 * and the generic types were unavailable. Default: false</li>
 * </ul>
 *
 * <h3>The "war" element</h3>
 *
 * <p>The "war" element under the "gwt" element is used to configure the webapp that will host the GWT endpoints and GWT applications. It supports
 * the following attributes:</p>
 *
 * <ul>
 * <li>The "gwtSubcontext" attribute is the subcontext at which the gwt endpoints will be mounted.  Default: "/gwt/".</li>
 * <li>The "gwtAppDir" attribute is the directory in the war to which the gwt applications will be put.  The default is the root of the war.</li>
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
 * outputDir is the empty string (""), which means the compiled module will be placed at the root of the application directory. <u>Note: as of GWT 1.6,
 * a new "war" directory structure is supported, along with support to control the directory where the GWT compiler puts the compiled application. Because
 * of this, the "outputDir" attribute will only be honored if not using GWT 1.6 or above.</u></li>
 * <li>The "shellPage" attribute specifies the (usually HTML) page to open when invoking the shell for this module (used to generate the shell script). By
 * default, the shell page is the [moduleId].html, where [moduleId] is the (short, unqualified) name of the module.</li>
 * </ul>
 *
 * <h3>The "gwtCompileJVMArg" element</h3>
 *
 * <p>The "gwtCompileJVMArg" element is used to specify additional JVM parameters that will be used when invoking GWTCompile.  It supports a single
 * "value" attribute.</p>
 *
 * <h3>The "gwtCompilerArg" element</h3>
 *
 * <p>The "gwtCompilerArg" element is used to specify additional arguments that will be psssed to the GWT compiler.  It supports a single
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
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;gwtHome="/home/myusername/tools/gwt-linux-1.5.2"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;app srcDir="src/main/mainapp"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;module name="com.mycompany.apps.main.MyRootModule"/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;module name="com.mycompany.apps.main.MyModuleTwo" outputDir="two"/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/app&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;app srcDir="src/main/anotherapp" name="another"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;module name="com.mycompany.apps.another.AnotherRootModule"/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;module name="com.mycompany.apps.another.MyModuleThree" outputDir="three"/&gt;
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
 * <li>The "[appName].[moduleName].shell" artifact is the GWT shell script used to invoke the gwt shell for the module [moduleName] in app [appName].</li>
 * Script is different from the alternative in that it assumes a server is already running before invoking the GWTShell.</li>
 * </ul>
 *
 * @author Ryan Heaton
 * @docFileName module_gwt.html
 */
public class GWTDeploymentModule extends FreemarkerDeploymentModule implements ProjectExtensionModule, GWTHomeAwareModule, EnunciateClasspathListener {

  private boolean forceGenerateJsonOverlays = false;
  private boolean disableJsonOverlays = false;
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
  private final List<String> gwtCompilerArgs = new ArrayList<String>();
  private String gwtCompilerClass;
  private String gwtSubcontext = "/gwt";
  private String gwtAppDir = null;
  private boolean useWrappedServices = false;
  private boolean gwtRtFound = false;
  private boolean springDIFound = false;
  private boolean jacksonXcAvailable = false;
  private String label = null;
  private int[] gwtVersion = null;
  private GWTModuleClasspathHandler gwtClasspathHandler;

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

    if (!isDisabled()) {
      if (rpcModuleName == null) {
        throw new EnunciateException("You must specify a \"rpcModuleName\" for the GWT module.");
      }
      if (rpcModuleNamespace == null) {
        throw new EnunciateException("You must specify a \"rpcModuleNamespace\" for the GWT module.");
      }

      gwtClasspathHandler = new GWTModuleClasspathHandler(enunciate);
      enunciate.addClasspathHandler(gwtClasspathHandler);

      if (gwtApps.size() > 0) {
        if (this.gwtHome == null) {
          throw new EnunciateException("To compile a GWT app you must specify the GWT home directory, either in configuration, by setting the GWT_HOME environment variable, or setting the 'gwt.home' system property.");
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

      if (this.gwtVersion == null) {
        int[] gwtVersion = null; //default to 1.5
        if (this.gwtHome != null) {
          File about = new File(gwtHome, "about.txt");
          if (about.exists()) {
            try {
              BufferedReader reader = new BufferedReader(new FileReader(about));
              String line = reader.readLine();
              if (line != null) {
                Matcher matcher = Pattern.compile("[\\d\\.]+").matcher(line);
                if (matcher.find()) {
                  String gwtVersionStr = matcher.group();
                  if (getEnunciate().isDebug()) {
                    getEnunciate().debug("Targeting GWT version %s according to %s.", gwtVersionStr, (this.gwtHome + File.separatorChar + "about.txt"));
                  }

                  try {
                    gwtVersion = parseGwtVersion(gwtVersionStr);
                  }
                  catch (NumberFormatException e) {
                    getEnunciate().warn("Invalid GWT version %s according to %s.", gwtVersionStr, (this.gwtHome + File.separatorChar + "about.txt"));
                  }
                }
                else {
                  getEnunciate().warn("Unable to determine GWT version from %s.", (this.gwtHome + File.separatorChar + "about.txt"));
                }
              }
              reader.close();
            }
            catch (IOException e) {
              //fall through...
            }
          }
        }

        if (gwtVersion == null) {
          gwtVersion = new int[]{1, 5};
        }

        this.gwtVersion = gwtVersion;
      }

      if (this.gwtVersion.length < 2) {
        throw new IllegalStateException("Illegal GWT version.");
      }

      if (!gwtVersionGreaterThan(1, 4)) {
        throw new EnunciateException(String.format("As of version 1.15, Enunciate no longer supports versions of GWT less than 1.5. " +
          "It appears GWT %s.%s is being used, according to %s. If this is an invalid assessment, you can get around this error by " +
          "setting the correct version with the 'gwtVersion' attribute of the Enunciate GWT module configuration.",
                                                   this.gwtVersion[0],
                                                   this.gwtVersion[1],
                                                   (this.gwtHome + File.separatorChar + "about.txt")));
      }

      if (getGwtCompilerClass() == null) {
        setGwtCompilerClass(gwtVersionGreaterThan(1, 5) ? "com.google.gwt.dev.Compiler" : "com.google.gwt.dev.GWTCompiler");
      }
    }
  }

  protected boolean gwtVersionGreaterThan(int major, int minor) {
    return (this.gwtVersion[0] == major) ? (this.gwtVersion[1] > minor) : (this.gwtVersion[0] > major);
  }

  @Override
  public void initModel(EnunciateFreemarkerModel model) {
    super.initModel(model);

    if (!isDisabled()) {
      if (!gwtRtFound && !isGenerateJsonOverlays()) {
        info("GWT runtime wasn't found on the classpath. If you're doing GWT-RPC, you're going to fail at runtime.");
      }
    }
  }

  public void onClassesFound(Set<String> classes) {
    gwtRtFound |= classes.contains("org.codehaus.enunciate.modules.gwt.GWTEndpointImpl");
    springDIFound |= classes.contains("org.springframework.beans.factory.annotation.Autowired");
    jacksonXcAvailable |= classes.contains("org.codehaus.jackson.xc.JaxbAnnotationIntrospector");
  }

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException, EnunciateException {
    File clientSideGenerateDir = getClientSideGenerateDir();
    File serverSideGenerateDir = getServerSideGenerateDir();
    boolean upToDate = enunciate.isUpToDateWithSources(clientSideGenerateDir) && enunciate.isUpToDateWithSources(serverSideGenerateDir);
    if (!upToDate) {
      //load the references to the templates....
      URL typeMapperTemplate = getTemplateURL("gwt-type-mapper.fmt");
      URL enumTypeMapperTemplate = getTemplateURL("gwt-enum-15-type-mapper.fmt");
      URL faultMapperTemplate = getTemplateURL("gwt-fault-mapper.fmt");
      URL moduleXmlTemplate = getTemplateURL("gwt-module-xml.fmt");

      URL eiTemplate = isUseWrappedServices() ? getTemplateURL("gwt-legacy-endpoint-interface.fmt") : getTemplateURL("gwt-endpoint-interface.fmt");
      URL endpointImplTemplate = getTemplateURL("gwt-endpoint-impl.fmt");
      URL faultTemplate = getTemplateURL("gwt-fault.fmt");
      URL typeTemplate = getTemplateURL("gwt-type.fmt");
      URL overlayTypeTemplate = getTemplateURL("gwt-overlay-type.fmt");
      URL enumTypeTemplate = getTemplateURL("gwt-enum-15-type.fmt");
      URL overlayEnumTypeTemplate = getTemplateURL("gwt-enum-overlay-type.fmt");

      EnunciateFreemarkerModel model = getModel();
      model.put("useSpringDI", this.springDIFound);
      model.put("useWrappedServices", this.isUseWrappedServices());
      Map<String, String> conversions = new LinkedHashMap<String, String>();
      Set<String> knownGwtPackages = this.gwtClasspathHandler != null ? this.gwtClasspathHandler.getSourcePackagesToModules().keySet() : Collections.<String>emptySet();
      for (String knownGwtPackage : knownGwtPackages) {
        //makes sure any known gwt packages are preserved.
        conversions.put(knownGwtPackage, knownGwtPackage);
      }
      Map<String, String> overlayConversions = new HashMap<String, String>();
      String clientNamespace = this.rpcModuleNamespace + ".client";
      conversions.put(this.rpcModuleNamespace, clientNamespace);
      overlayConversions.put(this.rpcModuleNamespace, clientNamespace + ".json");
      if (!this.enforceNamespaceConformance) {
        TreeSet<WebFault> allFaults = new TreeSet<WebFault>(new TypeDeclarationComparator());
        for (WsdlInfo wsdlInfo : model.getNamespacesToWSDLs().values()) {
          for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
            if (!isGWTTransient(ei)) {
              String pckg = ei.getPackage().getQualifiedName();
              if (!pckg.startsWith(this.rpcModuleNamespace) && !conversions.containsKey(pckg)) {
                conversions.put(pckg, clientNamespace + "." + pckg);
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
            if (!pckg.startsWith(this.rpcModuleNamespace) && !conversions.containsKey(pckg) && (getKnownGwtModule(webFault) == null)) {
              conversions.put(pckg, clientNamespace + "." + pckg);
            }
          }
        }
        for (SchemaInfo schemaInfo : model.getNamespacesToSchemas().values()) {
          for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
            if (!isGWTTransient(typeDefinition)) {
              String pckg = typeDefinition.getPackage().getQualifiedName();
              if (!pckg.startsWith(this.rpcModuleNamespace) && !conversions.containsKey(pckg)) {
                if (getKnownGwtModule(typeDefinition) == null) {
                  conversions.put(pckg, clientNamespace + "." + pckg);
                }
                overlayConversions.put(pckg, clientNamespace + ".json." + pckg);
              }
            }
          }
        }
      }

      ClientClassnameForMethod classnameFor = new ClientClassnameForMethod(conversions);
      classnameFor.setJdk15(true);
      OverlayClientClassnameForMethod overlayClassnameFor = new OverlayClientClassnameForMethod(overlayConversions);
      overlayClassnameFor.setJdk15(true);
      model.put("packageFor", new ClientPackageForMethod(conversions));
      model.put("overlayPackageFor", new ClientPackageForMethod(overlayConversions));
      model.put("classnameFor", classnameFor);
      model.put("overlayClassnameFor", overlayClassnameFor);
      model.put("simpleNameFor", new SimpleNameWithParamsMethod(classnameFor));
      model.put("gwtSubcontext", getGwtSubcontext());
      model.put("accessorOverridesAnother", new AccessorOverridesAnotherMethod());

      model.setFileOutputDirectory(clientSideGenerateDir);
      Properties gwt2jaxbMappings = new Properties();
      TreeSet<WebFault> allFaults = new TreeSet<WebFault>(new TypeDeclarationComparator());

      Set<String> importedModules = new TreeSet<String>();
      if (isGenerateRPCSupport()) {
        debug("Generating the GWT endpoints...");
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

        debug("Generating the GWT faults...");
        for (WebFault webFault : allFaults) {
          if (!isGWTTransient(webFault)) {
            String knownGwtModule = getKnownGwtModule(webFault);
            if (knownGwtModule == null) {
              model.put("fault", webFault);
              processTemplate(faultTemplate, model);
            }
            else {
              importedModules.add(knownGwtModule);
              debug("Skipping generating fault for %s because it's in a known GWT module.", webFault.getQualifiedName());
            }
          }
        }

        debug("Generating the GWT types...");
        for (SchemaInfo schemaInfo : model.getNamespacesToSchemas().values()) {
          for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
            if (!isGWTTransient(typeDefinition)) {
              String knownGwtModule = getKnownGwtModule(typeDefinition);
              if (knownGwtModule == null) {

                model.put("type", typeDefinition);

                URL template = typeDefinition.isEnum() ? enumTypeTemplate : typeTemplate;
                processTemplate(template, model);
              }
              else {
                importedModules.add(knownGwtModule);
                debug("Skipping generating GWT type for %s because it's in a known GWT module.", typeDefinition.getQualifiedName());
              }
            }
          }
        }
      }

      if (isGenerateJsonOverlays()) {
        debug("Generating the GWT json overlay types...");
        for (SchemaInfo schemaInfo : model.getNamespacesToSchemas().values()) {
          for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
            if (!isGWTTransient(typeDefinition)) {
              model.put("type", typeDefinition);
              URL template = typeDefinition.isEnum() ? overlayEnumTypeTemplate : overlayTypeTemplate;
              processTemplate(template, model);
            }
          }
        }
      }

      model.put("gwtModuleName", this.rpcModuleName);
      model.put("importedGwtModules", importedModules);
      processTemplate(moduleXmlTemplate, model);

      model.setFileOutputDirectory(serverSideGenerateDir);
      if (isGenerateRPCSupport()) {
        debug("Generating the GWT endpoint implementations...");
        for (WsdlInfo wsdlInfo : model.getNamespacesToWSDLs().values()) {
          for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
            if (!isGWTTransient(ei)) {
              model.put("endpointInterface", ei);
              processTemplate(endpointImplTemplate, model);
            }
          }
        }

        debug("Generating the GWT type mappers...");
        for (SchemaInfo schemaInfo : model.getNamespacesToSchemas().values()) {
          for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
            if (!isGWTTransient(typeDefinition)) {
              if (typeDefinition.isEnum()) {
                model.put("type", typeDefinition);
                processTemplate(enumTypeMapperTemplate, model);
                gwt2jaxbMappings.setProperty(classnameFor.convert(typeDefinition), typeDefinition.getQualifiedName());
              }
              else if (getKnownGwtModule(typeDefinition) == null) {
                if (!typeDefinition.isEnum()) {
                  model.put("type", typeDefinition);
                  processTemplate(typeMapperTemplate, model);
                  gwt2jaxbMappings.setProperty(classnameFor.convert(typeDefinition), typeDefinition.getQualifiedName());
                }
              }
              else {
                debug("Skipping generation of type mapper for %s because it's a known GWT type.", typeDefinition.getQualifiedName());
              }
            }
          }
        }

        debug("Generating the GWT fault mappers...");
        for (WebFault webFault : allFaults) {
          if (!isGWTTransient(webFault) && (getKnownGwtModule(webFault) == null)) {
            model.put("fault", webFault);
            processTemplate(faultMapperTemplate, model);
            gwt2jaxbMappings.setProperty(classnameFor.convert(webFault), webFault.getQualifiedName());
          }
        }

        FileOutputStream mappingsOut = new FileOutputStream(new File(serverSideGenerateDir, "gwt-to-jaxb-mappings.properties"));
        gwt2jaxbMappings.store(mappingsOut, "mappings for gwt classes to jaxb classes.");
        mappingsOut.flush();
        mappingsOut.close();
      }
    }
    else {
      info("Skipping GWT source generation as everything appears up-to-date...");
    }

    enunciate.addArtifact(new FileArtifact(getName(), "gwt.client.src.dir", clientSideGenerateDir));
    enunciate.addArtifact(new FileArtifact(getName(), "gwt.server.src.dir", serverSideGenerateDir));

    enunciate.addAdditionalSourceRoot(clientSideGenerateDir); //server-side also uses client-side classes.
    enunciate.addAdditionalSourceRoot(serverSideGenerateDir);
  }

  private String getKnownGwtModule(TypeDeclaration declaration) {
    Set<String> knownGwtPackages = this.gwtClasspathHandler != null ? this.gwtClasspathHandler.getSourcePackagesToModules().keySet() : Collections.<String>emptySet();
    String declPackage = declaration.getPackage() == null ? "" : declaration.getPackage().getQualifiedName();
    for (String knownGwtPackage : knownGwtPackages) {
      if (declPackage.startsWith(knownGwtPackage)) {
        return this.gwtClasspathHandler.getSourcePackagesToModules().get(knownGwtPackage);
      }
    }

    return null;
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
    File gwtDevJar = new File(gwtHomeDir, "gwt-dev.jar");
    if (!gwtDevJar.exists()) {
      File linuxDevJar = new File(gwtHomeDir, "gwt-dev-linux.jar");
      gwtDevJar = linuxDevJar;

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
    }

    boolean windows = false;
    File javaBinDir = new File(System.getProperty("java.home"), "bin");
    File javaExecutable = new File(javaBinDir, "java");
    if (!javaExecutable.exists()) {
      //append the "exe" for windows users.
      javaExecutable = new File(javaBinDir, "java.exe");
      windows = true;
    }

    String javaCommand = javaExecutable.getAbsolutePath();
    if (!javaExecutable.exists()) {
      warn("No java executable found in %s.  We'll just hope the environment is set up to execute 'java'...", javaBinDir.getAbsolutePath());
      javaCommand = "java";
    }

    StringBuilder classpath = new StringBuilder(enunciate.getEnunciateRuntimeClasspath());
    //append the client-side gwt directory.
    classpath.append(File.pathSeparatorChar).append(getClientSideGenerateDir().getAbsolutePath());
    //append the gwt-user jar.
    classpath.append(File.pathSeparatorChar).append(gwtUserJar.getAbsolutePath());
    //append the gwt-dev jar.
    classpath.append(File.pathSeparatorChar).append(gwtDevJar.getAbsolutePath());

    //so here's the GWT compile command:
    //java [extra jvm args] -cp [classpath] [compilerClass] -gen [gwt-gen-dir] -style [style] -out [out] [moduleName]
    List<String> jvmargs = getGwtCompileJVMArgs();
    List<String> compilerArgs = getGwtCompilerArgs();
    List<String> gwtcCommand = new ArrayList<String>(jvmargs.size() + compilerArgs.size() + 11);
    int argIndex = 0;
    gwtcCommand.add(argIndex++, javaCommand);
    for (String arg : jvmargs) {
      gwtcCommand.add(argIndex++, arg);
    }
    gwtcCommand.add(argIndex++, "-cp");
    int classpathArgIndex = argIndex; //app-specific arg.
    gwtcCommand.add(argIndex++, null);
    int compileClassIndex = argIndex;
    gwtcCommand.add(argIndex++, getGwtCompilerClass());
    gwtcCommand.add(argIndex++, "-gen");
    gwtcCommand.add(argIndex++, getGwtGenDir().getAbsolutePath());
    gwtcCommand.add(argIndex++, "-style");
    int styleArgIndex = argIndex;
    gwtcCommand.add(argIndex++, null); //app-specific arg.
    gwtcCommand.add(argIndex++, gwtVersionGreaterThan(1, 5) ? "-war" : "-out");
    int outArgIndex = argIndex;
    gwtcCommand.add(argIndex++, null); //app-specific arg.
    for (String arg : compilerArgs) {
      gwtcCommand.add(argIndex++, arg);
    }
    int moduleNameIndex = argIndex;
    gwtcCommand.add(argIndex, null); //module-specific arg.

    for (GWTApp gwtApp : gwtApps) {
      String appName = gwtApp.getName();
      File appSource = enunciate.resolvePath(gwtApp.getSrcDir());
      String style = gwtApp.getJavascriptStyle().toString();
      File appDir = getAppGenerateDir(appName);

      gwtcCommand.set(classpathArgIndex, classpath.toString() + File.pathSeparatorChar + appSource.getAbsolutePath());
      gwtcCommand.set(styleArgIndex, style);
      gwtcCommand.set(outArgIndex, appDir.getAbsolutePath());

      boolean upToDate = enunciate.isUpToDate(getClientSideGenerateDir(), appDir) && enunciate.isUpToDate(appSource, appDir);
      if (!upToDate) {
        for (GWTAppModule appModule : gwtApp.getModules()) {
          String moduleName = appModule.getName();

          gwtcCommand.set(moduleNameIndex, moduleName);
          debug("Executing GWTCompile for module '%s'...", moduleName);
          if (enunciate.isDebug()) {
            StringBuilder command = new StringBuilder();
            for (String commandPiece : gwtcCommand) {
              command.append(' ').append(commandPiece);
            }
            debug("Executing GWTCompile for module %s with the command: %s", moduleName, command);
          }
          ProcessBuilder processBuilder = new ProcessBuilder(gwtcCommand);
          processBuilder.directory(getGenerateDir());
          processBuilder.redirectErrorStream(true);
          Process process = processBuilder.start();
          BufferedReader procReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
          String line = procReader.readLine();
          while (line != null) {
            line = URLDecoder.decode(line, "utf-8").replaceAll("%", "%%").trim(); //GWT URL-encodes spaces and other weird Windows characters.
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

          if (!gwtVersionGreaterThan(1, 5)) {
            File moduleOutputDir = appDir;
            String outputPath = appModule.getOutputPath();
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

          StringBuilder shellCommand = new StringBuilder();
          for (int i = 0; i < moduleNameIndex; i++) {
            String commandArg = gwtcCommand.get(i);
            if (i == compileClassIndex) {
              commandArg = gwtVersionGreaterThan(1, 5) ? "com.google.gwt.dev.HostedMode" : "com.google.gwt.dev.GWTShell";
            }
            else if (commandArg.indexOf(' ') >= 0) {
              commandArg = '"' + commandArg + '"';
            }

            shellCommand.append(commandArg).append(' ');
          }

          //add any extra args before the module name.
          shellCommand.append(windows ? "%*" : "$@").append(' ');

          String shellPage = getModuleId(moduleName) + ".html";
          if (appModule.getShellPage() != null) {
            shellPage = appModule.getShellPage();
          }

          if (!gwtVersionGreaterThan(1, 5)) {
            //when invoking the shell for GWT 1.4 or 1.5, it requires a URL to load.
            //The URL is the [moduleName]/[shellPage.html]
            shellCommand.append(moduleName).append('/').append(shellPage);
          }
          else {
            //as of 1.6, you invoke it with -startupUrl [shellPage.html] [moduleName]
            shellCommand.append("-startupUrl ").append(shellPage).append(' ').append(moduleName);
          }

          File scriptFile = getShellScriptFile(appName, moduleName);
          scriptFile.getParentFile().mkdirs();
          FileWriter writer = new FileWriter(scriptFile);
          writer.write(shellCommand.toString());
          writer.flush();
          writer.close();

          File shellFile = getShellScriptFile(appName, moduleName);
          if (shellFile.exists()) {
            StringBuilder scriptArtifactId = new StringBuilder();
            if ((appName != null) && (appName.trim().length() > 0)) {
              scriptArtifactId.append(appName).append('.');
            }
            scriptArtifactId.append(moduleName).append(".shell");
            getEnunciate().addArtifact(new FileArtifact(getName(), scriptArtifactId.toString(), shellFile));
          }
          else {
            debug("No GWT shell script file exists at %s.  No artifact added.", shellFile);
          }
        }
      }
      else {
        info("Skipping GWT compile for app %s as everything appears up-to-date...", appName);
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
      enunciate.addArtifact(new FileArtifact(getName(), "gwt.app.dir", getAppGenerateDir()));
    }

    if (!enunciate.isUpToDate(getClientSideGenerateDir(), getClientSideCompileDir())) {
      debug("Compiling the GWT client-side files...");
      Collection<String> clientSideFiles = enunciate.getJavaFiles(getClientSideGenerateDir());
      String clientClasspath = enunciate.getRuntimeClasspath();
      enunciate.invokeJavac(clientClasspath, "1.5", getClientSideCompileDir(), new ArrayList<String>(), clientSideFiles.toArray(new String[clientSideFiles.size()]));
    }
    else {
      info("Skipping compile of GWT client-side files because everything appears up-to-date...");
    }
  }

  @Override
  protected void doBuild() throws EnunciateException, IOException {
    buildClientJar();

    //assemble the server-side webapp fragment
    BaseWebAppFragment webAppFragment = new BaseWebAppFragment(getName());

    //base webapp dir...
    File webappDir = new File(getBuildDir(), "webapp");
    webappDir.mkdirs();

    File gwtCompileDir = getAppGenerateDir();
    if ((this.gwtApps.size() > 0) && (gwtCompileDir != null) && (gwtCompileDir.exists())) {
      File gwtAppDir = webappDir;
      if ((getGwtAppDir() != null) && (!"".equals(getGwtAppDir()))) {
        debug("Gwt applications will be put into the %s subdirectory of the web application.", getGwtAppDir());
        gwtAppDir = new File(webappDir, getGwtAppDir());
      }
      getEnunciate().copyDir(gwtCompileDir, gwtAppDir, new File(gwtCompileDir, ".gwt-tmp"));
    }
    else {
      debug("No gwt apps were found.");
    }
    webAppFragment.setBaseDir(webappDir);

    File classesDir = new File(new File(webappDir, "WEB-INF"), "classes");
    File gwtToJaxbMappings = new File(getServerSideGenerateDir(), "gwt-to-jaxb-mappings.properties");
    if (gwtToJaxbMappings.exists()) {
      if (!enunciate.isUpToDate(gwtToJaxbMappings, new File(classesDir, "gwt-to-jaxb-mappings.properties"))) {
        enunciate.copyFile(gwtToJaxbMappings, new File(classesDir, "gwt-to-jaxb-mappings.properties"));
      }
      else {
        info("Skipping gwt-to-jaxb mappings copy because everything appears up to date!");
      }
    }

    //servlets.
    ArrayList<WebAppComponent> servlets = new ArrayList<WebAppComponent>();
    for (WsdlInfo wsdlInfo : getModel().getNamespacesToWSDLs().values()) {
      for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
        WebAppComponent gwtServlet = new WebAppComponent();
        gwtServlet.setClassname(ei.getPackage().getQualifiedName() + ".gwt.GWT" + ei.getSimpleName() + "Impl");
        gwtServlet.setName("GWT" + ei.getSimpleName());
        TreeSet<String> urlMappings = new TreeSet<String>();
        urlMappings.add(getGwtSubcontext() + '/' + ei.getServiceName());
        gwtServlet.setUrlMappings(urlMappings);
        servlets.add(gwtServlet);
      }
    }
    webAppFragment.setServlets(servlets);

    getEnunciate().addWebAppFragment(webAppFragment);
  }

  protected void buildClientJar() throws IOException, EnunciateException {
    Enunciate enunciate = getEnunciate();
    String clientJarName = getClientJarName();

    if (clientJarName == null) {
      String label = "enunciate";
      if (getLabel() != null) {
        label = getLabel();
      }
      else if ((enunciate.getConfig() != null) && (enunciate.getConfig().getLabel() != null)) {
        label = enunciate.getConfig().getLabel();
      }

      clientJarName = label + "-gwt-client.jar";
    }

    File clientJar = new File(getBuildDir(), clientJarName);
    if (!enunciate.isUpToDate(getClientSideGenerateDir(), clientJar)) {
      enunciate.copyDir(getClientSideGenerateDir(), getClientSideCompileDir());
      enunciate.zip(clientJar, getClientSideCompileDir());
    }
    else {
      info("GWT client jar appears up-to-date...");
    }

    List<ArtifactDependency> clientDeps = new ArrayList<ArtifactDependency>();
    MavenDependency gwtUserDependency = new MavenDependency();
    gwtUserDependency.setId("gwt-user");
    gwtUserDependency.setArtifactType("jar");
    gwtUserDependency.setDescription("Base GWT classes.");
    gwtUserDependency.setGroupId("com.google.gwt");
    gwtUserDependency.setURL("http://code.google.com/webtoolkit/");
    gwtUserDependency.setVersion(String.format("%s.%s", this.gwtVersion[0], this.gwtVersion[1]));
    clientDeps.add(gwtUserDependency);

    ClientLibraryArtifact gwtClientArtifact = new ClientLibraryArtifact(getName(), "gwt.client.library", "GWT Client Library");
    gwtClientArtifact.setPlatform("JavaScript/GWT");
    //read in the description from file:
    gwtClientArtifact.setDescription(readResource("library_description.fmt"));
    NamedFileArtifact clientArtifact = new NamedFileArtifact(getName(), "gwt.client.jar", clientJar);
    clientArtifact.setDescription("The binaries and sources for the GWT client library.");
    clientArtifact.setPublic(clientJarDownloadable);
    clientArtifact.setArtifactType(ArtifactType.binaries);
    gwtClientArtifact.addArtifact(clientArtifact);
    gwtClientArtifact.setDependencies(clientDeps);
    enunciate.addArtifact(clientArtifact);
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

    URL res = GWTDeploymentModule.class.getResource(resource);
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
   * The base generate dir for the gwt scripts (e.g. shell scripts).
   *
   * @return The base generate dir for the gwt scripts (e.g. shell scripts).
   */
  public File getGwtScriptDir() {
    return new File(getCompileDir(), "scripts");
  }

  /**
   * Get the GWT shell script file for the specified module, app.
   *
   * @param appName    The app name.
   * @param moduleName The module name.
   * @return The shell script file.
   */
  public File getShellScriptFile(String appName, String moduleName) {
    StringBuilder filename = new StringBuilder();
    if ((appName != null) && (appName.trim().length() > 0)) {
      filename.append(appName).append('-');
    }

    String moduleId = getModuleId(moduleName);
    filename.append(moduleId).append(".gwt-shell");
    File scriptDir = getGwtScriptDir();
    if (!scriptDir.exists()) {
      scriptDir.mkdirs();
    }
    return new File(scriptDir, filename.toString());
  }

  /**
   * Get the module id of the specified GWT module.
   *
   * @param moduleName The module name.
   * @return The module id.
   */
  protected String getModuleId(String moduleName) {
    String moduleId = moduleName;
    int lastDot = moduleName.lastIndexOf('.');
    if (lastDot >= 0) {
      moduleId = moduleName.substring(lastDot + 1);
    }
    return moduleId;
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
    return new GWTValidator(this.rpcModuleNamespace,
                            this.gwtClasspathHandler != null ? this.gwtClasspathHandler.getSourcePackagesToModules().keySet() : Collections.<String>emptySet(),
                            this.enforceNamespaceConformance,
                            this.enforceNoFieldAccessors);
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
   * Additional arguments to pass to the GWT compiler.
   *
   * @return Additional arguments to pass to the GWT compiler.
   */
  public List<String> getGwtCompilerArgs() {
    return gwtCompilerArgs;
  }

  /**
   * Additional argument to pass to the GWT compiler.
   *
   * @param arg The additional arg.
   */
  public void addGwtCompilerArg(String arg) {
    this.gwtCompilerArgs.add(arg);
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

  /**
   * The GWT subcontext.
   *
   * @return The GWT subcontext.
   */
  public String getGwtSubcontext() {
    return gwtSubcontext;
  }

  /**
   * The gwt subcontext.
   *
   * @param gwtSubcontext The gwt subcontext.
   */
  public void setGwtSubcontext(String gwtSubcontext) {
    if (gwtSubcontext == null) {
      throw new IllegalArgumentException("The GWT context must not be null.");
    }

    if ("".equals(gwtSubcontext)) {
      throw new IllegalArgumentException("The GWT context must not be the emtpy string.");
    }

    if (!gwtSubcontext.startsWith("/")) {
      gwtSubcontext = "/" + gwtSubcontext;
    }

    while (gwtSubcontext.endsWith("/")) {
      gwtSubcontext = gwtSubcontext.substring(gwtSubcontext.length() - 1);
    }

    this.gwtSubcontext = gwtSubcontext;
  }

  /**
   * The gwt app dir.
   *
   * @return The gwt app dir.
   */
  public String getGwtAppDir() {
    return gwtAppDir;
  }

  /**
   * The gwt app dir.
   *
   * @param gwtAppDir The gwt app dir.
   */
  public void setGwtAppDir(String gwtAppDir) {
    this.gwtAppDir = gwtAppDir;
  }

  /**
   * Whether to generated wrapped GWT remote services in the client-code.
   *
   * @return Whether to generated wrapped GWT remote services in the client-code.
   */
  public boolean isUseWrappedServices() {
    return useWrappedServices;
  }

  /**
   * Whether to generated wrapped GWT remote services in the client-code.
   *
   * @param useWrappedServices Whether to generated wrapped GWT remote services in the client-code.
   */
  public void setUseWrappedServices(boolean useWrappedServices) {
    this.useWrappedServices = useWrappedServices;
  }

  /**
   * Whether to generate JSON overlays.
   *
   * @return Whether to generate JSON overlays.
   */
  public boolean isGenerateJsonOverlays() {
    return forceGenerateJsonOverlays || (!disableJsonOverlays && jacksonXcAvailable && existsAnyJsonResourceMethod(getModelInternal().getRootResources()));
  }

  /**
   * Whether to generate the RPC support classes.
   *
   * @return Whether to generate the RPC support classes.
   */
  public boolean isGenerateRPCSupport() {
    return !getModelInternal().getNamespacesToWSDLs().isEmpty();
  }

  /**
   * Whether to generate JSON overlays.
   *
   * @param generateJsonOverlays Whether to generate JSON overlays.
   */
  public void setGenerateJsonOverlays(boolean generateJsonOverlays) {
    this.forceGenerateJsonOverlays = generateJsonOverlays;
    this.disableJsonOverlays = !generateJsonOverlays;
  }

  /**
   * The label for the GWT client API.
   *
   * @return The label for the GWT client API.
   */
  public String getLabel() {
    return label;
  }

  /**
   * The label for the ActionScript API.
   *
   * @param label The label for the ActionScript API.
   */
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * Sets the GWT version that Enunciate will target.
   *
   * @param version The GWT version Enunciate will target.
   */
  public void setGwtVersion(String version) {
    this.gwtVersion = parseGwtVersion(version);
  }

  protected int[] parseGwtVersion(String version) throws NumberFormatException {
    String[] versionStr = version.split("\\.");
    int[] gwtVersion = new int[Math.max(versionStr.length, 2)];
    for (int i = 0; i < versionStr.length; i++) {
      String versionToken = versionStr[i];
      gwtVersion[i] = Integer.parseInt(versionToken);
    }
    return gwtVersion;
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

  public List<File> getProjectSources() {
    List<File> projectSources = new ArrayList<File>();
    projectSources.add(getClientSideGenerateDir());
    projectSources.add(getServerSideGenerateDir());
    for (GWTApp gwtApp : getGwtApps()) {
      File srcDir = getEnunciate().resolvePath(gwtApp.getSrcDir());
      projectSources.add(srcDir);
    }
    return projectSources;
  }

  public List<File> getProjectTestSources() {
    return Collections.emptyList();
  }

  public List<File> getProjectResourceDirectories() {
    return Collections.emptyList();
  }

  public List<File> getProjectTestResourceDirectories() {
    return Collections.emptyList();
  }
}
