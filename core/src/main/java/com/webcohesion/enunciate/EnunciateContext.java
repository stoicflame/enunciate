package com.webcohesion.enunciate;

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
  private final ProcessingEnvironment processingEnvironment;
  private Set<Element> apiElements;
  private final Map<String, Object> properties = new ConcurrentHashMap<String, Object>();

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

  public Types getTypeUtils() {
    return getProcessingEnvironment().getTypeUtils();
  }

  public Elements getElementUtils() {
    return getProcessingEnvironment().getElementUtils();
  }

  public boolean isInstanceOf(TypeMirror type, Class<?> clazz) {
    return isInstanceOf(type, clazz.getName());
  }

  public boolean isInstanceOf(TypeMirror type, String fqn) {
    return isInstanceOf(type, getTypeElement(fqn));
  }

  public TypeElement getTypeElement(CharSequence fqn) {
    return getElementUtils().getTypeElement(fqn);
  }

  public boolean isInstanceOf(TypeMirror type, TypeElement typeElement) {
    return isInstanceOf(type, getTypeUtils().getDeclaredType(typeElement));
  }

  public boolean isInstanceOf(TypeMirror type, TypeMirror superClass) {
    return getTypeUtils().isSubtype(type, superClass);
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
