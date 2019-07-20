/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.java_json_client;

import com.sun.tools.javac.api.JavacTool;
import com.webcohesion.enunciate.Enunciate;
import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.api.DefaultRegistrationContext;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import com.webcohesion.enunciate.api.resources.Method;
import com.webcohesion.enunciate.api.resources.Resource;
import com.webcohesion.enunciate.api.resources.ResourceGroup;
import com.webcohesion.enunciate.artifacts.ArtifactType;
import com.webcohesion.enunciate.artifacts.ClientLibraryJavaArtifact;
import com.webcohesion.enunciate.artifacts.FileArtifact;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.javac.decorations.SourcePosition;
import com.webcohesion.enunciate.metadata.DocumentationExample;
import com.webcohesion.enunciate.module.*;
import com.webcohesion.enunciate.modules.jackson.EnunciateJacksonContext;
import com.webcohesion.enunciate.modules.jackson.JacksonModule;
import com.webcohesion.enunciate.modules.jackson.model.TypeDefinition;
import com.webcohesion.enunciate.modules.jackson.model.util.JacksonCodeErrors;
import com.webcohesion.enunciate.modules.jackson1.EnunciateJackson1Context;
import com.webcohesion.enunciate.modules.jackson1.Jackson1Module;
import com.webcohesion.enunciate.modules.jackson1.api.impl.SyntaxImpl;
import com.webcohesion.enunciate.modules.jackson1.model.util.Jackson1CodeErrors;
import com.webcohesion.enunciate.modules.jaxrs.JaxrsModule;
import com.webcohesion.enunciate.util.AntPatternMatcher;
import com.webcohesion.enunciate.util.freemarker.AnnotationValueMethod;
import com.webcohesion.enunciate.util.freemarker.ClientPackageForMethod;
import com.webcohesion.enunciate.util.freemarker.FileDirective;
import com.webcohesion.enunciate.util.freemarker.IsFacetExcludedMethod;
import freemarker.cache.URLTemplateLoader;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.configuration.HierarchicalConfiguration;

import javax.lang.model.element.TypeElement;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * @author Ryan Heaton
 */
public class JavaJSONClientModule extends BasicGeneratingModule implements ApiFeatureProviderModule, ProjectExtensionModule {

  private static final String LIRBARY_DESCRIPTION_PROPERTY = "com.webcohesion.enunciate.modules.java_xml_client.EnunciateJavaJSONClientModule#LIRBARY_DESCRIPTION_PROPERTY";

  JacksonModule jacksonModule;
  Jackson1Module jackson1Module;
  JaxrsModule jaxrsModule;

  /**
   * @return "java-xml"
   */
  @Override
  public String getName() {
    return "java-json-client";
  }

  @Override
  public List<DependencySpec> getDependencySpecifications() {
    return Arrays.asList((DependencySpec) new DependencySpec() {
      @Override
      public boolean accept(EnunciateModule module) {
        if (module instanceof JacksonModule) {
          jacksonModule = (JacksonModule) module;
          return true;
        }
        if (module instanceof Jackson1Module) {
          jackson1Module = (Jackson1Module) module;
          return true;
        }
        else if (module instanceof JaxrsModule) {
          jaxrsModule = (JaxrsModule) module;
          return true;
        }

        return module instanceof ApiRegistryProviderModule;
      }

      @Override
      public boolean isFulfilled() {
        return true;
      }

      @Override
      public String toString() {
        return "optional jackson, optional jackson1, optional jaxrs";
      }
    });
  }


  @Override
  public void call(EnunciateContext context) {
    if ((this.jacksonModule == null || this.jacksonModule.getJacksonContext() == null || this.jacksonModule.getJacksonContext().getTypeDefinitions().isEmpty()) &&
      (this.jackson1Module == null || this.jackson1Module.getJacksonContext() == null || this.jackson1Module.getJacksonContext().getTypeDefinitions().isEmpty()))
      {
      info("No Jackson JSON data types: Java JSON client will not be generated.");
      return;
    }

    detectAccessorNamingErrors();

    File sourceDir = generateClientSources();
    File compileDir = compileClientSources(sourceDir);

    packageArtifacts(sourceDir, compileDir);
  }

