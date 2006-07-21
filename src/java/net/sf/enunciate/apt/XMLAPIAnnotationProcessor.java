package net.sf.enunciate.apt;

import freemarker.template.TemplateModelException;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import net.sf.jelly.apt.freemarker.FreemarkerProcessor;

/**
 * The annotation processor that generates the XML API.
 *
 * @author Ryan Heaton
 */
public class XMLAPIAnnotationProcessor extends FreemarkerProcessor {

  private final EnunciateFreemarkerModel rootModel;

  public XMLAPIAnnotationProcessor(EnunciateFreemarkerModel rootModel) {
    super(XMLAPIAnnotationProcessor.class.getResource("/net/sf/enunciate/template/xml.fmt"));
    this.rootModel = rootModel;
  }

  @Override
  protected FreemarkerModel getRootModel() throws TemplateModelException {
    return rootModel;
  }


}
