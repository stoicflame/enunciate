package com.webcohesion.enunciate.module;

import com.webcohesion.enunciate.Enunciate;
import com.webcohesion.enunciate.EnunciateContext;
import org.apache.commons.configuration.HierarchicalConfiguration;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public abstract class BasicEnunicateModule implements EnunciateModule, DependingModuleAware {

  protected Set<String> dependingModules = null;
  protected Enunciate enunciate;
  protected EnunciateContext context;
  protected HierarchicalConfiguration config;

  @Override
  public void init(Enunciate engine) {
    this.enunciate = engine;
    this.config = (HierarchicalConfiguration) this.enunciate.getConfiguration().getSource().subset("modules." + getName());
  }

  @Override
  public void init(EnunciateContext context) {
    this.context = context;
  }

  @Override
  public List<DependencySpec> getDependencySpecifications() {
    return Collections.emptyList();
  }

  @Override
  public boolean isEnabled() {
    return !this.config.getBoolean("[@disabled]", false);
  }

  public File resolveFile(String filePath) {
    if (File.separatorChar != '/') {
      filePath = filePath.replace('/', File.separatorChar); //normalize on the forward slash...
    }

    File downloadFile = new File(filePath);

    if (!downloadFile.isAbsolute()) {
      //try to relativize this download file to the directory of the config file.
      File configFile = this.enunciate.getConfiguration().getSource().getFile();
      if (configFile != null) {
        downloadFile = new File(configFile.getAbsoluteFile().getParentFile(), filePath);
        debug("%s relatived to %s.", filePath, downloadFile.getAbsolutePath());
      }
    }
    return downloadFile;
  }

  @Override
  public void acknowledgeDependingModules(Set<String> dependingModules) {
    this.dependingModules = dependingModules;
  }

  protected void debug(String message, Object... formatArgs) {
    this.enunciate.getLogger().debug(message, formatArgs);
  }

  protected void info(String message, Object... formatArgs) {
    this.enunciate.getLogger().info(message, formatArgs);
  }

  protected void warn(String message, Object... formatArgs) {
    this.enunciate.getLogger().warn(message, formatArgs);
  }

}