  protected void detectAccessorNamingErrors() {
    if (this.jacksonModule != null) {
      List<String> namingConflicts = JacksonCodeErrors.findConflictingAccessorNamingErrors(this.jacksonModule.getJacksonContext());
      if (namingConflicts != null && !namingConflicts.isEmpty()) {
        error("Jackson naming conflicts have been found:");
        for (String namingConflict : namingConflicts) {
          error(namingConflict);
        }
        error("These naming conflicts are often between the field and it's associated property, in which case you need to use one or both of the following strategies to avoid the conflicts:");
        error("1. Explicitly exclude one or the other.");
        error("2. Put the annotations on the property instead of the field.");
        throw new EnunciateException("Jackson naming conflicts detected.");
      }
    }

    if (this.jackson1Module != null) {
      List<String> namingConflicts = Jackson1CodeErrors.findConflictingAccessorNamingErrors(this.jackson1Module.getJacksonContext());
      if (namingConflicts != null && !namingConflicts.isEmpty()) {
        error("Jackson naming conflicts have been found:");
        for (String namingConflict : namingConflicts) {
          error(namingConflict);
        }
        error("These naming conflicts are often between the field and it's associated property, in which case you need to use one or both of the following strategies to avoid the conflicts:");
        error("1. Explicitly exclude one or the other.");
        error("2. Put the annotations on the property instead of the field.");
        throw new EnunciateException("Jackson naming conflicts detected.");
      }
    }
  }

  protected File generateClientSources() {
    File sourceDir = getSourceDir();
    sourceDir.mkdirs();

    Map<String, Object> model = new HashMap<String, Object>();

    Map<String, String> conversions = getClientPackageConversions();
    EnunciateJacksonContext jacksonContext = this.jacksonModule != null ? this.jacksonModule.getJacksonContext() : null;
    EnunciateJackson1Context jackson1Context = this.jackson1Module != null ? this.jackson1Module.getJacksonContext() : null;
    MergedJsonContext jsonContext = new MergedJsonContext(jacksonContext, jackson1Context);
    model.put("packageFor", new ClientPackageForMethod(conversions, this.context));
    model.put("classnameFor", new ClientClassnameForMethod(conversions, jsonContext));
    model.put("simpleNameFor", new SimpleNameForMethod(new ClientClassnameForMethod(conversions, jsonContext, true), jsonContext));
    model.put("file", new FileDirective(sourceDir, this.enunciate.getLogger()));
    model.put("generatedCodeLicense", this.enunciate.getConfiguration().readGeneratedCodeLicenseFile());
    model.put("annotationValue", new AnnotationValueMethod());
    model.put("wrapRootValue", this.jacksonModule == null ? this.jackson1Module.isWrapRootValue() : this.jacksonModule.isWrapRootValue());

    Set<String> facetIncludes = new TreeSet<String>(this.enunciate.getConfiguration().getFacetIncludes());
    facetIncludes.addAll(getFacetIncludes());
    Set<String> facetExcludes = new TreeSet<String>(this.enunciate.getConfiguration().getFacetExcludes());
    facetExcludes.addAll(getFacetExcludes());
    FacetFilter facetFilter = new FacetFilter(facetIncludes, facetExcludes);

    model.put("isFacetExcluded", new IsFacetExcludedMethod(facetFilter));

    AntPatternMatcher matcher = new AntPatternMatcher();
    matcher.setPathSeparator(".");

    boolean upToDate = isUpToDateWithSources(sourceDir);
    if (!upToDate) {
      try {
        debug("Generating the Java client classes...");

        if (jacksonContext != null) {
          for (TypeDefinition typeDefinition : jacksonContext.getTypeDefinitions()) {
            if (facetFilter.accept(typeDefinition)) {
              if (useServerSide(typeDefinition, matcher)) {
                copyServerSideType(sourceDir, typeDefinition);
              }
              else {
                model.put("type", typeDefinition);
                URL template = typeDefinition.isEnum() ? getTemplateURL("client-enum-type.fmt") : typeDefinition.isSimple() ? getTemplateURL("client-simple-type.fmt") : getTemplateURL("client-complex-type.fmt");
                processTemplate(template, model);
              }
            }
          }
        }

        if (jackson1Context != null) {
          for (com.webcohesion.enunciate.modules.jackson1.model.TypeDefinition typeDefinition : jackson1Context.getTypeDefinitions()) {
            if (facetFilter.accept(typeDefinition)) {
              if (useServerSide(typeDefinition, matcher)) {
                copyServerSideType(sourceDir, typeDefinition);
              }
              else {
                model.put("type", typeDefinition);
                URL template = typeDefinition.isEnum() ? getTemplateURL("client-enum-type.fmt") : typeDefinition.isSimple() ? getTemplateURL("client-simple-type.fmt") : getTemplateURL("client-complex-type.fmt");
                processTemplate(template, model);
              }
            }
          }
        }
      }
      catch (IOException e) {
        throw new EnunciateException(e);
      }
      catch (TemplateException e) {
        throw new EnunciateException(e);
      }
    }
    else {
      info("Skipping generation of Java client sources as everything appears up-to-date...");
    }

    context.setProperty(LIRBARY_DESCRIPTION_PROPERTY, readLibraryDescription(model));

    return sourceDir;
  }

