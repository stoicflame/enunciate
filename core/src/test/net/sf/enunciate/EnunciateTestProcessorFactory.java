package net.sf.enunciate;

import net.sf.enunciate.apt.EnunciateAnnotationProcessorFactory;

import java.net.URL;

/**
 * An ProcessorFactory that can be invoked more than once in the same JVM.
 */
public class EnunciateTestProcessorFactory extends EnunciateAnnotationProcessorFactory {

  private final URL api;

  public EnunciateTestProcessorFactory(URL template) throws EnunciateException {
    super(null);
    this.api = template;
    round = 0; //reset the round.
  }

  //Inherited.
  @Override
  protected URL getTemplateURL() {
    return this.api;
  }

}
