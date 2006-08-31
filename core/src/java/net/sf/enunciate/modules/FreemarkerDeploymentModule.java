package net.sf.enunciate.modules;

import freemarker.cache.URLTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
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
      doFreemarkerGenerate();
    }
    catch (TemplateException e) {
      throw new IOException(e.getMessage());
    }
  }

  /**
   * Generate using Freemarker.  Same as {@link #doGenerate} but can throw a TemplateException.
   */
  public abstract void doFreemarkerGenerate() throws IOException, TemplateException;

  /**
   * Processes the specified template with the given model.
   *
   * @param templateURL The template URL.
   * @param model       The root model.
   */
  protected void processTemplate(URL templateURL, Object model) throws IOException, TemplateException {
    Configuration configuration = getConfiguration();
    configuration.setDefaultEncoding("UTF-8");
    Template template = configuration.getTemplate(templateURL.toString());
    processTemplate(template, model);
  }

  /**
   * Processes the specified template with the given model.
   *
   * @param template The template.
   * @param model    The root model.
   */
  protected void processTemplate(Template template, Object model) throws TemplateException, IOException {
    template.process(model, new OutputStreamWriter(System.out));
  }

  /**
   * Gets the model for processing.
   *
   * @return The model for processing.
   */
  protected EnunciateFreemarkerModel getModel() throws IOException {
    EnunciateFreemarkerModel model = (EnunciateFreemarkerModel) FreemarkerModel.get();

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

}
