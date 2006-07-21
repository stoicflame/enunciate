package net.sf.enunciate.apt;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import net.sf.jelly.apt.ProcessorFactory;
import net.sf.jelly.apt.freemarker.FreemarkerProcessorFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class EnunciateAnnotationProcessorFactory extends ProcessorFactory {

  /**
   * Option to specify the config file to use.
   */
  public static final String CONFIG_OPTION = "-Aconfig";

  /**
   * Option to specify the namespace of the enunciate transform library.
   */
  public static final String FM_LIBRARY_NS_OPTION = "-AEnunciateFreemarkerLibraryNS";

  /**
   * Option to specify a verbose output.
   */
  public static final String VERBOSE_OPTION = "-Averbose";

  private static final Collection<String> SUPPORTED_OPTIONS = Collections.unmodifiableCollection(Arrays.asList(CONFIG_OPTION,
                                                                                                               FM_LIBRARY_NS_OPTION,
                                                                                                               FreemarkerProcessorFactory.FM_LIBRARY_NS_OPTION,
                                                                                                               VERBOSE_OPTION));
  private static final Collection<String> SUPPORTED_TYPES = Collections.unmodifiableCollection(Arrays.asList("*"));

  //Inherited.
  public Collection<String> supportedOptions() {
    return SUPPORTED_OPTIONS;
  }

  //Inherited.
  public Collection<String> supportedAnnotationTypes() {
    return SUPPORTED_TYPES;
  }

  @Override
  protected AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> annotations) {
    return new EnunciateAnnotationProcessor();
  }

  //no-op.
  protected AnnotationProcessor newProcessor(URL url) {
    return null;
  }

}