  protected void copyServerSideType(File sourceDir, TypeElement type) throws IOException {
    SourcePosition source = this.context.getProcessingEnvironment().findSourcePosition(type);
    JavaFileObject sourceFile = source.getSourceFile();
    File destFile = getServerSideDestFile(sourceDir, sourceFile, type);
    FileWriter writer = new FileWriter(destFile);
    debug("Writing server-side java type to %s.", destFile);
    writer.write(sourceFile.getCharContent(false).toString());
    writer.flush();
    writer.close();
  }

  protected File getSourceDir() {
    return new File(new File(this.enunciate.getBuildDir(), getName()), "src");
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
    configuration.setLocale(new Locale("en", "US"));

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
    configuration.setObjectWrapper(new JavaJSONClientObjectWrapper());
    Template template = configuration.getTemplate(templateURL.toString());
    StringWriter unhandledOutput = new StringWriter();
    template.process(model, unhandledOutput);
    unhandledOutput.close();
    return unhandledOutput.toString();
  }

  protected File getServerSideDestFile(File sourceDir, JavaFileObject sourceFile, TypeElement declaration) {
    File destDir = sourceDir;
    String packageName = this.context.getProcessingEnvironment().getElementUtils().getPackageOf(declaration).getQualifiedName().toString();
    for (StringTokenizer packagePaths = new StringTokenizer(packageName, "."); packagePaths.hasMoreTokens();) {
      String packagePath = packagePaths.nextToken();
      destDir = new File(destDir, packagePath);
    }
    destDir.mkdirs();
    String simpleFilename = sourceFile.toUri().toString();
    simpleFilename = simpleFilename.substring(simpleFilename.lastIndexOf('/'));
    return new File(destDir, simpleFilename);
  }

  /**
   * Whether to use the server-side declaration for this declaration.
   *
   * @param declaration The declaration.
   * @param matcher     The matcher.
   * @return Whether to use the server-side declaration for this declaration.
   */
  protected boolean useServerSide(TypeElement declaration, AntPatternMatcher matcher) {
    boolean useServerSide = false;

    for (String pattern : getServerSideTypesToUse()) {
      if (matcher.match(pattern, declaration.getQualifiedName().toString())) {
        useServerSide = true;
        break;
      }
    }
    return useServerSide;
  }

  /**
   * Get the bean name for a specified string.
   *
   * @param conversion The conversion to use.
   * @param preconvert The pre-converted fqn.
   * @return The converted fqn.
   */
  protected String getBeanName(ClientClassnameForMethod conversion, String preconvert) {
    String pckg = conversion.convert(preconvert.substring(0, preconvert.lastIndexOf('.')));
    String simpleName = preconvert.substring(preconvert.lastIndexOf('.') + 1);
    return pckg + "." + simpleName;
  }

