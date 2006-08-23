package net.sf.enunciate.template.strategies;

import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.apt.EnunciateAnnotationProcessorFactory;
import net.sf.enunciate.config.SchemaInfo;
import net.sf.enunciate.config.WsdlInfo;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import net.sf.jelly.apt.freemarker.FreemarkerTemplateBlock;
import net.sf.jelly.apt.strategies.TemplateLoopStrategy;
import net.sf.jelly.apt.Context;

import java.util.Map;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;

/**
 * Basic class for enunciate loops.
 *
 * @author Ryan Heaton
 */
public abstract class EnunciateTemplateLoopStrategy<L> extends TemplateLoopStrategy<L, FreemarkerTemplateBlock> {

  /**
   * Convenience method to lookup a namespace prefix given a namespace.
   *
   * @param namespace The namespace for which to lookup the prefix.
   * @return The namespace prefix.
   */
  protected String lookupPrefix(String namespace) {
    return getNamespacesToPrefixes().get(namespace);
  }

  /**
   * Convenience method to lookup a namespace schema given a namespace.
   *
   * @param namespace The namespace for which to lookup the schema.
   * @return The schema info.
   */
  protected SchemaInfo lookupSchema(String namespace) {
    return getNamespacesToSchemas().get(namespace);
  }

  /**
   * Convenience method to lookup a namespace wsdl given a namespace.
   *
   * @param namespace The namespace for which to lookup the wsdl.
   * @return The wsdl info.
   */
  protected WsdlInfo lookupWSDL(String namespace) {
    return getNamespacesToWSDLs().get(namespace);
  }

  /**
   * The namespace to prefix map.
   *
   * @return The namespace to prefix map.
   */
  protected Map<String, String> getNamespacesToPrefixes() {
    return getModel().getNamespacesToPrefixes();
  }

  /**
   * The namespace to schema map.
   *
   * @return The namespace to schema map.
   */
  protected Map<String, SchemaInfo> getNamespacesToSchemas() {
    return getModel().getNamespacesToSchemas();
  }

  /**
   * The namespace to wsdl map.
   *
   * @return The namespace to wsdl map.
   */
  protected Map<String, WsdlInfo> getNamespacesToWSDLs() {
    return getModel().getNamespacesToWSDLs();
  }

  /**
   * Get the current root model.
   *
   * @return The current root model.
   */
  protected EnunciateFreemarkerModel getModel() {
    return ((EnunciateFreemarkerModel) FreemarkerModel.get());
  }

  /**
   * The current annotation processing environment.
   *
   * @return The current annotation processing environment.
   */
  protected AnnotationProcessorEnvironment getAnnotationProcessorEnvironment() {
    return Context.getCurrentEnvironment();
  }

  /**
   * Whether processing should be verbose.
   *
   * @return Whether processing should be verbose.
   */
  protected boolean isVerbose() {
    return getAnnotationProcessorEnvironment().getOptions().containsKey(EnunciateAnnotationProcessorFactory.VERBOSE_OPTION);
  }

}
