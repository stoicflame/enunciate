package com.webcohesion.enunciate;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.Set;

/**
 * Encapsulation of the output of the Enunciate engine.
 *
 * @author Ryan Heaton
 */
public class EnunciateContext {

  private final EnunciateConfiguration configuration;
  private final EnunciateLogger logger;
  private final Set<String> includedTypes;
  private final ProcessingEnvironment processingEnvironment;

  public EnunciateContext(EnunciateConfiguration configuration, EnunciateLogger logger, Set<String> includedTypes, ProcessingEnvironment processingEnvironment) {
    this.configuration = configuration;
    this.logger = logger;
    this.includedTypes = includedTypes;
    this.processingEnvironment = processingEnvironment;
  }

  public EnunciateConfiguration getConfiguration() {
    return configuration;
  }

  public EnunciateLogger getLogger() {
    return logger;
  }

  public Set<String> getIncludedTypes() {
    return includedTypes;
  }

  public ProcessingEnvironment getProcessingEnvironment() {
    return processingEnvironment;
  }
}
