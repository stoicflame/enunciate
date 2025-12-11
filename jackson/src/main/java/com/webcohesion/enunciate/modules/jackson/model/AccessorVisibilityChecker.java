package com.webcohesion.enunciate.modules.jackson.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

/**
 * Jackson visibility checker settings for each of the accessor methods.
 * It would be nice to use the standard Jackson checker directly
 * ({@link com.fasterxml.jackson.databind.introspect.VisibilityChecker.Std}).
 * Unfortunately enough, it does not allow to <em>read</em> values stored in it.
 *
 * @author Martin Kacer
 */
@JsonAutoDetect(
   getterVisibility = Visibility.PUBLIC_ONLY,
   isGetterVisibility = Visibility.PUBLIC_ONLY,
   setterVisibility = Visibility.ANY,
   /*
    * By default, all matching single-arg constructed are found,
    * regardless of visibility. Does not apply to factory methods,
    * they can not be auto-detected; ditto for multiple-argument
    * constructors.
   */
   creatorVisibility = Visibility.ANY,
   fieldVisibility = Visibility.PUBLIC_ONLY
)
public class AccessorVisibilityChecker {

  private final Map<PropertyAccessor, Visibility> minLevels;

  public static final JsonAutoDetect DEFAULT_VISIBILITY = AccessorVisibilityChecker.class.getAnnotation(JsonAutoDetect.class);
  public static final AccessorVisibilityChecker DEFAULT_CHECKER = new AccessorVisibilityChecker(DEFAULT_VISIBILITY);

  private AccessorVisibilityChecker(Map<PropertyAccessor, Visibility> minLevels) {
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

  public AccessorVisibilityChecker withVisibility(PropertyAccessor method, Visibility level) {
    return new AccessorVisibilityChecker(changeMap(minLevels, method, level));
  }

  /**
   * This should always return a non-null value for "normal" usage and accessor methods.
   */
  public Visibility getVisibility(PropertyAccessor method) {
    return minLevels.get(method);
  }

  private static Map<PropertyAccessor, Visibility> createMap(Visibility getterLevel, Visibility isGetterLevel,
                                                             Visibility setterLevel, Visibility creatorLevel, Visibility fieldLevel) {
    EnumMap<PropertyAccessor, Visibility> levels = new EnumMap<PropertyAccessor, Visibility>(PropertyAccessor.class);
    levels.put(PropertyAccessor.GETTER, getterLevel);
    levels.put(PropertyAccessor.IS_GETTER, isGetterLevel);
    levels.put(PropertyAccessor.SETTER, setterLevel);
    levels.put(PropertyAccessor.CREATOR, creatorLevel);
    levels.put(PropertyAccessor.FIELD, fieldLevel);
    return Collections.unmodifiableMap(levels);
  }

  private static Map<PropertyAccessor, Visibility> changeMap(Map<PropertyAccessor, Visibility> original,
                                                             PropertyAccessor method, Visibility level) {
    EnumMap<PropertyAccessor, Visibility> levels = new EnumMap<PropertyAccessor, Visibility>(PropertyAccessor.class);
    if (method == PropertyAccessor.ALL) {
      return createMap(level, level, level, level, level);
    }
    levels.putAll(original);
    levels.put(method, level);
    return Collections.unmodifiableMap(levels);
  }
}
