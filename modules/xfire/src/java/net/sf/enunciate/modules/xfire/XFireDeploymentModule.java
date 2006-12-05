package net.sf.enunciate.modules.xfire;

import com.sun.mirror.declaration.ParameterDeclaration;
import freemarker.template.TemplateException;
import net.sf.enunciate.EnunciateException;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.config.WsdlInfo;
import net.sf.enunciate.contract.jaxws.EndpointInterface;
import net.sf.enunciate.contract.jaxws.WebMethod;
import net.sf.enunciate.contract.validation.Validator;
import net.sf.enunciate.main.Enunciate;
import net.sf.enunciate.modules.FreemarkerDeploymentModule;
import net.sf.enunciate.modules.DeploymentModule;
import net.sf.enunciate.modules.xfire.config.WarConfig;
import net.sf.enunciate.modules.xfire.config.XFireRuleSet;
import org.apache.commons.digester.RuleSet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import sun.misc.Service;

import javax.servlet.ServletContext;

/**
 * Deployment module for XFire.
 *
 * @author Ryan Heaton
 */
public class XFireDeploymentModule extends FreemarkerDeploymentModule {

  private WarConfig warConfig;
  private String uuid;
  private boolean compileDebugInfo = true;

  public XFireDeploymentModule() {
    this.uuid = String.valueOf(System.currentTimeMillis());
  }

  /**
   * @return "xfire"
   */
  @Override
  public String getName() {
    return "xfire";
  }

  /**
   * @return The URL to "xfire-servlet.fmt"
   */
  protected URL getXFireServletTemplateURL() {
    return XFireDeploymentModule.class.getResource("xfire-servlet.fmt");
  }

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException {
    EnunciateFreemarkerModel model = getModel();

    //generate the xfire-servlet.xml
    model.put("uuid", this.uuid);
    processTemplate(getXFireServletTemplateURL(), model);

    HashMap<String, String[]> parameterNames = new HashMap<String, String[]>();
    for (WsdlInfo wsdlInfo : model.getNamespacesToWSDLs().values()) {
      for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
        for (WebMethod webMethod : ei.getWebMethods()) {
          Collection<ParameterDeclaration> paramList = webMethod.getParameters();
          ParameterDeclaration[] parameters = paramList.toArray(new ParameterDeclaration[paramList.size()]);
          String[] paramNames = new String[parameters.length];
          for (int i = 0; i < parameters.length; i++) {
            ParameterDeclaration declaration = parameters[i];
            paramNames[i] = declaration.getSimpleName();
          }

          parameterNames.put(ei.getQualifiedName() + "." + webMethod.getSimpleName(), paramNames);
        }
      }
    }

