package com.webcohesion.enunciate.modules.idl;

import com.webcohesion.enunciate.api.InterfaceDescriptionFile;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.modules.jaxb.util.PrefixMethod;
import com.webcohesion.enunciate.util.freemarker.IsFacetExcludedMethod;
import freemarker.cache.URLTemplateLoader;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.xml.sax.InputSource;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public abstract class BaseXMLInterfaceDescriptionFile implements InterfaceDescriptionFile {

  protected final FacetFilter facetFilter;
  protected final Map<String, String> namespacePrefixes;
  protected final String filename;
  private String contents;

  public BaseXMLInterfaceDescriptionFile(String filename, Map<String, String> namespacePrefixes, FacetFilter facetFilter) {
    this.namespacePrefixes = namespacePrefixes;
    this.facetFilter = facetFilter;
    this.filename = filename;
  }

  @Override
  public String getHref() {
    if (contents == null) {
      throw new IllegalStateException(String.format("%s hasn't been written yet.", filename));
    }

    return filename;
  }

  @Override
  public void writeTo(File directory) throws IOException {
    FileWriter writer = new FileWriter(new File(directory, this.filename));
    writeTo(writer);
    writer.flush();
    writer.close();
  }

  protected void writeTo(Writer writer) throws IOException {
    if (this.contents != null) {
      writer.write(this.contents);
    }
    else {
      Map<String, Object> model = createModel();
      URL template = getTemplateURL();
      String idl = processTemplate(template, model);
      writer.write(idl);
    }
  }

  protected Map<String, Object> createModel() {
    Map<String, Object> model = new HashMap<String, Object>();
    model.put("prefix", new PrefixMethod(namespacePrefixes));
    model.put("isFacetExcluded", new IsFacetExcludedMethod(this.facetFilter));
    return model;
  }

  protected abstract URL getTemplateURL();

  /**
   * Processes the specified template with the given model.
   *
   * @param model       The root model.
   */
  protected String processTemplate(URL templateURL, Map<String, Object> model) throws IOException {
    Configuration configuration = new Configuration(Configuration.VERSION_2_3_22);

    configuration.setTemplateLoader(new URLTemplateLoader() {
      protected URL getURL(String name) {
        try {
          return new URL(name);
        }
        catch (MalformedURLException e) {
          return null;
        }
      }
    });

    configuration.setTemplateExceptionHandler(new TemplateExceptionHandler() {
      public void handleTemplateException(TemplateException templateException, Environment environment, Writer writer) throws TemplateException {
        throw templateException;
      }
    });

    configuration.setLocalizedLookup(false);
    configuration.setDefaultEncoding("UTF-8");
    configuration.setObjectWrapper(new IDLObjectWrapper(this.namespacePrefixes));
    Template template = configuration.getTemplate(templateURL.toString());
    StringWriter output = new StringWriter();

    try {
      template.process(model, output);
    }
    catch (TemplateException e) {
      throw new RuntimeException(e);
    }

    //pretty-print the idl.
    String idl = prettyPrint(output.toString());

    this.contents = idl;
    return idl;
  }

  /**
   * Pretty-prints the specified XML.
   *
   * @param input The XML to pretty-print.
   */
  protected String prettyPrint(String input) {
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setNamespaceAware(false);
      SAXParser parser = factory.newSAXParser();
      StringWriter output = new StringWriter();
      parser.parse(new InputSource(new StringReader(input)), new PrettyPrinter(output));
      return output.toString();
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
