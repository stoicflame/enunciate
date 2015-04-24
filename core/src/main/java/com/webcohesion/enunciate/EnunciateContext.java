package com.webcohesion.enunciate;

import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Encapsulation of the output of the Enunciate engine.
 *
 * @author Ryan Heaton
 */
public class EnunciateContext {

  private final EnunciateConfiguration configuration;
  private final EnunciateLogger logger;
  private final DecoratedProcessingEnvironment processingEnvironment;
  private Set<Element> apiElements;
  private final Map<String, Object> properties = new ConcurrentHashMap<String, Object>();

  public EnunciateContext(EnunciateConfiguration configuration, EnunciateLogger logger, Set<String> includedTypes, DecoratedProcessingEnvironment processingEnvironment) {
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

  public DecoratedProcessingEnvironment getProcessingEnvironment() {
    return processingEnvironment;
  }

  public Set<Element> getApiElements() {
    return apiElements;
  }

  void setApiElements(Set<Element> apiElements) {
    this.apiElements = apiElements;
  }

  public <P> P getProperty(String key, Class<P> type) {
    return type.cast(getProperty(key));
  }

  public Object getProperty(String key) {
    return this.properties.get(key);
  }

  public void setProperty(String key, Object value) {
    this.properties.put(key, value);
  }

}
