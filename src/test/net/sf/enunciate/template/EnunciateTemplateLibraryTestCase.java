package net.sf.enunciate.template;

import freemarker.cache.StringTemplateLoader;
import freemarker.cache.URLTemplateLoader;
import freemarker.template.*;
import net.sf.enunciate.apt.EnunciateAnnotationProcessor;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.contract.EnunciateContractTestCase;
import net.sf.enunciate.template.freemarker.PrefixMethod;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import net.sf.jelly.apt.freemarker.FreemarkerTransform;

import java.io.*;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public abstract class EnunciateTemplateLibraryTestCase extends EnunciateContractTestCase {

  /**
   * Invoke a template library method and return the output.
   *
   * @param library    The name of template library.
   * @param methodName The method name.
   * @param args       The arguments to the method.
   * @return The output of invoking the method.
   */
  public String invokeLibraryMethod(String library, String methodName, HashMap<String, Object> args) throws IOException, TemplateException {
    Configuration configuration = new Configuration();
    StringTemplateLoader loader = new StringTemplateLoader();
    configuration.setTemplateLoader(loader);
    configuration.setLocalizedLookup(false);

    StringWriter sourceWriter = new StringWriter();
    sourceWriter.append("[#ftl]\n");
    sourceWriter.append("[#import \"" + library + "\" as lib/]\n");

    FreemarkerModel model = new EnunciateFreemarkerModel();

    //override the methods as necessary.
    HashMap<String, String> methodMocks = getMethodMocks();
    for (String method : methodMocks.keySet()) {
      if (!methodName.equals(method)) {
        sourceWriter.append("[#assign " + method + "=mock_" + method + " in lib/]\n");
        model.put("mock_" + method, new MethodMock(methodMocks.get(method)));
      }
    }

    sourceWriter.append("[@lib." + methodName);
    for (String arg : args.keySet()) {
      sourceWriter.append(" " + arg + "=" + arg);
    }
    sourceWriter.append("/]\n");

    loader.putTemplate("this", sourceWriter.toString());
    loader.putTemplate(library, getLibrarySource(library));

    PrefixMethod prefixMethod = new PrefixMethod() {
      @Override
      protected String lookupPrefix(String namespace) {
        if (namespace == null) {
          return "null";
        }

        return "ns" + String.valueOf(namespace.hashCode());
      }
    };

    model.put("prefix", prefixMethod);
    for (String arg : args.keySet()) {
      model.put(arg, args.get(arg));
    }

    //put all the transforms we may depend on...
    for (FreemarkerTransform transform : getTransforms()) {
      model.put(transform.getTransformName(), transform);
    }

    FreemarkerModel.set(model);

    StringWriter output = new StringWriter();
    Template template = configuration.getTemplate("this");
    template.process(model, output);
    return output.toString();
  }

  protected String getLibrarySource(String library) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(EnunciateTemplateLibraryTestCase.class.getResourceAsStream(library)));

    StringWriter source = new StringWriter();
    String line;
    while ((line = reader.readLine()) != null) {
      source.append(line + "\n");
    }

    return source.toString();
  }

  /**
   * Get the template loader for the freemarker configuration.
   *
   * @return the template loader for the freemarker configuration.
   */
  protected URLTemplateLoader getTemplateLoader() {
    return new URLTemplateLoader() {
      protected URL getURL(String name) {
        return EnunciateTemplateLibraryTestCase.class.getResource(name);
      }
    };
  }

  /**
   * A map from method name to output value.
   *
   * @return A map from method name to output value.
   */
  protected abstract HashMap<String, String> getMethodMocks();

  /**
   * Get the transforms to use for the model.
   *
   * @return The transforms to use for the model.
   */
  protected Collection<FreemarkerTransform> getTransforms() {
    return new EnunciateAnnotationProcessor().getTransforms();
  }

  /**
   * Mock for a method.
   */
  private class MethodMock implements TemplateTransformModel {

    private final String value;

    public MethodMock(String value) {
      this.value = value;
    }

    public Writer getWriter(Writer out, Map args) throws TemplateModelException, IOException {
      out.write(value);
      return null;
    }

  }

}