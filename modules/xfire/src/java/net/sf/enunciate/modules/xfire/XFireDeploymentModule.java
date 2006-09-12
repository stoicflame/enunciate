package net.sf.enunciate.modules.xfire;

import freemarker.template.TemplateException;
import net.sf.enunciate.EnunciateException;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.main.Enunciate;
import net.sf.enunciate.modules.FreemarkerDeploymentModule;
import net.sf.enunciate.modules.xfire.config.WarConfig;
import net.sf.enunciate.modules.xml.XMLAPILookup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

/**
 * Deployment module for XFire.
 *
 * @author Ryan Heaton
 */
public class XFireDeploymentModule extends FreemarkerDeploymentModule {

  private WarConfig warConfig;

  /**
   * @return "xfire"
   */
  @Override
  public String getName() {
    return "xfire";
  }

  /**
   * @return "http://enunciate.sf.net"
   */
  @Override
  public String getNamespace() {
    return "http://enunciate.sf.net";
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
    processTemplate(getXFireServletTemplateURL(), model);
  }

  @Override
  protected void doCompile() throws EnunciateException, IOException {
    Enunciate enunciate = getEnunciate();
    enunciate.invokeJavac(getCompileDir(), enunciate.getSourceFiles());
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

    //todo: copy any additional jars (specified in the config)?

    //todo: assert that the necessary jars (spring, xfire, commons-whatever, etc.) are there?

    //copy the web.xml file to WEB-INF.
    enunciate.copyResource("/net/sf/enunciate/modules/xfire/web.xml", new File(webinf, "web.xml"));

    //copy the xfire config file from the xfire configuration directory to the WEB-INF directory.
    File xfireConfigDir = new File(new File(enunciate.getGenerateDir(), "xfire"), "xml");
    enunciate.copyFile(new File(xfireConfigDir, "xfire-servlet.xml"), new File(webinf, "xfire-servlet.xml"));

    File xmlDir = (File) enunciate.getProperty("xml.dir");
    if (xmlDir != null) {
      //if the xml deployment module has been run, copy all generated xml files to the WEB-INF/classes directory.
      enunciate.copyDir(xmlDir, webinfClasses);
    }

    XMLAPILookup lookup = (XMLAPILookup) enunciate.getProperty(XMLAPILookup.class.getName());
    if (lookup != null) {
      //store the lookup, if it exists.
      FileOutputStream out = new FileOutputStream(new File(webinfClasses, "xml-api.lookup"));
      lookup.store(out);
      out.close();
    }
    else {
      System.err.println("ERROR: No lookup was generated!  The contoller used to serve up the WSDLs and schemas will not function!");
    }
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
    if (!getWarLibs().isEmpty()) {
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

    return false;
  }

  /**
   * @return 10
   */
  @Override
  public int getOrder() {
    return 10;
  }

}
