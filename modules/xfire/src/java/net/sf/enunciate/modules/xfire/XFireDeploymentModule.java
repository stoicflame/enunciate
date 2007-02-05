package net.sf.enunciate.modules.xfire;

import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import freemarker.template.TemplateException;
import net.sf.enunciate.EnunciateException;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.config.WsdlInfo;
import net.sf.enunciate.contract.jaxws.*;
import net.sf.enunciate.contract.validation.Validator;
import net.sf.enunciate.main.Artifact;
import net.sf.enunciate.main.Enunciate;
import net.sf.enunciate.main.FileArtifact;
import net.sf.enunciate.modules.DeploymentModule;
import net.sf.enunciate.modules.FreemarkerDeploymentModule;
import net.sf.enunciate.modules.xfire.config.WarConfig;
import net.sf.enunciate.modules.xfire.config.XFireRuleSet;
import net.sf.enunciate.modules.xfire.config.SpringImport;
import org.apache.commons.digester.RuleSet;
import sun.misc.Service;

import javax.servlet.ServletContext;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Deployment module for XFire.
 *
 * @author Ryan Heaton
 */
public class XFireDeploymentModule extends FreemarkerDeploymentModule {

  private WarConfig warConfig;
  private final List<SpringImport> springImports = new ArrayList<SpringImport>();
  private boolean compileDebugInfo = true;

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
  protected URL getSpringServletTemplateURL() {
    return XFireDeploymentModule.class.getResource("spring-servlet.fmt");
  }

  /**
   * @return The URL to "rpc-request-bean.fmt"
   */
  protected URL getRPCRequestBeanTemplateURL() {
    return XFireDeploymentModule.class.getResource("rpc-request-bean.fmt");
  }

  /**
   * @return The URL to "rpc-response-bean.fmt"
   */
  protected URL getRPCResponseBeanTemplateURL() {
    return XFireDeploymentModule.class.getResource("rpc-response-bean.fmt");
  }

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException {
    EnunciateFreemarkerModel model = getModel();

    //generate the xfire-servlet.xml
    model.setFileOutputDirectory(getXMLGenerateDir());
    model.put("springImports", getSpringImportURIs());
    processTemplate(getSpringServletTemplateURL(), model);

    //generate the rpc request/response beans.
    model.setFileOutputDirectory(getJAXWSGenerateDir());
    for (WsdlInfo wsdlInfo : model.getNamespacesToWSDLs().values()) {
      for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
        for (WebMethod webMethod : ei.getWebMethods()) {
          for (WebMessage webMessage : webMethod.getMessages()) {
            if (webMessage instanceof RPCInputMessage) {
              model.put("message", webMessage);
              processTemplate(getRPCRequestBeanTemplateURL(), model);
            }
            else if (webMessage instanceof RPCOutputMessage) {
              model.put("message", webMessage);
              processTemplate(getRPCResponseBeanTemplateURL(), model);
            }
          }
        }
      }
    }
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

