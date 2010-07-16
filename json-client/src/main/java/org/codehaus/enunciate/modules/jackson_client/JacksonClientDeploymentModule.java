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

package org.codehaus.enunciate.modules.jackson_client;

import com.sun.mirror.declaration.TypeDeclaration;
import freemarker.template.*;
import net.sf.jelly.apt.decorations.JavaDoc;
import net.sf.jelly.apt.freemarker.FreemarkerJavaDoc;
import org.apache.commons.digester.RuleSet;
import org.codehaus.enunciate.EnunciateException;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.config.SchemaInfo;
import org.codehaus.enunciate.contract.jaxb.Registry;
import org.codehaus.enunciate.contract.jaxb.TypeDefinition;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.main.ArtifactType;
import org.codehaus.enunciate.main.ClientLibraryArtifact;
import org.codehaus.enunciate.main.Enunciate;
import org.codehaus.enunciate.main.NamedFileArtifact;
import org.codehaus.enunciate.modules.DeploymentModule;
import org.codehaus.enunciate.modules.FreemarkerDeploymentModule;
import org.codehaus.enunciate.modules.ProjectExtensionModule;
import org.codehaus.enunciate.modules.jackson_client.config.JacksonClientRuleSet;
import org.codehaus.enunciate.modules.xml.XMLDeploymentModule;
import org.codehaus.enunciate.template.freemarker.ClientClassnameForMethod;
import org.codehaus.enunciate.template.freemarker.ClientPackageForMethod;
import org.codehaus.enunciate.template.freemarker.SimpleNameWithParamsMethod;
import org.codehaus.enunciate.util.AntPatternMatcher;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.*;

/**
 * <h1>JSON Client Module</h1>
 * <p/>
 * <p>The JSON client deployment module generates the client-side libraries that will access the
 * deployed web app using <a href="http://www.json.org/">JSON</a>.</p>
 * <p/>
 * <p>A useful by-product of this module is the generated Java types that can be used via JAXB to serialize the client-side
 * objects to/from XML. These can be used, for example, to use Java objects to interface via REST endpoints.</p>
 * <p/>
 * <p>The order of the JSON client deployment module is 50, so as to allow the JSON module to apply
 * metadata to the endpoints before processing the client.</p>
 * <p/>
 * <p>Note that the JSON Client module is disabled by default, so you must enable it in the enunciate configuration file, e.g.:</p>
 * <p/>
 * <code class="console">
 * &lt;enunciate&gt;
 * &nbsp;&nbsp;&lt;modules&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;json-client disabled="false"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;...
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/json-client&gt;
 * &nbsp;&nbsp;&lt;/modules&gt;
 * &lt;/enunciate&gt;
 * <p/>
 * <ul>
 * <li><a href="#steps">steps</a></li>
 * <li><a href="#config">configuration</a></li>
 * <li><a href="#artifacts">artifacts</a></li>
 * </ul>
 * <p/>
 * <h1><a name="steps">Steps</a></h1>
 * <p/>
 * <h3>generate</h3>
 * <p/>
 * <p>The "generate" step is by far the most intensive and complex step in the execution of the JSON client
 * module.  The "generate" step generates all source code for accessing the deployed API.</p>
 * <p/>
 * <h3>compile</h3>
 * <p/>
 * <p>During the "compile" step, the JSON client module compiles the code that was generated.</p>
 * <p/>
 * <h3>build</h3>
 * <p/>
 * <p>The "build" step assembles the classes that were assembled into a jar.  It also creates a source jar for
 * the libraries.</p>
 * <p/>
 * <h1><a name="config">Configuration</a></h1>
 * <p/>
 * <p>The JSON client module is configured by the "json-client" element under the "modules" element of the
 * enunciate configuration file.  It supports the following attributes:</p>
 * <p/>
 * <ul>
 * <li>The "label" attribute is used to determine the name of the client-side artifact files. The default is the Enunciate project label.</li>
 * <li>The "jarName" attribute specifies the name of the jar file(s) that are to be created.  If no jar name is specified,
 * the name will be calculated from the enunciate label, or a default will be supplied.</li>
 * </ul>
 * <p/>
 * <h3>The "package-conversions" element</h3>
 * <p/>
 * <p>The "package-conversions" subelement of the "json-client" element is used to map packages from
 * the original API packages to different package names.  This element supports an arbitrary number of
 * "convert" child elements that are used to specify the conversions.  These "convert" elements support
 * the following attributes:</p>
 * <p/>
 * <ul>
 * <li>The "from" attribute specifies the package that is to be converted.  This package will match
 * all classes in the package as well as any subpackages of the package.  This means that if "org.enunciate"
 * were specified, it would match "org.enunciate", "org.enunciate.api", and "org.enunciate.api.impl".</li>
 * <li>The "to" attribute specifies what the package is to be converted to.  Only the part of the package
 * that matches the "from" attribute will be converted.</li>
 * </ul>
 * <p/>
 * <h3>The "server-side-type" element</h3>
 * <p/>
 * <p>An arbitrary number of "server-side-type" elements are allowed as child elements of the "json-client" element.  The "server-side-type" element
 * can be used to specify a server-side type that is to be ported directly over to the client-side library (as opposed to <i>generating</i> the client-side type
 * from the server-side type). This can be useful to provide more useful client-side capabilities, but requires that there be no package conversions for types
 * and web faults.</p>
 * <p/>
 * <p>The "server-side-type" element supports one attribute, "pattern" that defines an ant-style pattern of the type(s) that are to be included (using a '.'
 * for separating the package name).</p>
 * <p/>
 * <h1><a name="artifacts">Artifacts</a></h1>
 * <p/>
 * <p>The JSON client deployment module exports the following artifacts:</p>
 * <p/>
 * <ul>
 * <li>The libraries and sources are exported under the id "json.client.library".  (Note that this is a
 * bundle, so if exporting to a directory multiple files will be exported.  If exporting to a file, the bundle will
 * be zipped first.)</li>
 * </ul>
 *
 * @author Ryan Heaton
 * @docFileName module_json_client.html
 */
