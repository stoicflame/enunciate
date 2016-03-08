package com.webcohesion.enunciate;

import com.webcohesion.enunciate.api.ApiRegistry;
import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.DecoratedRoundEnvironment;
import com.webcohesion.enunciate.util.AntPatternInclude;
import com.webcohesion.enunciate.util.AntPatternMatcher;
import com.webcohesion.enunciate.util.StringEqualsInclude;
import org.reflections.util.FilterBuilder;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
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
  private final Map<String, Object> properties = new ConcurrentHashMap<String, Object>();
  private final EnunciateLogger logger;
  private final ApiRegistry apiRegistry;
  private final EnunciateConfiguration configuration;
  private Set<Element> apiElements;
  private Set<Element> localApiElements;
  private DecoratedRoundEnvironment roundEnvironment;
  private final FilterBuilder includeFilter;
  private final FilterBuilder excludeFilter;

  public EnunciateContext(DecoratedProcessingEnvironment processingEnvironment, EnunciateLogger logger, ApiRegistry registry, EnunciateConfiguration configuration, Set<String> includes, Set<String> excludes) {
    this.processingEnvironment = processingEnvironment;
    this.logger = logger;
    this.apiRegistry = registry;
    this.configuration = configuration;
    this.includeFilter = buildFilter(includes);
    this.excludeFilter = buildFilter(excludes);
  }

  public DecoratedProcessingEnvironment getProcessingEnvironment() {
    return processingEnvironment;
  }

  public DecoratedRoundEnvironment getRoundEnvironment() {
    return roundEnvironment;
  }

  public void setRoundEnvironment(DecoratedRoundEnvironment roundEnvironment) {
    this.roundEnvironment = roundEnvironment;
  }

  public Set<Element> getApiElements() {
    return apiElements;
  }

  void setApiElements(Set<Element> apiElements) {
    this.apiElements = apiElements;
  }

  public Set<Element> getLocalApiElements() {
    return localApiElements;
  }

  void setLocalApiElements(Set<Element> apiElements) {
    this.localApiElements = apiElements;
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

  public ApiRegistry getApiRegistry() {
    return apiRegistry;
  }

  public EnunciateConfiguration getConfiguration() {
    return configuration;
  }

  public boolean isExcluded(Element next) {
    String className = null;
    if (next instanceof TypeElement) {
      className = ((TypeElement) next).getQualifiedName().toString();
    }
    else {
      PackageElement pckg = this.processingEnvironment.getElementUtils().getPackageOf(next);
      if (pckg != null) {
        className = pckg.getQualifiedName().toString();
      }
    }

    boolean filteredIn = this.includeFilter != null && this.includeFilter.apply(className);
    boolean filteredOut = this.excludeFilter != null && this.excludeFilter.apply(className);
    return !filteredIn && filteredOut;
  }

  private FilterBuilder buildFilter(Set<String> includes) {
    FilterBuilder includeFilter = null;
    if (includes != null && !includes.isEmpty()) {
      includeFilter = new FilterBuilder();
      for (String include : includes) {
        if (AntPatternMatcher.isValidPattern(include)) {
          includeFilter = includeFilter.add(new AntPatternInclude(include));
        }
        else {
          includeFilter = includeFilter.add(new StringEqualsInclude(include));
        }
      }
    }
    return includeFilter;
  }
}
