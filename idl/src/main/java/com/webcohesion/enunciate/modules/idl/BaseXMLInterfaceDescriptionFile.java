/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.idl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.webcohesion.enunciate.Enunciate;
import com.webcohesion.enunciate.api.InterfaceDescriptionFile;
import com.webcohesion.enunciate.artifacts.FileArtifact;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.modules.jaxb.util.PrefixMethod;
import com.webcohesion.enunciate.util.freemarker.IsFacetExcludedMethod;
import freemarker.cache.URLTemplateLoader;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * @author Ryan Heaton
 */
public abstract class BaseXMLInterfaceDescriptionFile implements InterfaceDescriptionFile {

  private final Enunciate enunciate;
  private final String artifactId;
  protected final FacetFilter facetFilter;
  protected final Map<String, String> namespacePrefixes;
  protected final String filename;
  private String contents;

  public BaseXMLInterfaceDescriptionFile(Enunciate enunciate, String artifactId, String filename, Map<String, String> namespacePrefixes, FacetFilter facetFilter) {
    this.enunciate = enunciate;
    this.artifactId = artifactId;
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
    File file = new File(directory, this.filename);
    OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
    writeTo(writer);
    writer.flush();
    writer.close();
    this.enunciate.addArtifact(new FileArtifact("idl", this.artifactId, file));
  }

  protected void writeTo(Writer writer) throws IOException {
    if (this.contents != null) {
      writer.write(this.contents);
    } else {
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
    configuration.setLocale(new Locale("en", "US"));

    configuration.setTemplateLoader(new URLTemplateLoader() {
      protected URL getURL(String name) {
        try {
          return new URL(name);
        } catch (MalformedURLException e) {
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
    } catch (TemplateException e) {
      throw new RuntimeException(e);
    }

    String idl = output.toString();
    this.contents = idl;
    return idl;
  }

}