  protected File compileClientSources(File sourceDir) {
    File compileDir = getCompileDir();
    compileDir.mkdirs();

    //Compile the java files.
    if (!isDisableCompile()) {
      if (!isUpToDateWithSources(compileDir)) {
        List<File> sources = findJavaFiles(sourceDir);
        if (sources != null && !sources.isEmpty()) {
          String classpath = this.enunciate.writeClasspath(enunciate.getClasspath());
          JavaCompiler compiler = JavacTool.create();
          List<String> options = Arrays.asList("-source", getJavacSource(), "-target", getJavacTarget(), "-encoding", "UTF-8", "-cp", classpath, "-d", compileDir.getAbsolutePath(), "-nowarn");
          JavaCompiler.CompilationTask task = compiler.getTask(null, null, null, options, null, compiler.getStandardFileManager(null, null, null).getJavaFileObjectsFromFiles(sources));
          if (!task.call()) {
            throw new EnunciateException("Compile failed of Java JSON client-side classes.");
          }
        }
        else {
          debug("No Java JSON client classes to compile.");
        }
      }
      else {
        info("Skipping compilation of Java JSON client classes as everything appears up-to-date...");
      }
    }

    return compileDir;

  }

  private List<File> findJavaFiles(File sourceDir) {
    final ArrayList<File> javaFiles = new ArrayList<File>();
    this.enunciate.visitFiles(sourceDir, Enunciate.JAVA_FILTER, new Enunciate.FileVisitor() {
      @Override
      public void visit(File file) {
        javaFiles.add(file);
      }
    });
    return javaFiles;
  }

  protected File getCompileDir() {
    return new File(new File(this.enunciate.getBuildDir(), getName()), "classes");
  }

  protected File getResourcesDir() {
    return new File(new File(this.enunciate.getBuildDir(), getName()), "resources");
  }

  protected File packageArtifacts(File sourceDir, File compileDir) {
    File packageDir = getPackageDir();
    packageDir.mkdirs();

    try {
      String jarName = getJarName();

      File clientJarFile = null;
      if (!isDisableCompile()) {
        clientJarFile = new File(packageDir, jarName);
        if (!isUpToDateWithSources(clientJarFile)) {
          if (isBundleSourcesWithClasses()) {
            boolean anyFiles = this.enunciate.zip(clientJarFile, sourceDir, compileDir);
            if (!anyFiles) {
              clientJarFile = null;
            }
          }
          else {
            boolean anyFiles = this.enunciate.zip(clientJarFile, compileDir);
            if (!anyFiles) {
              clientJarFile = null;
            }
          }
        }
        else {
          info("Skipping creation of Java client jar as everything appears up-to-date...");
        }
      }

      File clientSourcesJarFile = null;
      if (!isBundleSourcesWithClasses()) {
        clientSourcesJarFile = new File(packageDir, jarName.replaceFirst("\\.jar", "-json-sources.jar"));
        if (!isUpToDateWithSources(clientSourcesJarFile)) {
          boolean anyFiles = this.enunciate.zip(clientSourcesJarFile, sourceDir);
          if (!anyFiles) {
            clientSourcesJarFile = null;
          }
        }
        else {
          info("Skipping creation of the Java client source jar as everything appears up-to-date...");
        }
      }

      ClientLibraryJavaArtifact artifactBundle = new ClientLibraryJavaArtifact(getName(), "java.json.client.library", "Java JSON Client Library");
      artifactBundle.setGroupId(getGroupId());
      artifactBundle.setArtifactId(getArtifactId());
      artifactBundle.setVersion(getVersion());
      artifactBundle.setPlatform("Java (Version 5+)");
      //read in the description from file:
      artifactBundle.setDescription((String) context.getProperty(LIRBARY_DESCRIPTION_PROPERTY));
      if (clientJarFile != null) {
        FileArtifact binariesJar = new FileArtifact(getName(), "java.json.client.library.binaries", clientJarFile);
        binariesJar.setDescription("The binaries for the Java JSON client library.");
        binariesJar.setPublic(false);
        binariesJar.setArtifactType(ArtifactType.binaries);
        artifactBundle.addArtifact(binariesJar);
        this.enunciate.addArtifact(binariesJar);
      }

      if (clientSourcesJarFile != null) {
        FileArtifact sourcesJar = new FileArtifact(getName(), "java.json.client.library.sources", clientSourcesJarFile);
        sourcesJar.setDescription("The sources for the Java JSON client library.");
        sourcesJar.setPublic(false);
        sourcesJar.setArtifactType(ArtifactType.sources);
        artifactBundle.addArtifact(sourcesJar);
        this.enunciate.addArtifact(sourcesJar);
      }

      if (clientJarFile != null || clientSourcesJarFile != null) {
        this.enunciate.addArtifact(artifactBundle);
      }
    }
    catch (IOException e) {
      throw new EnunciateException(e);
    }

    return packageDir;
  }

