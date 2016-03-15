package com.webcohesion.enunciate.module;

import com.webcohesion.enunciate.Enunciate;
import com.webcohesion.enunciate.EnunciateContext;
import org.apache.commons.configuration.HierarchicalConfiguration;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public abstract class BasicEnunicateModule implements EnunciateModule, DependingModuleAwareModule {

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
        debug("%s relativized to %s.", filePath, downloadFile.getAbsolutePath());
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

  protected void error(String message, Object... formatArgs) {
    this.enunciate.getLogger().error(message, formatArgs);
  }

  protected String positionOf(javax.lang.model.element.Element element) {
    StringBuilder position = new StringBuilder(descriptionOf(element));

    javax.lang.model.element.Element enclosing = element.getEnclosingElement();
    while (enclosing != null && !(enclosing instanceof PackageElement)) {
      position.append(" of ").append(enclosing.getSimpleName().toString());
      enclosing = enclosing.getEnclosingElement();
    }

    //capitalize it.
    return capitalize(position.toString());
  }

  protected String capitalize(String position) {
    return Character.toUpperCase(position.charAt(0)) + position.substring(1);
  }

  protected String descriptionOf(javax.lang.model.element.Element element) {
    StringBuilder description = new StringBuilder();
    ElementKind kind = element.getKind();
    if (kind != null) {
      description.append(kind.name()).append(' ');
    }

    Name name = element.getSimpleName();
    if (element instanceof TypeElement) {
      name = ((TypeElement) element).getQualifiedName();
    }
    description.append(name.toString());
    return description.toString();
  }
}
