package net.sf.enunciate.apt;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import net.sf.enunciate.API;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.ProcessorFactory;
import net.sf.jelly.apt.freemarker.FreemarkerProcessorFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Ryan Heaton
 */
public class EnunciateAnnotationProcessorFactory extends ProcessorFactory {

  /**
   * Option to pass to the factory specifying which api to generate.
   */
  public static final String API_OPTION = "-Aapi";

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

  private static final Collection<String> SUPPORTED_OPTIONS = Collections.unmodifiableCollection(Arrays.asList(API_OPTION,
                                                                                                               CONFIG_OPTION,
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

  //Inherited.
  protected AnnotationProcessor newProcessor(URL url) {
    return new EnunciateAnnotationProcessor(url);
  }

  //Inherited.
  @Override
  protected URL getTemplateURL() {
    AnnotationProcessorEnvironment env = Context.getCurrentEnvironment();

    String api = env.getOptions().get(API_OPTION);

    if (api == null) {
      throw new IllegalArgumentException(String.format("A valid api must be specified with the %s option.", API_OPTION));
    }

    return API.valueOf(api.toUpperCase()).getTemplate();
  }

}