  protected File getPackageDir() {
    return new File(new File(this.enunciate.getBuildDir(), getName()), "build");
  }

  /**
   * Reads a resource into string form.
   *
   * @return The string form of the resource.
   */
  protected String readLibraryDescription(Map<String, Object> model) {
    model.put("sample_resource", findExampleResourceMethod());
    model.put("mediaTypeFor", new MediaTypeForMethod());

    URL res = JavaJSONClientModule.class.getResource("library_description.fmt");
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
    List<ResourceGroup> resourceGroups = this.jaxrsModule.getJaxrsContext().getResourceGroups(new DefaultRegistrationContext(context));
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
        String syntax = mediaTypeDescriptor.getSyntax();
        if (com.webcohesion.enunciate.modules.jackson.api.impl.SyntaxImpl.SYNTAX_LABEL.equals(syntax) || SyntaxImpl.SYNTAX_LABEL.equals(syntax)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean hasXmlRequestEntity(Method method) {
    if (method.getRequestEntity() != null) {
      for (MediaTypeDescriptor mediaTypeDescriptor : method.getRequestEntity().getMediaTypes()) {
        String syntax = mediaTypeDescriptor.getSyntax();
        if (com.webcohesion.enunciate.modules.jackson.api.impl.SyntaxImpl.SYNTAX_LABEL.equals(syntax) || SyntaxImpl.SYNTAX_LABEL.equals(syntax)) {
          return true;
        }
      }
    }
    return false;
  }

  protected URL getTemplateURL(String template) {
    return JavaJSONClientModule.class.getResource(template);
  }

  public String getJarName() {
    return this.config.getString("[@jarName]", getSlug() + "-json-client.jar");
  }

  public String getGroupId() {
    return this.config.getString("[@groupId]", null);
  }

  public String getArtifactId() {
    return this.config.getString("[@artifactId]", null);
  }

  public String getVersion() {
    return this.config.getString("[@version]", null);
  }

  public String getJavacSource() {
    return this.config.getString("[@javac-source]", "1.6");
  }

  public String getJavacTarget() {
    return this.config.getString("[@javac-target]", "1.6");
  }

  public Map<String, String> getClientPackageConversions() {
    List<HierarchicalConfiguration> conversionElements = this.config.configurationsAt("package-conversions.convert");
    HashMap<String, String> conversions = new HashMap<String, String>();
    conversions.put("java.lang.Exception", "client.java.lang.Exception");
    conversions.put("java.util.Map.Entry", "java.util.Map.Entry");

    for (HierarchicalConfiguration conversionElement : conversionElements) {
      conversions.put(conversionElement.getString("[@from]"), conversionElement.getString("[@to]"));
    }
    return conversions;
  }

  public Set<String> getServerSideTypesToUse() {
    List<HierarchicalConfiguration> typeElements = this.config.configurationsAt("server-side-type");
    TreeSet<String> types = new TreeSet<String>();
    for (HierarchicalConfiguration typeElement : typeElements) {
      types.add(typeElement.getString("[@pattern]"));
    }
    return types;
  }

  public String getSlug() {
    return this.config.getString("[@slug]", this.enunciate.getConfiguration().getSlug());
  }

  public boolean isBundleSourcesWithClasses() {
    return this.config.getBoolean("[@bundleSourcesWithClasses]", false);
  }

  public List<File> getProjectSources() {
    return Collections.emptyList();
  }

  public List<File> getProjectTestSources() {
    return Arrays.asList(getSourceDir());
  }

  public List<File> getProjectResourceDirectories() {
    return Collections.emptyList();
  }

  public List<File> getProjectTestResourceDirectories() {
    return Arrays.asList(getResourcesDir());
  }

  /**
   * Whether to disable the compilation of the java sources (default: false).
   *
   * @return Whether to disable the compilation of the java sources (default: false).
   */
  public boolean isDisableCompile() {
    return this.config.getBoolean("[@disableCompile]", this.jacksonModule == null || !this.jacksonModule.isJacksonDetected());
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
