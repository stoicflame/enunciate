package org.codehaus.enunciate.modules.docs;

import freemarker.core.Environment;
import freemarker.template.*;
import freemarker.template.utility.DeepUnwrap;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class FileDirective implements TemplateDirectiveModel {

  private final File outputDir;

  public FileDirective(File outputDir) {
    this.outputDir = outputDir;

    if (!outputDir.exists()) {
      outputDir.mkdirs();
    }
    else if (!outputDir.isDirectory()) {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
    String filePath = (String) DeepUnwrap.unwrap((TemplateModel)  params.get("name"));
    if (filePath == null) {
      throw new TemplateModelException("A 'name' parameter must be provided to create a new file.");
    }

    String charset = (String) DeepUnwrap.unwrap((TemplateModel)  params.get("charset"));
    if (charset == null) {
      charset = "utf-8";
    }

    File output = new File(this.outputDir, filePath);
    if (!output.getParentFile().exists()) {
      output.getParentFile().mkdirs();
    }

    PrintWriter writer = new PrintWriter(output, charset);
    body.render(writer);
    writer.close();
  }
}
