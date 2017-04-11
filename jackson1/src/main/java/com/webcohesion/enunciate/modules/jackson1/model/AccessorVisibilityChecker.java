package com.webcohesion.enunciate.modules.jackson1.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.introspect.VisibilityChecker;

/**
 * Jackson visibility checker settings for each of the accessor methods.
 * It would be nice to use the standard Jackson checker directly
 * ({@link VisibilityChecker.Std}).
 * Unfortunately enough, it does not allow to <em>read</em> values stored in it.
 *
 * @author Martin Kacer
 */
public class AccessorVisibilityChecker {
  
  private final Map<JsonMethod,Visibility> minLevels;
  
  public static final JsonAutoDetect DEFAULT_VISIBILITY = VisibilityChecker.Std.class.getAnnotation(JsonAutoDetect.class);
  public static final AccessorVisibilityChecker DEFAULT_CHECKER = new AccessorVisibilityChecker(DEFAULT_VISIBILITY);

  private AccessorVisibilityChecker(Map<JsonMethod,Visibility> minLevels) {
    this.minLevels = minLevels;
  }
  
  public AccessorVisibilityChecker(JsonAutoDetect annotation) {
    this(createMap(annotation.getterVisibility(), annotation.isGetterVisibility(),
        annotation.setterVisibility(), annotation.creatorVisibility(), annotation.fieldVisibility()));
  }
  
  public AccessorVisibilityChecker with(JsonAutoDetect annotation) {
    return new AccessorVisibilityChecker(createMap(
        (annotation.getterVisibility() == Visibility.DEFAULT ? DEFAULT_VISIBILITY : annotation).getterVisibility(),
        (annotation.isGetterVisibility() == Visibility.DEFAULT ? DEFAULT_VISIBILITY : annotation).isGetterVisibility(),
        (annotation.setterVisibility() == Visibility.DEFAULT ? DEFAULT_VISIBILITY : annotation).setterVisibility(),
        (annotation.creatorVisibility() == Visibility.DEFAULT ? DEFAULT_VISIBILITY : annotation).creatorVisibility(),
        (annotation.fieldVisibility() == Visibility.DEFAULT ? DEFAULT_VISIBILITY : annotation).fieldVisibility()));
  }
  
  public AccessorVisibilityChecker withVisibility(JsonMethod method, Visibility level) {
    return new AccessorVisibilityChecker(changeMap(minLevels, method, level));
  }
  
  /**
   * This should always return a non-null value for "normal" usage and accessor methods.
   */
  public Visibility getVisibility(JsonMethod method) {
    return minLevels.get(method);
  }
  
  private static Map<JsonMethod,Visibility> createMap(Visibility getterLevel, Visibility isGetterLevel,
      Visibility setterLevel, Visibility creatorLevel, Visibility fieldLevel) {
    EnumMap<JsonMethod, Visibility> levels = new EnumMap<JsonMethod, Visibility>(JsonMethod.class);
    levels.put(JsonMethod.GETTER, getterLevel);
    levels.put(JsonMethod.IS_GETTER, isGetterLevel);
    levels.put(JsonMethod.SETTER, setterLevel);
    levels.put(JsonMethod.CREATOR, creatorLevel);
    levels.put(JsonMethod.FIELD, fieldLevel);
    return Collections.unmodifiableMap(levels);
  }
  
  private static Map<JsonMethod,Visibility> changeMap(Map<JsonMethod,Visibility> original,
      JsonMethod method, Visibility level) {
    EnumMap<JsonMethod, Visibility> levels = new EnumMap<JsonMethod, Visibility>(JsonMethod.class);
    if (method == JsonMethod.ALL) {
      return createMap(level, level, level, level, level);
    }
    levels.putAll(original);
    levels.put(method, level);
    return Collections.unmodifiableMap(levels);
  }
}
