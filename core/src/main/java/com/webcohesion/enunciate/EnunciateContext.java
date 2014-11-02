package com.webcohesion.enunciate;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.util.Set;

/**
 * Encapsulation of the output of the Enunciate engine.
 *
 * @author Ryan Heaton
 */
public class EnunciateContext {

  private final EnunciateConfiguration configuration;
  private final EnunciateLogger logger;
  private final ProcessingEnvironment processingEnvironment;
  private Set<Element> apiElements;

  public EnunciateContext(EnunciateConfiguration configuration, EnunciateLogger logger, Set<String> includedTypes, ProcessingEnvironment processingEnvironment) {
    this.configuration = configuration;
    this.logger = logger;
    this.processingEnvironment = processingEnvironment;
  }

  public EnunciateConfiguration getConfiguration() {
    return configuration;
  }

  public EnunciateLogger getLogger() {
    return logger;
  }

  public ProcessingEnvironment getProcessingEnvironment() {
    return processingEnvironment;
  }

  public Set<Element> getApiElements() {
    return apiElements;
  }

  public void setApiElements(Set<Element> apiElements) {
    this.apiElements = apiElements;
  }

}