    Collection<String> jaxwsSourceFiles = new ArrayList<String>(enunciate.getJavaFiles(jaxwsSources));
    //make sure we include all the wrappers generated for the rpc methods, too...
    jaxwsSourceFiles.addAll(enunciate.getJavaFiles(getJAXWSGenerateDir()));
    StringBuilder jaxwsClasspath = new StringBuilder(enunciate.getDefaultClasspath());
    jaxwsClasspath.append(File.pathSeparator).append(getCompileDir().getAbsolutePath());
    enunciate.invokeJavac(jaxwsClasspath.toString(), getCompileDir(), javacAdditionalArgs, jaxwsSourceFiles.toArray(new String[jaxwsSourceFiles.size()]));
  }

  @Override
  protected void doBuild() throws IOException, EnunciateException {
    Enunciate enunciate = getEnunciate();
    File buildDir = getBuildDir();
    File webinf = new File(buildDir, "WEB-INF");
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

    //todo: assert that the necessary jars (spring, xfire, commons-whatever, etc.) are there?

    //put the web.xml in WEB-INF.  Pass it through a stylesheet, if specified.
    URL webXML = getClass().getResource("/net/sf/enunciate/modules/xfire/web.xml");
    if ((this.warConfig != null) && (this.warConfig.getWebXMLTransformURL() != null)) {
      URL transformURL = this.warConfig.getWebXMLTransformURL();
      try {
        StreamSource source = new StreamSource(transformURL.openStream());
        Transformer transformer = new TransformerFactoryImpl().newTransformer(source);
        transformer.transform(new StreamSource(webXML.openStream()), new StreamResult(new File(webinf, "web.xml")));
      }
      catch (TransformerException e) {
        throw new EnunciateException("Error during transformation of the web.xml (stylesheet " + transformURL + ", file " + webXML + ")", e);
      }
    }
    else {
      enunciate.copyResource(webXML, new File(webinf, "web.xml"));
    }

    //copy the spring servlet config from the build dir to the WEB-INF directory.
    File xfireConfigDir = getXMLGenerateDir();
    enunciate.copyFile(new File(xfireConfigDir, "spring-servlet.xml"), new File(webinf, "spring-servlet.xml"));
    for (SpringImport springImport : springImports) {
      //copy the extra spring import files to the WEB-INF directory to be imported.
      File importFile = springImport.getFile();
      if (importFile != null) {
        enunciate.copyFile(importFile, new File(webinf, importFile.getName()));
      }
    }

    //now try to find the documentation and export it to the build directory...
    Artifact artifact = enunciate.findArtifact("docs");
    if (artifact != null) {
      artifact.exportTo(buildDir, enunciate);
    }
    else {
      System.out.println("WARNING: No documentation artifact found!");
    }

    //export the unexpanded application directory.
    enunciate.addArtifact(new FileArtifact(getName(), "xfire.webapp", buildDir));
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
    enunciate.addArtifact(new FileArtifact(getName(), "xfire.war", warFile));
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
    String filename = "enunciate.war";
    if (getEnunciate().getConfig().getLabel() != null) {
      filename = getEnunciate().getConfig().getLabel() + ".war";
    }
    
    if ((this.warConfig != null) && (this.warConfig.getName() != null)) {
      filename = this.warConfig.getName();
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
   * Get the string form of the spring imports that have been configured.
   *
   * @return The string form of the spring imports that have been configured.
   */
  protected ArrayList<String> getSpringImportURIs() {
    ArrayList<String> springImportURIs = new ArrayList<String>(this.springImports.size());
    for (SpringImport springImport : springImports) {
      if (springImport.getFile() != null) {
        if (springImport.getUri() != null) {
          throw new IllegalStateException("A spring import configuration must specify a file or a URI, but not both.");
        }

        springImportURIs.add(springImport.getFile().getName());
      }
      else if (springImport.getUri() != null) {
        springImportURIs.add(springImport.getUri());
      }
      else {
        throw new IllegalStateException("A spring import configuration must specify either a file or a URI.");
      }
    }
    return springImportURIs;
  }

  /**
   * Add a spring import.
   *
   * @param springImports The spring import to add.
   */
  public void addSpringImport(SpringImport springImports) {
    this.springImports.add(springImports);
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
    if (loader.findResource("META-INF/enunciate/preserve-in-war") != null) {
      //if a jar happens to have the enunciate "preserve-in-war" file, it is NOT excluded.
      return false;
    }
    else if (loader.findResource(com.sun.tools.apt.Main.class.getName().replace('.', '/').concat(".class")) != null) {
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
    else if (loader.findResource("net/sf/enunciate/modules/xfire_client/EnunciatedClientSoapSerializerHandler.class") != null) {
      //exclude xfire-client-tools
      return true;
    }
    else if (Service.providers(DeploymentModule.class, loader).hasNext()) {
      //exclude by default any deployment module libraries.
      return true;
    }

    return false;
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
   * @return 200
   */
  @Override
  public int getOrder() {
    return 200;
  }

  @Override
  public RuleSet getConfigurationRules() {
    return new XFireRuleSet();
  }

  @Override
  public Validator getValidator() {
    return new XFireValidator();
  }

  /**
   * The directory where the RPC request/response beans are generated.
   *
   * @return The directory where the RPC request/response beans are generated.
   */
  protected File getJAXWSGenerateDir() {
    return new File(getGenerateDir(), "jaxws");
  }

  /**
   * The directory where the servlet config file is generated.
   *
   * @return The directory where the servlet config file is generated.
   */
  protected File getXMLGenerateDir() {
    return new File(getGenerateDir(), "xml");
  }

}
