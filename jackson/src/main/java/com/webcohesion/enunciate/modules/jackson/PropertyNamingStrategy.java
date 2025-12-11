package com.webcohesion.enunciate.modules.jackson;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;

public enum PropertyNamingStrategy {

  /**
   * Naming convention used in Java, where words other than first are capitalized
   * and no separator is used between words. Since this is the native Java naming convention,
   * naming strategy will not do any transformation between names in data (JSON) and
   * POJOS.
   *<p>
   * Example external property names would be "numberValue", "namingStrategy", "theDefiniteProof".
   */
  LOWER_CAMEL_CASE((PropertyNamingStrategies.NamingBase) PropertyNamingStrategies.LOWER_CAMEL_CASE),

  /**
   * Naming convention used in languages like Pascal, where all words are capitalized
   * and no separator is used between words.
   * See {@link PropertyNamingStrategies.UpperCamelCaseStrategy} for details.
   * <p>
   * Example external property names would be "NumberValue", "NamingStrategy", "TheDefiniteProof".
   */
  UPPER_CAMEL_CASE((PropertyNamingStrategies.NamingBase) PropertyNamingStrategies.UPPER_CAMEL_CASE),

  /**
   * Naming convention used in languages like C, where words are in lower-case
   * letters, separated by underscores.
   * See {@link PropertyNamingStrategies.SnakeCaseStrategy} for details.
   * <p>
   * Example external property names would be "number_value", "naming_strategy", "the_definite_proof".
   */
  SNAKE_CASE((PropertyNamingStrategies.NamingBase) PropertyNamingStrategies.SNAKE_CASE),

  /**
   * Naming convention in which the words are in upper-case letters, separated by underscores.
   * See {@link PropertyNamingStrategies.UpperSnakeCaseStrategy} for details.
   *
   * @since 2.13
   * <p>
   */
  UPPER_SNAKE_CASE((PropertyNamingStrategies.NamingBase) PropertyNamingStrategies.UPPER_SNAKE_CASE),

  /**
   * Naming convention in which all words of the logical name are in lower case, and
   * no separator is used between words.
   * See {@link PropertyNamingStrategies.LowerCaseStrategy} for details.
   * <p>
   * Example external property names would be "numbervalue", "namingstrategy", "thedefiniteproof".
   */
  LOWER_CASE((PropertyNamingStrategies.NamingBase) PropertyNamingStrategies.LOWER_CASE),

  /**
   * Naming convention used in languages like Lisp, where words are in lower-case
   * letters, separated by hyphens.
   * See {@link PropertyNamingStrategies.KebabCaseStrategy} for details.
   * <p>
   * Example external property names would be "number-value", "naming-strategy", "the-definite-proof".
   */
  KEBAB_CASE((PropertyNamingStrategies.NamingBase) PropertyNamingStrategies.KEBAB_CASE),

  /**
   * Naming convention widely used as configuration properties name, where words are in
   * lower-case letters, separated by dots.
   * See {@link PropertyNamingStrategies.LowerDotCaseStrategy} for details.
   * <p>
   * Example external property names would be "number.value", "naming.strategy", "the.definite.proof".
   */
  LOWER_DOT_CASE((PropertyNamingStrategies.NamingBase) PropertyNamingStrategies.LOWER_DOT_CASE);

  private final PropertyNamingStrategies.NamingBase strategy;

  PropertyNamingStrategy(PropertyNamingStrategies.NamingBase strategy) {
    this.strategy = strategy;
  }

  public String translate(String input) {
    return strategy.translate(input);
  }
}