public class JacksonClientDeploymentModule extends FreemarkerDeploymentModule implements ProjectExtensionModule {

  private String jarName = null;
  private final Map<String, String> clientPackageConversions;
  private final JacksonClientRuleSet configurationRules;
  private final Set<String> serverSideTypesToUse;
  private String label = null;

  public JacksonClientDeploymentModule() {
    this.clientPackageConversions = new HashMap<String, String>();
    this.configurationRules = new JacksonClientRuleSet();
    this.serverSideTypesToUse = new TreeSet<String>();
  }

  /**
   * @return "json-client"
   */
  @Override
  public String getName() {
    return "jackson-client";
  }

  /**
   * @return 50
   */
  @Override
  public int getOrder() {
    return 200;
  }

  @Override
  public void init(Enunciate enunciate) throws EnunciateException {
    super.init(enunciate);

    if (!isDisabled() && !enunciate.isModuleEnabled("xml")) {
      throw new EnunciateException("The JSON Client module requires you to enable the XML module.");
    }
  }

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException, EnunciateException {
    File generateDir = getGenerateDir();

    boolean upToDate = getEnunciate().isUpToDateWithSources(generateDir);
    if (!upToDate) {
      //load the references to the templates....
      URL enumTypeTemplate = getTemplateURL("json-enum-type.fmt");
      URL simpleTypeTemplate = getTemplateURL("json-simple-type.fmt");
      URL complexTypeTemplate = getTemplateURL("json-complex-type.fmt");

      //set up the model, first allowing for jdk 14 compatibility.
      EnunciateFreemarkerModel model = getModel();
      Map<String, String> conversions = getClientPackageConversions();
      ClientClassnameForMethod classnameFor = new ClientClassnameForMethod(conversions);
      classnameFor.setJdk15(true);
      model.put("packageFor", new ClientPackageForMethod(conversions));
      model.put("classnameFor", classnameFor);
      model.put("simpleNameFor", new SimpleNameWithParamsMethod(classnameFor));

      debug("Generating the JSON client classes...");
      model.setFileOutputDirectory(generateDir);

      Set<String> seeAlsos = new TreeSet<String>();

      model.put("seeAlsoBeans", seeAlsos);

      AntPatternMatcher matcher = new AntPatternMatcher();
      matcher.setPathSeparator(".");

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
      info("Skipping generation of JSON Client sources as everything appears up-to-date...");
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
      String clientClasspath = enunciate.getEnunciateBuildClasspath(); //we use the build classpath for client-side jars so you don't have to include client-side dependencies on the server-side.
      enunciate.invokeJavac(clientClasspath, "1.5", compileDir, new ArrayList<String>(), jdk15Files.toArray(new String[jdk15Files.size()]));

      for (DeploymentModule module : enunciate.getConfig().getEnabledModules()) {
        if (module instanceof XMLDeploymentModule) {
          XMLDeploymentModule xmlModule = (XMLDeploymentModule) module;
          enunciate.copyDir(xmlModule.getGenerateDir(), compileDir);
        }
      }
    }
    else {
      info("Skipping compilation of JSON client classes as everything appears up-to-date...");
    }
  }

