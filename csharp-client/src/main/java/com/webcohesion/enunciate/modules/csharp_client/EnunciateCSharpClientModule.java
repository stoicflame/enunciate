/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webcohesion.enunciate.modules.csharp_client;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import com.webcohesion.enunciate.api.resources.Method;
import com.webcohesion.enunciate.api.resources.Resource;
import com.webcohesion.enunciate.api.resources.ResourceGroup;
import com.webcohesion.enunciate.artifacts.ArtifactType;
import com.webcohesion.enunciate.artifacts.ClientLibraryArtifact;
import com.webcohesion.enunciate.artifacts.FileArtifact;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.metadata.DocumentationExample;
import com.webcohesion.enunciate.module.ApiProviderModule;
import com.webcohesion.enunciate.module.BasicGeneratingModule;
import com.webcohesion.enunciate.module.DependencySpec;
import com.webcohesion.enunciate.module.EnunciateModule;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbModule;
import com.webcohesion.enunciate.modules.jaxb.model.SchemaInfo;
import com.webcohesion.enunciate.modules.jaxb.model.TypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.util.AccessorOverridesAnotherMethod;
import com.webcohesion.enunciate.modules.jaxb.util.FindRootElementMethod;
import com.webcohesion.enunciate.modules.jaxrs.EnunciateJaxrsModule;
import com.webcohesion.enunciate.modules.jaxws.EnunciateJaxwsModule;
import com.webcohesion.enunciate.modules.jaxws.WsdlInfo;
import com.webcohesion.enunciate.modules.jaxws.model.EndpointInterface;
import com.webcohesion.enunciate.modules.jaxws.model.WebFault;
import com.webcohesion.enunciate.modules.jaxws.model.WebMethod;
import com.webcohesion.enunciate.util.freemarker.ClientPackageForMethod;
import com.webcohesion.enunciate.util.freemarker.FileDirective;
import com.webcohesion.enunciate.util.freemarker.IsFacetExcludedMethod;
import com.webcohesion.enunciate.util.freemarker.SimpleNameWithParamsMethod;
import freemarker.cache.URLTemplateLoader;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.configuration.HierarchicalConfiguration;

import javax.lang.model.type.DeclaredType;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * @author Ryan Heaton
 */
public class EnunciateCSharpClientModule extends BasicGeneratingModule implements ApiProviderModule {

  private static final String LIRBARY_DESCRIPTION_PROPERTY = "com.webcohesion.enunciate.modules.csharp_client.EnunciateCSharpClientModule#LIRBARY_DESCRIPTION_PROPERTY";

  EnunciateJaxbModule jaxbModule;
  EnunciateJaxwsModule jaxwsModule;
  EnunciateJaxrsModule jaxrsModule;

  /**
   * @return "java-xml"
   */
  @Override
  public String getName() {
    return "csharp-client";
  }

  @Override
  public List<DependencySpec> getDependencySpecifications() {
    return Arrays.asList((DependencySpec) new DependencySpec() {
      @Override
      public boolean accept(EnunciateModule module) {
        if (module instanceof EnunciateJaxbModule) {
          jaxbModule = (EnunciateJaxbModule) module;
          return true;
        }
        else if (module instanceof EnunciateJaxwsModule) {
          jaxwsModule = (EnunciateJaxwsModule) module;
          return true;
        }
        else if (module instanceof EnunciateJaxrsModule) {
          jaxrsModule = (EnunciateJaxrsModule) module;
          return true;
        }

        return false;
      }

      @Override
      public boolean isFulfilled() {
        return true;
      }
    });
  }

  @Override
  public void call(EnunciateContext context) {
    if (this.jaxbModule == null) {
      debug("JAXB module is unavailable: no C# client will be generated.");
      return;
    }

    Map<String, String> packageToNamespaceConversions = buildPackageToNamespaceConversions();
    File srcDir = generateSources(packageToNamespaceConversions);
    File compileDir = compileSources(srcDir);
    packageArtifacts(srcDir, compileDir);
  }

