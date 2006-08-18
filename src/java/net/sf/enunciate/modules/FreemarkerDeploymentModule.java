package net.sf.enunciate.modules;

import freemarker.cache.URLTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import net.sf.jelly.apt.freemarker.FreemarkerModel;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Basic module that processes a freemarker template when generating, handling the TemplateException that occurs.
 *
 * @author Ryan Heaton
 */
public abstract class FreemarkerDeploymentModule extends BasicDeploymentModule {

  /**
   * Processes the template.  Declared final because we don't ever want to do more than process
   * the template so it can be safe to call the processTemplate() method directly (e.g. in the
   * {@link net.sf.enunciate.apt.EnunciateAnnotationProcessor}).
   */
  @Override
  protected final void doGenerate() throws IOException {
    try {
      processTemplate();
    }
    catch (TemplateException e) {
      throw new IOException(e.getMessage());
    }
  }

  /**
   * Processes the template.  Assumes that the model is already established.
   */
  public void processTemplate() throws IOException, TemplateException {
    Configuration configuration = getConfiguration();
    configuration.setDefaultEncoding("UTF-8");
    Template template = configuration.getTemplate(getTemplateURL().toString());
    template.process(getModel(), new OutputStreamWriter(System.out));
  }

  /**
   * Gets the model for processing.
   *
   * @return The model for processing.
   */
  protected FreemarkerModel getModel() throws IOException {
    FreemarkerModel model = FreemarkerModel.get();

    if (model == null) {
      throw new IOException("A model must be established.");
    }

    return model;
  }

  /**
   * Get the freemarker configuration.
   *
   * @return the freemarker configuration.
   */
  protected Configuration getConfiguration() {
    Configuration configuration = new Configuration();
    configuration.setTemplateLoader(getTemplateLoader());
    configuration.setLocalizedLookup(false);
    return configuration;
  }

  /**
   * Get the template loader for the freemarker configuration.
   *
   * @return the template loader for the freemarker configuration.
   */
  protected URLTemplateLoader getTemplateLoader() {
    return new URLTemplateLoader() {
      protected URL getURL(String name) {
        try {
          return new URL(name);
        }
        catch (MalformedURLException e) {
          return null;
        }
      }
    };
  }

  /**
   * The URL to the template.
   *
   * @return The URL to the template.
   */
  protected abstract URL getTemplateURL();


}