    Enunciate enunciate = getEnunciate();
    File genDir = new File(enunciate.getGenerateDir(), "xfire");
    genDir.mkdirs();
    File propertyNamesFile = new File(genDir, this.uuid + ".property.names");
    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(propertyNamesFile));
    oos.writeObject(parameterNames);
    oos.flush();
    oos.close();

    enunciate.setProperty("property.names.file", propertyNamesFile);
  }

  @Override
  protected void doCompile() throws EnunciateException, IOException {
    ArrayList<String> javacAdditionalArgs = new ArrayList<String>();
    if (compileDebugInfo) {
      javacAdditionalArgs.add("-g");
    }

    Enunciate enunciate = getEnunciate();
    enunciate.invokeJavac(enunciate.getDefaultClasspath(), getCompileDir(), javacAdditionalArgs, enunciate.getSourceFiles());

    File jaxwsSources = (File) enunciate.getProperty("jaxws.src.dir");
    if (jaxwsSources == null) {
      throw new EnunciateException("Required dependency on the JAXWS module was not found.  The generated request/response/fault beans are required.");
    }

    Collection<String> jaxwsSourceFiles = enunciate.getJavaFiles(jaxwsSources);
    StringBuilder jaxwsClasspath = new StringBuilder(enunciate.getDefaultClasspath());
    jaxwsClasspath.append(File.pathSeparator).append(getCompileDir().getAbsolutePath());
    enunciate.invokeJavac(jaxwsClasspath.toString(), getCompileDir(), javacAdditionalArgs, jaxwsSourceFiles.toArray(new String[jaxwsSourceFiles.size()]));

    File propertyNamesFile = (File) enunciate.getProperty("property.names.file");
    if (propertyNamesFile == null) {
      throw new EnunciateException("No generated property names file was found.");
    }
    enunciate.copyFile(propertyNamesFile, propertyNamesFile.getParentFile(), getCompileDir());
  }

  @Override
  protected void doBuild() throws IOException {
    Enunciate enunciate = getEnunciate();
    File webinf = getWebInf();
    File webinfClasses = new File(webinf, "classes");
    File webinfLib = new File(webinf, "lib");

    //copy the compiled classes to WEB-INF/classes.
    enunciate.copyDir(getCompileDir(), webinfClasses);

    List<String> warLibs = getWarLibs();
    if ((warLibs == null) || (warLibs.isEmpty())) {
      String classpath = enunciate.getClasspath();
      if (classpath == null) {
        classpath = System.getProperty("java.class.path");
      }
      warLibs = Arrays.asList(classpath.split(File.pathSeparator));
    }

    for (String pathEntry : warLibs) {
      File file = new File(pathEntry);
      if (file.exists()) {
        if (file.isDirectory()) {
          if (enunciate.isVerbose()) {
            System.out.println("Adding the contents of " + file.getAbsolutePath() + " to WEB-INF/classes.");
          }
          enunciate.copyDir(file, webinfClasses);
        }
        else if (!excludeLibrary(file)) {
          if (enunciate.isVerbose()) {
            System.out.println("Including " + file.getName() + " in WEB-INF/lib.");
          }
          enunciate.copyFile(file, file.getParentFile(), webinfLib);
        }
      }
    }

    //todo: copy any additional files as resources (specified in the config);

    //todo: assert that the necessary jars (spring, xfire, commons-whatever, etc.) are there?

    //copy the web.xml file to WEB-INF.
    //todo: merge the specified web.xml with another specified in the config?
    enunciate.copyResource("/net/sf/enunciate/modules/xfire/web.xml", new File(webinf, "web.xml"));

    //copy the xfire config file from the xfire configuration directory to the WEB-INF directory.
    File xfireConfigDir = new File(new File(enunciate.getGenerateDir(), "xfire"), "xml");
    enunciate.copyFile(new File(xfireConfigDir, "xfire-servlet.xml"), new File(webinf, "xfire-servlet.xml"));

    HashSet<File> xmlArtifacts = new HashSet<File>();
    HashMap<String, String> ns2schemaResource = new HashMap<String, String>();
    HashMap<String, File> ns2schema = (HashMap<String, File>) enunciate.getProperty("xml.ns2schema");
    if (ns2schema != null) {
      for (String ns : ns2schema.keySet()) {
        File artifact = ns2schema.get(ns);
        String resourceName = getXmlResourceName(artifact);
        ns2schemaResource.put(ns, resourceName);
        xmlArtifacts.add(artifact);
      }
    }
    else {
      System.err.println("WARNING: No schemas for the namespaces of the project were found.  Schema publication will be disabled.");
    }

    HashMap<String, String> ns2wsdlResource = new HashMap<String, String>();
    HashMap<String, File> ns2wsdl = (HashMap<String, File>) enunciate.getProperty("xml.ns2wsdl");
    if (ns2wsdl != null) {
      for (String ns : ns2wsdl.keySet()) {
        File artifact = ns2wsdl.get(ns);
        String resourceName = getXmlResourceName(artifact);
        ns2wsdlResource.put(ns, resourceName);
        xmlArtifacts.add(artifact);
      }
    }
    else {
      System.err.println("WARNING: No wsdls for the namespaces of the project were found.  WSDL publication will be disabled.");
    }

    HashMap<String, File> service2wsdl = (HashMap<String, File>) enunciate.getProperty("xml.service2wsdl");
    HashMap<String, String> service2WsdlResource = new HashMap<String, String>();
    if (service2wsdl != null) {
      for (String service : service2wsdl.keySet()) {
        File wsdl = service2wsdl.get(service);
        String resourceName = getXmlResourceName(wsdl);
        service2WsdlResource.put(service, resourceName);
        xmlArtifacts.add(wsdl);
      }
    }
    else {
      System.err.println("WARNING: No wsdls for the services of the project were found.  WSDL publication will be disabled.");
    }

    for (File artifact : xmlArtifacts) {
      String resourceName = getXmlResourceName(artifact);
      enunciate.copyFile(artifact, new File(webinfClasses, resourceName));
    }

    XMLAPILookup lookup = new XMLAPILookup(ns2wsdlResource, ns2schemaResource, service2WsdlResource);
    lookup.store(new FileOutputStream(new File(webinfClasses, "xml.lookup")));
  }

  /**
   * Get the name of the resource for the specified xml artifact.
   *
   * @param artifact The artifact.
   * @return The resource name.
   */
  protected String getXmlResourceName(File artifact) {
    //todo: generate a unique id in case artifacts of the name name are put in different directories?
    return artifact.getName();
  }

  @Override
  protected void doPackage() throws EnunciateException, IOException {
    File buildDir = getBuildDir();
    File warFile = getWarFile();

    if (!warFile.getParentFile().exists()) {
      warFile.getParentFile().mkdirs();
    }

    Enunciate enunciate = getEnunciate();
    if (enunciate.isVerbose()) {
      System.out.println("Creating " + warFile.getAbsolutePath());
    }

    enunciate.zip(buildDir, warFile);
  }

  /**
   * The directory for compiling.
   *
   * @return The directory for compiling.
   */
  protected File getCompileDir() {
    return new File(getEnunciate().getCompileDir(), "xfire");
  }

  /**
   * The build directory for this module.
   *
   * @return The build directory for this module.
   */
  protected File getBuildDir() {
    return new File(getEnunciate().getBuildDir(), "xfire");
  }

  /**
   * The package directory for this module.
   *
   * @return The package directory for this module.
   */
  protected File getPackageDir() {
    return new File(getEnunciate().getPackageDir(), "xfire");
  }

  /**
   * The WEB-INF directory for this module.
   *
   * @return The WEB-INF directory for this module.
   */
  protected File getWebInf() {
    return new File(getBuildDir(), "WEB-INF");
  }

  /**
   * Get the list of libraries to include in the war.
   *
   * @return the list of libraries to include in the war.
   */
  public List<String> getWarLibs() {
    if (this.warConfig != null) {
      return this.warConfig.getWarLibs();
    }

    return null;
  }

  /**
   * The war file to create.
   *
   * @return The war file to create.
   */
  public File getWarFile() {
    String filename = "enunciated.war";
    if (this.warConfig != null) {
      if (this.warConfig.getFile() != null) {
        return new File(this.warConfig.getFile());
      }
      else if (this.warConfig.getName() != null) {
        filename = this.warConfig.getName();
      }
    }

    return new File(getPackageDir(), filename);
  }

  /**
   * Set the configuration for the war.
   *
   * @param warConfig The configuration for the war.
   */
  public void setWarConfig(WarConfig warConfig) {
    this.warConfig = warConfig;
  }

  /**
   * Whether to exclude a file from copying to the WEB-INF/lib directory.
   *
   * @param file The file to exclude.
   * @return Whether to exclude a file from copying to the lib directory.
   */
  protected boolean excludeLibrary(File file) throws IOException {
    List<String> warLibs = getWarLibs();
    if ((warLibs != null) && (!warLibs.isEmpty())) {
      //if the war libraries were explicitly declared, don't exclude anything.
      return false;
    }

    //instantiate a loader with this library only in its path...
    URLClassLoader loader = new URLClassLoader(new URL[]{file.toURL()}, null);
    if (loader.findResource(com.sun.tools.apt.Main.class.getName().replace('.', '/').concat(".class")) != null) {
      //exclude tools.jar.
      return true;
    }
    else if (loader.findResource(net.sf.jelly.apt.Context.class.getName().replace('.', '/').concat(".class")) != null) {
      //exclude apt-jelly-core.jar
      return true;
    }
    else if (loader.findResource(net.sf.jelly.apt.freemarker.FreemarkerModel.class.getName().replace('.', '/').concat(".class")) != null) {
      //exclude apt-jelly-freemarker.jar
      return true;
    }
    else if (loader.findResource(freemarker.template.Configuration.class.getName().replace('.', '/').concat(".class")) != null) {
      //exclude freemarker.jar
      return true;
    }
    else if (loader.findResource(Enunciate.class.getName().replace('.', '/').concat(".class")) != null) {
      //exclude enunciate-core.jar
      return true;
    }
    else if (loader.findResource(ServletContext.class.getName().replace('.', '/').concat(".class")) != null) {
      //exclude the servlet api.
      return true;
    }
    else if (Service.providers(DeploymentModule.class, loader).hasNext()) {
      //exclude by default any deployment module libraries.
      return true;
    }

    return false;
  }

  /**
   * A unique id to associate with this build of the xfire module.
   *
   * @return A unique id to associate with this build of the xfire module.
   */
  public String getUuid() {
    return uuid;
  }

  /**
   * A unique id to associate with this build of the xfire module.
   *
   * @param uuid A unique id to associate with this build of the xfire module.
   */
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  /**
   * Configure whether to compile with debug info (default: true).
   *
   * @param compileDebugInfo Whether to compile with debug info (default: true).
   */
  public void setCompileDebugInfo(boolean compileDebugInfo) {
    this.compileDebugInfo = compileDebugInfo;
  }

  /**
   * @return 10
   */
  @Override
  public int getOrder() {
    return 10;
  }

  @Override
  public RuleSet getConfigurationRules() {
    return new XFireRuleSet();
  }

  @Override
  public Validator getValidator() {
    return new XFireValidator();
  }

}