  private Map<String, String> buildPackageToNamespaceConversions() {
    Map<String, String> packageToNamespaceConversions = getPackageToNamespaceConversions();
    if (this.jaxwsModule != null) {
      HashMap<String, WebFault> allFaults = new HashMap<String, WebFault>();
      for (WsdlInfo wsdlInfo : this.jaxwsModule.getJaxwsContext().getWsdls().values()) {
        for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
          String pckg = ei.getPackage().getQualifiedName().toString();
          if (!packageToNamespaceConversions.containsKey(pckg)) {
            packageToNamespaceConversions.put(pckg, packageToNamespace(pckg));
          }
          for (WebMethod webMethod : ei.getWebMethods()) {
            for (WebFault webFault : webMethod.getWebFaults()) {
              allFaults.put(webFault.getQualifiedName().toString(), webFault);
            }
          }
        }
      }

      for (WebFault webFault : allFaults.values()) {
        String pckg = webFault.getPackage().getQualifiedName().toString();
        if (!packageToNamespaceConversions.containsKey(pckg)) {
          packageToNamespaceConversions.put(pckg, packageToNamespace(pckg));
        }
      }
    }

    if (jaxbModule != null) {
      for (SchemaInfo schemaInfo : jaxbModule.getJaxbContext().getSchemas().values()) {
        for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
          String pckg = typeDefinition.getPackage().getQualifiedName().toString();
          if (!packageToNamespaceConversions.containsKey(pckg)) {
            packageToNamespaceConversions.put(pckg, packageToNamespace(pckg));
          }
        }
      }
    }
    return packageToNamespaceConversions;
  }

  private File generateSources(Map<String, String> packageToNamespaceConversions) {
    File srcDir = getSourceDir();
    srcDir.mkdirs();

    Map<String, Object> model = new HashMap<String, Object>();

    ClientPackageForMethod namespaceFor = new ClientPackageForMethod(packageToNamespaceConversions, this.context);
    Collection<WsdlInfo> wsdls = null;
    if (this.jaxwsModule != null) {
      wsdls = this.jaxwsModule.getJaxwsContext().getWsdls().values();
    }
    model.put("wsdls", wsdls);
    EnunciateJaxbContext jaxbContext = this.jaxbModule.getJaxbContext();
    model.put("schemas", jaxbContext.getSchemas().values());
    model.put("baseUri", this.enunciate.getConfiguration().getApplicationRoot());
    model.put("generatedCodeLicense", this.enunciate.getConfiguration().readGeneratedCodeLicense());
    model.put("namespaceFor", namespaceFor);
    model.put("findRootElement", new FindRootElementMethod(jaxbContext));
    model.put("requestDocumentQName", new RequestDocumentQNameMethod());
    model.put("responseDocumentQName", new ResponseDocumentQNameMethod());
    ClientClassnameForMethod classnameFor = new ClientClassnameForMethod(packageToNamespaceConversions, jaxbContext);
    model.put("classnameFor", classnameFor);
    model.put("listsAsArraysClassnameFor", new ListsAsArraysClientClassnameForMethod(packageToNamespaceConversions, jaxbContext));
    model.put("simpleNameFor", new SimpleNameFor(classnameFor));
    model.put("csFileName", getSourceFileName());
    model.put("accessorOverridesAnother", new AccessorOverridesAnotherMethod());
    model.put("file", new FileDirective(srcDir));

    Set<String> facetIncludes = new TreeSet<String>(this.enunciate.getConfiguration().getFacetIncludes());
    facetIncludes.addAll(getFacetIncludes());
    Set<String> facetExcludes = new TreeSet<String>(this.enunciate.getConfiguration().getFacetExcludes());
    facetExcludes.addAll(getFacetExcludes());
    FacetFilter facetFilter = new FacetFilter(facetIncludes, facetExcludes);

    model.put("isFacetExcluded", new IsFacetExcludedMethod(facetFilter));

    if (!isUpToDateWithSources(srcDir)) {
      debug("Generating the C# client classes...");
      URL apiTemplate = isSingleFilePerClass() ? getTemplateURL("api-multiple-files.fmt") : getTemplateURL("api.fmt");
      try {
        processTemplate(apiTemplate, model);
      }
      catch (IOException e) {
        throw new EnunciateException(e);
      }
      catch (TemplateException e) {
        throw new EnunciateException(e);
      }
    }
    else {
      info("Skipping C# code generation because everything appears up-to-date.");
    }

    this.context.setProperty(LIRBARY_DESCRIPTION_PROPERTY, readLibraryDescription(model));

    return srcDir;
  }

  private File compileSources(File srcDir) {
    File compileDir = getCompileDir();
    compileDir.mkdirs();

    if (!isDisableCompile()) {
      if (!isUpToDateWithSources(compileDir)) {
        String compileExectuable = getCompileExecutable();
        if (compileExectuable == null) {
          String osName = System.getProperty("os.name");
          if (osName != null && osName.toUpperCase().contains("WINDOWS")) {
            //try the "csc" command on Windows environments.
            debug("Attempting to execute command \"csc /help\" for the current environment (%s).", osName);
            try {
              Process process = new ProcessBuilder("csc", "/help").redirectErrorStream(true).start();
              InputStream in = process.getInputStream();
              byte[] buffer = new byte[1024];
              int len = in.read(buffer);
              while (len >- 0) {
                len = in.read(buffer);
              }

              int exitCode = process.waitFor();
              if (exitCode != 0) {
                debug("Command \"csc /help\" failed with exit code " + exitCode + ".");
              }
              else {
                compileExectuable = "csc";
                debug("C# compile executable to be used: csc");
              }
            }
            catch (Throwable e) {
              debug("Command \"csc /help\" failed (" + e.getMessage() + ").");
            }
          }

          if (compileExectuable == null) {
            //try the "gmcs" command (Mono)
            debug("Attempting to execute command \"gmcs /help\" for the current environment (%s).", osName);
            try {
              Process process = new ProcessBuilder("gmcs", "/help").redirectErrorStream(true).start();
              InputStream in = process.getInputStream();
              byte[] buffer = new byte[1024];
              int len = in.read(buffer);
              while (len >- 0) {
                len = in.read(buffer);
              }

              int exitCode = process.waitFor();
              if (exitCode != 0) {
                debug("Command \"gmcs /help\" failed with exit code " + exitCode + ".");
              }
              else {
                compileExectuable = "gmcs";
                debug("C# compile executable to be used: %s", compileExectuable);
              }
            }
            catch (Throwable e) {
              debug("Command \"gmcs /help\" failed (" + e.getMessage() + ").");
            }
          }

          if (compileExectuable == null && isRequire()) {
            throw new EnunciateException("C# client code generation is required, but there was no valid compile executable found. " +
                                           "Please supply one in the configuration file, or set it up on your system path.");
          }
        }

        String compileCommand = getCompileCommand();
        if (compileCommand == null) {
          throw new IllegalStateException("Somehow the \"compile\" step was invoked on the C# module without a valid compile command.");
        }

        compileCommand = compileCommand.replace(' ', '\0'); //replace all spaces with the null character, so the command can be tokenized later.
        File dll = new File(compileDir, getDLLFileName());
        File docXml = new File(compileDir, getDocXmlFileName());
        File sourceFile = new File(srcDir, getSourceFileName());
        compileCommand = String.format(compileCommand, compileExectuable,
                                       dll.getAbsolutePath(),
                                       docXml.getAbsolutePath(),
                                       sourceFile.getAbsolutePath());
        StringTokenizer tokenizer = new StringTokenizer(compileCommand, "\0"); //tokenize on the null character to preserve the spaces in the command.
        List<String> command = new ArrayList<String>();
        while (tokenizer.hasMoreElements()) {
          command.add((String) tokenizer.nextElement());
        }

        try {
          Process process = new ProcessBuilder(command).redirectErrorStream(true).directory(compileDir).start();
          BufferedReader procReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
          String line = procReader.readLine();
          while (line != null) {
            info(line);
            line = procReader.readLine();
          }
          int procCode;
          try {
            procCode = process.waitFor();
          }
          catch (InterruptedException e1) {
            throw new EnunciateException("Unexpected inturruption of the C# compile process.");
          }

          if (procCode != 0) {
            throw new EnunciateException("C# compile failed.");
          }
        }
        catch (IOException e) {
          throw new EnunciateException(e);
        }

        FileArtifact assembly = new FileArtifact(getName(), "csharp.assembly", dll);
        assembly.setPublic(false);
        enunciate.addArtifact(assembly);
        if (docXml.exists()) {
          FileArtifact docs = new FileArtifact(getName(), "csharp.docs.xml", docXml);
          docs.setPublic(false);
          enunciate.addArtifact(docs);
        }
      }
      else {
        info("Skipping C# compile because everything appears up-to-date.");
      }
    }
    else {
      debug("Skipping C# compile because a compile executale was neither found nor provided.  The C# bundle will only include the sources.");
    }
    return compileDir;
  }

  private void packageArtifacts(File srcDir, File compileDir) {
    File packageDir = getPackageDir();
    packageDir.mkdirs();

    if (!isUpToDateWithSources(packageDir)) {
      try {
        //we want to zip up the source file, too, so we'll just copy it to the compile dir.
        enunciate.copyDir(srcDir, compileDir);

        File bundle = new File(packageDir, getBundleFileName());
        boolean anyFiles = enunciate.zip(bundle, compileDir);

        if (anyFiles) {
          ClientLibraryArtifact artifactBundle = new ClientLibraryArtifact(getName(), "csharp.client.library", "C# Client Library");
          artifactBundle.setPlatform(".NET 2.0");

          StringBuilder builder = new StringBuilder("C# source code");
          boolean docsExist = new File(compileDir, getDocXmlFileName()).exists();
          boolean dllExists = new File(compileDir, getDLLFileName()).exists();
          if (docsExist && dllExists) {
            builder.append(", the assembly, and the XML docs");
          }
          else if (dllExists) {
            builder.append("and the assembly");
          }

          artifactBundle.setDescription((String) context.getProperty(LIRBARY_DESCRIPTION_PROPERTY));
          FileArtifact binariesJar = new FileArtifact(getName(), "dotnet.client.bundle", bundle);
          binariesJar.setArtifactType(ArtifactType.binaries);
          binariesJar.setDescription(String.format("The %s for the C# client library.", builder.toString()));
          binariesJar.setPublic(false);
          artifactBundle.addArtifact(binariesJar);
          enunciate.addArtifact(artifactBundle);
        }
      }
      catch (IOException e) {
        throw new EnunciateException(e);
      }
    }
  }

  protected File getSourceDir() {
    return new File(new File(this.enunciate.getBuildDir(), getName()), "src");
  }

  protected File getCompileDir() {
    return new File(new File(this.enunciate.getBuildDir(), getName()), "compile");
  }

  protected File getPackageDir() {
    return new File(new File(this.enunciate.getBuildDir(), getName()), "build");
  }

  protected String packageToNamespace(String pckg) {
    if (pckg == null) {
      return null;
    }
    else {
      StringBuilder ns = new StringBuilder();
      for (StringTokenizer toks = new StringTokenizer(pckg, "."); toks.hasMoreTokens();) {
        String tok = toks.nextToken();
        ns.append(Character.toString(tok.charAt(0)).toUpperCase());
        if (tok.length() > 1) {
          ns.append(tok.substring(1));
        }
        if (toks.hasMoreTokens()) {
          ns.append('.');
        }
      }
      return ns.toString();
    }
  }

  protected String readLibraryDescription(Map<String, Object> model) {
    model.put("sample_service_method", findExampleWebMethod());
    model.put("sample_resource", findExampleResourceMethod());

    URL res = EnunciateCSharpClientModule.class.getResource("library_description.fmt");
    try {
      return processTemplate(res, model);
    }
    catch (TemplateException e) {
      throw new EnunciateException(e);
    }
    catch (IOException e) {
      throw new EnunciateException(e);
    }
  }

  /**
   * Processes the specified template with the given model.
   *
   * @param templateURL The template URL.
   * @param model       The root model.
   */
  public String processTemplate(URL templateURL, Object model) throws IOException, TemplateException {
    debug("Processing template %s.", templateURL);
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
    configuration.setObjectWrapper(new CSharpClientObjectWrapper());
    Template template = configuration.getTemplate(templateURL.toString());
    StringWriter unhandledOutput = new StringWriter();
    template.process(model, unhandledOutput);
    unhandledOutput.close();
    return unhandledOutput.toString();
  }

  /**
   * Finds an example resource method, according to the following preference order:
   *
   * <ol>
   * <li>The first method annotated with {@link com.webcohesion.enunciate.metadata.DocumentationExample}.
   * <li>The first web method that returns a declared type.
   * <li>The first web method.
   * </ol>
   *
   * @return An example resource method, or if no good examples were found.
   */
  public WebMethod findExampleWebMethod() {
    WebMethod example = null;
    for (EndpointInterface ei : this.jaxwsModule.getJaxwsContext().getEndpointInterfaces()) {
      for (WebMethod method : ei.getWebMethods()) {
        if (method.getAnnotation(DocumentationExample.class) != null && !method.getAnnotation(DocumentationExample.class).exclude()) {
          return method;
        }
        else if (method.getWebResult() != null && method.getWebResult().getType() instanceof DeclaredType
          && (example == null || example.getWebResult() == null || (!(example.getWebResult().getType() instanceof DeclaredType)))) {
          example = method;
        }
        else {
          //we'll prefer the first one we find with an output.
          example = example == null ? method : example;
        }
      }
    }
    return example;
  }

  /**
   * Finds an example resource method, according to the following preference order:
   *
   * <ol>
   * <li>The first method annotated with {@link DocumentationExample}.
   * <li>The first method with BOTH an output payload with a known XML element and an input payload with a known XML element.
   * <li>The first method with an output payload with a known XML element.
   * </ol>
   *
   * @return An example resource method, or if no good examples were found.
   */
  public Method findExampleResourceMethod() {
    Method example = null;
    List<ResourceGroup> resourceGroups = this.jaxrsModule.getJaxrsContext().getResourceGroups();
    for (ResourceGroup resourceGroup : resourceGroups) {
      List<Resource> resources = resourceGroup.getResources();
      for (Resource resource : resources) {
        for (Method method : resource.getMethods()) {
          if (hasXmlResponseEntity(method)) {
            if (hasXmlRequestEntity(method)) {
              //we'll prefer one with both an output AND an input.
              return method;
            }
            else {
              //we'll prefer the first one we find with an output.
              example = example == null ? method : example;
            }
          }
        }
      }
    }

    return example;
  }

  private boolean hasXmlResponseEntity(Method method) {
    if (method.getResponseEntity() != null) {
      for (MediaTypeDescriptor mediaTypeDescriptor : method.getResponseEntity().getMediaTypes()) {
        if (EnunciateJaxbContext.SYNTAX_LABEL.equals(mediaTypeDescriptor.getSyntax())) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean hasXmlRequestEntity(Method method) {
    if (method.getRequestEntity() != null) {
      for (MediaTypeDescriptor mediaTypeDescriptor : method.getRequestEntity().getMediaTypes()) {
        if (EnunciateJaxbContext.SYNTAX_LABEL.equals(mediaTypeDescriptor.getSyntax())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * The name of the bundle file.
   *
   * @return The name of the bundle file.
   */
  protected String getBundleFileName() {
    String label = getLabel();
    label = label == null ? this.enunciate.getConfiguration().getLabel() : label;
    return this.config.getString("[@bundleFileName]", label + "-dotnet.zip");
  }

  /**
   * The name of the generated C# dll.
   *
   * @return The name of the generated C# file.
   */
  protected String getDLLFileName() {
    String label = getLabel();
    label = label == null ? this.enunciate.getConfiguration().getLabel() : label;
    return this.config.getString("[@DLLFileName]", label + ".dll");
  }

  /**
   * The name of the generated C# xml documentation.
   *
   * @return The name of the generated C# xml documentation.
   */
  protected String getDocXmlFileName() {
    String label = getLabel();
    label = label == null ? this.enunciate.getConfiguration().getLabel() : label;
    return this.config.getString("[@docXmlFileName]", label + "-docs.xml");
  }

  /**
   * The name of the generated C# source file.
   *
   * @return The name of the generated C# source file.
   */
  protected String getSourceFileName() {
    String label = getLabel();
    label = label == null ? this.enunciate.getConfiguration().getLabel() : label;
    return this.config.getString("[@sourceFileName]", label + ".cs");
  }

  /**
   * Get a template URL for the template of the given name.
   *
   * @param template The specified template.
   * @return The URL to the specified template.
   */
  protected URL getTemplateURL(String template) {
    return EnunciateCSharpClientModule.class.getResource(template);
  }

  /**
   * Whether to require the C# client code.
   *
   * @return Whether to require the C# client code.
   */
  public boolean isRequire() {
    return this.config.getBoolean("[@require]", false);
  }

  /**
   * The label for the C# API.
   *
   * @return The label for the C# API.
   */
  public String getLabel() {
    return this.config.getString("[@label]", null);
  }

  /**
   * The path to the compile executable.
   *
   * @return The path to the compile executable.
   */
  public String getCompileExecutable() {
    return this.config.getString("[@compileExecutable]", null);
  }

  /**
   * The C# compile command.
   *
   * @return The C# compile command.
   */
  public String getCompileCommand() {
    return this.config.getString("[@compileCommand]", "%s /target:library /out:%s /r:System.Web.Services /doc:%s %s");
  }

  /**
   * The package-to-namespace conversions.
   *
   * @return The package-to-namespace conversions.
   */
  public Map<String, String> getPackageToNamespaceConversions() {
    List<HierarchicalConfiguration> conversionElements = this.config.configurationsAt("package-conversions.convert");
    HashMap<String, String> conversions = new HashMap<String, String>();
    for (HierarchicalConfiguration conversionElement : conversionElements) {
      conversions.put(conversionElement.getString("[@from]"), conversionElement.getString("[@to]"));
    }
    return conversions;
  }

  /**
   * Whether to disable the compile step.
   *
   * @return Whether to disable the compile step.
   */
  public boolean isDisableCompile() {
    return this.config.getBoolean("[@disableCompile]", true);
  }

  /**
   * Whether there should be a single file per class. Default: false (all classes are contained in a single file).
   *
   * @return Whether there should be a single file per class.
   */
  public boolean isSingleFilePerClass() {
    return this.config.getBoolean("[@singleFilePerClass]", false);
  }

  public Set<String> getFacetIncludes() {
    List<Object> includes = this.config.getList("facets.include[@name]");
    Set<String> facetIncludes = new TreeSet<String>();
    for (Object include : includes) {
      facetIncludes.add(String.valueOf(include));
    }
    return facetIncludes;
  }

  public Set<String> getFacetExcludes() {
    List<Object> excludes = this.config.getList("facets.exclude[@name]");
    Set<String> facetExcludes = new TreeSet<String>();
    for (Object exclude : excludes) {
      facetExcludes.add(String.valueOf(exclude));
    }
    return facetExcludes;
  }

}
