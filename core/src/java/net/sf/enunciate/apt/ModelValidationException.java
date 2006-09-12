package net.sf.enunciate.apt;

import freemarker.template.TemplateModelException;

/**
 * Throws when finding validation errors when attempting to create a model.
 *
 * @author Ryan Heaton
 */
public class ModelValidationException extends TemplateModelException {

  public ModelValidationException() {
    super("There were validation errors");
  }

}