  @Override
  protected void doBuild() throws EnunciateException, IOException {
    Enunciate enunciate = getEnunciate();
    String jarName = getJarName();

    if (jarName == null) {
      String label = "json";
      if (getLabel() != null) {
        label = getLabel();
      }
      else if ((enunciate.getConfig() != null) && (enunciate.getConfig().getLabel() != null)) {
        label = enunciate.getConfig().getLabel();
      }

      jarName = label + "-client.jar";
    }

    File jdk15Jar = new File(getBuildDir(), jarName);
    if (!enunciate.isUpToDate(getCompileDir(), jdk15Jar)) {
      enunciate.zip(jdk15Jar, getCompileDir());
      enunciate.setProperty("json.client.jar", jdk15Jar);
    }
    else {
      info("Skipping creation of JSON client jar as everything appears up-to-date...");
    }

    File jdk15Sources = new File(getBuildDir(), jarName.replaceFirst("\\.jar", "-sources.jar"));
    if (!enunciate.isUpToDate(getGenerateDir(), jdk15Sources)) {
      enunciate.zip(jdk15Sources, getGenerateDir());
      enunciate.setProperty("json.client.sources", jdk15Sources);
    }
    else {
      info("Skipping creation of the JSON client source jar as everything appears up-to-date...");
    }

    //todo: generate the javadocs?

    ClientLibraryArtifact artifactBundle = new ClientLibraryArtifact(getName(), "json.client.library", "JSON Client Library (Java 5+)");
    artifactBundle.setPlatform("Java (Version 5+)");
    //read in the description from file:
    artifactBundle.setDescription(readResource("library_description.fmt"));
    NamedFileArtifact binariesJar = new NamedFileArtifact(getName(), "json.client.library.binaries", jdk15Jar);
    binariesJar.setDescription("The binaries for the JSON client library.");
    binariesJar.setPublic(false);
    binariesJar.setArtifactType(ArtifactType.binaries);
    artifactBundle.addArtifact(binariesJar);
    NamedFileArtifact sourcesJar = new NamedFileArtifact(getName(), "json.client.library.sources", jdk15Sources);
    sourcesJar.setDescription("The sources for the JSON client library.");
    sourcesJar.setPublic(false);
    sourcesJar.setArtifactType(ArtifactType.sources);
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
  protected String readResource(String resource) throws IOException, EnunciateException {
    HashMap<String, Object> model = new HashMap<String, Object>();
    model.put("sample_service_method", getModelInternal().findExampleWebMethod());
    model.put("sample_resource", getModelInternal().findExampleResource());

    URL res = JacksonClientDeploymentModule.class.getResource(resource);
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
    return JacksonClientDeploymentModule.class.getResource(template);
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
   * An JSON configuration rule set.
   *
   * @return An JSON configuration rule set.
   */
  @Override
  public RuleSet getConfigurationRules() {
    return this.configurationRules;
  }

  /**
   * An json-client validator.
   *
   * @return An json-client validator.
   */
  @Override
  public Validator getValidator() {
    return new JacksonClientValidator(getServerSideTypesToUse(), getClientPackageConversions());
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
  public void addClientPackageConversion(org.codehaus.enunciate.modules.jackson_client.config.ClientPackageConversion conversion) {
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

  /**
   * The label for the JSON Client API.
   *
   * @return The label for the  JSON Client API.
   */
  public String getLabel() {
    return label;
  }

  /**
   * The label for the  JSON Client API.
   *
   * @param label The label for the  JSON Client API.
   */
  public void setLabel(String label) {
    this.label = label;
  }

  // Inherited.

  @Override
  public boolean isDisabled() {
    if (super.isDisabled()) {
      return true;
    }
    else if (getModelInternal() != null && getModelInternal().getNamespacesToWSDLs().isEmpty() && getModelInternal().getNamespacesToSchemas().isEmpty()) {
      debug("JSON client module is disabled because there are no endpoint interfaces, nor any XML types.");
      return true;
    }

    return false;
  }

  public List<File> getProjectSources() {
    return Collections.emptyList();
  }

  public List<File> getProjectTestSources() {
    return Arrays.asList(getGenerateDir());
  }

  public List<File> getProjectResourceDirectories() {
    return Collections.emptyList();
  }

  public List<File> getProjectTestResourceDirectories() {
    ArrayList<File> testResources = new ArrayList<File>();

    //the json-client requires the wsdl and schemas on the classpath.
    for (DeploymentModule enabledModule : getEnunciate().getConfig().getEnabledModules()) {
      if (enabledModule instanceof XMLDeploymentModule) {
        testResources.add(((XMLDeploymentModule) enabledModule).getGenerateDir());
      }
    }

    return testResources;
  }
}