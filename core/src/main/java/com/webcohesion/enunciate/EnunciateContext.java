package com.webcohesion.enunciate;

import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;

import javax.lang.model.element.Element;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Encapsulation of the output of the Enunciate engine.
 *
 * @author Ryan Heaton
 */
public class EnunciateContext {

  private final DecoratedProcessingEnvironment processingEnvironment;
  private Set<Element> apiElements;
  private final Map<String, Object> properties = new ConcurrentHashMap<String, Object>();
  private final EnunciateLogger logger;

  public EnunciateContext(DecoratedProcessingEnvironment processingEnvironment, EnunciateLogger logger) {
    this.processingEnvironment = processingEnvironment;
    this.logger = logger;
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

  public EnunciateLogger getLogger() {
    return logger;
  }
}
