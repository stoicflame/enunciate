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
package com.webcohesion.enunciate.modules.csharp_client;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.api.DefaultRegistrationContext;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import com.webcohesion.enunciate.api.resources.Method;
import com.webcohesion.enunciate.api.resources.Resource;
import com.webcohesion.enunciate.api.resources.ResourceGroup;
import com.webcohesion.enunciate.artifacts.ArtifactType;
import com.webcohesion.enunciate.artifacts.ClientLibraryArtifact;
import com.webcohesion.enunciate.artifacts.FileArtifact;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.javac.decorations.element.ElementUtils;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.metadata.ClientName;
import com.webcohesion.enunciate.metadata.DocumentationExample;
import com.webcohesion.enunciate.module.*;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.JaxbModule;
import com.webcohesion.enunciate.modules.jaxb.api.impl.SyntaxImpl;
import com.webcohesion.enunciate.modules.jaxb.model.*;
import com.webcohesion.enunciate.modules.jaxb.model.util.JAXBCodeErrors;
import com.webcohesion.enunciate.modules.jaxb.model.util.MapType;
import com.webcohesion.enunciate.modules.jaxb.util.AccessorOverridesAnotherMethod;
import com.webcohesion.enunciate.modules.jaxb.util.FindRootElementMethod;
import com.webcohesion.enunciate.modules.jaxrs.JaxrsModule;
import com.webcohesion.enunciate.modules.jaxws.JaxwsModule;
import com.webcohesion.enunciate.modules.jaxws.WsdlInfo;
import com.webcohesion.enunciate.modules.jaxws.model.EndpointInterface;
import com.webcohesion.enunciate.modules.jaxws.model.WebFault;
import com.webcohesion.enunciate.modules.jaxws.model.WebMethod;
import com.webcohesion.enunciate.modules.jaxws.model.WebParam;
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

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * @author Ryan Heaton
 */
public class CSharpXMLClientModule extends BasicGeneratingModule implements ApiFeatureProviderModule {

  private static final String LIRBARY_DESCRIPTION_PROPERTY = "com.webcohesion.enunciate.modules.csharp_client.CSharpXMLClientModule#LIRBARY_DESCRIPTION_PROPERTY";

  JaxbModule jaxbModule;
  JaxwsModule jaxwsModule;
  JaxrsModule jaxrsModule;

  /**
   * @return "csharp-xml-client"
   */
  @Override
  public String getName() {
    return "csharp-xml-client";
  }

  @Override
  public List<DependencySpec> getDependencySpecifications() {
    return Arrays.asList((DependencySpec) new DependencySpec() {
      @Override
      public boolean accept(EnunciateModule module) {
        if (module instanceof JaxbModule) {
          jaxbModule = (JaxbModule) module;
          return true;
        }
        else if (module instanceof JaxwsModule) {
          jaxwsModule = (JaxwsModule) module;
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
        return "optional jaxb, optional jaxws, optional jaxrs";
      }
    });
  }

  @Override
  public void call(EnunciateContext context) {
    if (this.jaxbModule == null || this.jaxbModule.getJaxbContext() == null || this.jaxbModule.getJaxbContext().getSchemas().isEmpty()) {
      info("No JAXB XML data types: C# XML client will not be generated.");
      return;
    }

    if (usesUnmappableElements()) {
      warn("Web service API makes use of elements that cannot be handled by the C# XML client. C# XML client will not be generated.");
      return;
    }

    List<String> namingConflicts = JAXBCodeErrors.findConflictingAccessorNamingErrors(this.jaxbModule.getJaxbContext());
    if (namingConflicts != null && !namingConflicts.isEmpty()) {
      error("JAXB naming conflicts have been found:");
      for (String namingConflict : namingConflicts) {
        error(namingConflict);
      }
      error("These naming conflicts are often between the field and it's associated property, in which case you need to use one or two of the following strategies to avoid the conflicts:");
      error("1. Explicitly exclude one or the other.");
      error("2. Put the annotations on the property instead of the field.");
      error("3. Tell JAXB to use a different process for detecting accessors using the @XmlAccessorType annotation.");
      throw new EnunciateException("JAXB naming conflicts detected.");
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

  protected boolean usesUnmappableElements() {
    boolean usesUnmappableElements = false;

    if (this.jaxwsModule != null && this.jaxwsModule.getJaxwsContext() != null) {
      for (EndpointInterface ei : this.jaxwsModule.getJaxwsContext().getEndpointInterfaces()) {
        Map<String, javax.lang.model.element.Element> paramsByName = new HashMap<String, javax.lang.model.element.Element>();
        for (WebMethod webMethod : ei.getWebMethods()) {
          for (WebParam webParam : webMethod.getWebParameters()) {
            //no out or in/out non-header parameters!
            if (webParam.isHeader()) {
              //unique parameter names for all header parameters of a given ei
              javax.lang.model.element.Element conflict = paramsByName.put(webParam.getElementName(), webParam);
              if (conflict != null) {
                warn("%s: C# requires that all header parameters defined in the same endpoint interface have unique names. This parameter conflicts with the one at %s.", positionOf(webParam), positionOf(conflict));
                usesUnmappableElements = true;
              }

              DecoratedTypeMirror paramType = (DecoratedTypeMirror) webParam.getType();
              if (paramType.isCollection()) {
                warn("%s: C# can't handle header parameters that are collections.", positionOf(webParam));
                usesUnmappableElements = true;
              }

            }
            else if (webParam.getMode() != javax.jws.WebParam.Mode.IN) {
              warn("%s: C# doesn't support non-header parameters of mode %s.", positionOf(webParam), webParam.getMode());
              usesUnmappableElements = true;
            }

            //parameters/results can't be maps
            if (webParam.getType() instanceof MapType) {
              warn("%s: C# can't handle parameter types that are maps.", positionOf(webParam));
              usesUnmappableElements = true;
            }
          }

          //web result cannot be a header.
          if (webMethod.getWebResult().isHeader()) {
            javax.lang.model.element.Element conflict = paramsByName.put(webMethod.getWebResult().getElementName(), webMethod);
            if (conflict != null) {
              warn("%s: C# requires that all header parameters defined in the same endpoint interface have unique names. This return parameter conflicts with the one at %s.", positionOf(webMethod), positionOf(conflict));
              usesUnmappableElements = true;
            }
          }

          if (webMethod.getWebResult().getType() instanceof MapType) {
            warn("%s: C# can't handle return types that are maps.", positionOf(webMethod));
            usesUnmappableElements = true;
          }

          if (ElementUtils.capitalize(webMethod.getClientSimpleName()).equals(ei.getClientSimpleName())) {
            warn("%s: C# can't handle methods that are of the same name as their containing class. Either rename the method, or use the @org.codehaus.enunciate.ClientName annotation to rename the method (or type) on the client-side.", positionOf(webMethod));
            usesUnmappableElements = true;
          }
        }
      }
    }

    if (this.jaxbModule != null && this.jaxbModule.getJaxbContext() != null && !this.jaxbModule.getJaxbContext().getSchemas().isEmpty()) {
      for (SchemaInfo schemaInfo : this.jaxbModule.getJaxbContext().getSchemas().values()) {
        for (TypeDefinition complexType : schemaInfo.getTypeDefinitions()) {
          for (Attribute attribute : complexType.getAttributes()) {
            if (ElementUtils.capitalize(attribute.getClientSimpleName()).equals(complexType.getClientSimpleName())) {
              warn("%s: C# can't handle properties/fields that are of the same name as their containing class. Either rename the property/field, or use the @com.webcohesion.enunciate.metadata.ClientName annotation to rename the property/field on the client-side.", positionOf(attribute));
              usesUnmappableElements = true;
            }
          }

          if (complexType.getValue() != null) {
            if (ElementUtils.capitalize(complexType.getValue().getClientSimpleName()).equals(complexType.getClientSimpleName())) {
              warn("%s: C# can't handle properties/fields that are of the same name as their containing class. Either rename the property/field, or use the @com.webcohesion.enunciate.metadata.ClientName annotation to rename the property/field on the client-side.", positionOf(complexType.getValue()));
              usesUnmappableElements = true;
            }
          }

          for (Element element : complexType.getElements()) {
            if (ElementUtils.capitalize(element.getClientSimpleName()).equals(complexType.getClientSimpleName())) {
              warn("%s: C# can't handle properties/fields that are of the same name as their containing class. Either rename the property/field, or use the @com.webcohesion.enunciate.metadata.ClientName annotation to rename the property/field on the client-side.", positionOf(element));
              usesUnmappableElements = true;
            }

            if (element.getAccessorType() instanceof MapType && !element.isAdapted()) {
              warn("%s: The C# client doesn't have a built-in way of serializing a Map. Use @XmlJavaTypeAdapter to supply your own adapter for the Map.", positionOf(element));
              usesUnmappableElements = true;
            }
          }

          if (complexType instanceof EnumTypeDefinition) {
            List<VariableElement> enums = complexType.enumValues();
            for (VariableElement enumItem : enums) {
              String simpleName = enumItem.getSimpleName().toString();
              ClientName clientNameInfo = enumItem.getAnnotation(ClientName.class);
              if (clientNameInfo != null) {
                simpleName = clientNameInfo.value();
              }

              if ("event".equals(simpleName)) {
                warn("%s: C# can't handle an enum constant named 'Event'. Either rename the enum constant, or use the @com.webcohesion.enunciate.metadata.ClientName annotation to rename it on the client-side.", positionOf(enumItem));
                usesUnmappableElements = true;
              }
              else if (simpleName.equals(complexType.getClientSimpleName())) {
                warn("C# can't handle properties/fields that are of the same name as their containing class. Either rename the property/field, or use the @com.webcohesion.enunciate.metadata.ClientName annotation to rename the property/field on the client-side.", positionOf(enumItem));
                usesUnmappableElements = true;
              }
            }
          }

          if (ElementUtils.isMap(complexType)) {
            warn("%s: C# client doesn't handles types that implement java.util.Map. Use @XmlJavaTypeAdapter to supply your own adapter for the Map.", positionOf(complexType));
            usesUnmappableElements = true;
          }
        }
      }
    }

    return usesUnmappableElements;
  }

  private File generateSources(Map<String, String> packageToNamespaceConversions) {
    File srcDir = getSourceDir();
    srcDir.mkdirs();

    Map<String, Object> model = new HashMap<String, Object>();

    ClientPackageForMethod namespaceFor = new ClientPackageForMethod(packageToNamespaceConversions, this.context);
    Collection<WsdlInfo> wsdls = new ArrayList<WsdlInfo>();
    if (this.jaxwsModule != null) {
      wsdls = this.jaxwsModule.getJaxwsContext().getWsdls().values();
    }
    model.put("wsdls", wsdls);
    EnunciateJaxbContext jaxbContext = this.jaxbModule.getJaxbContext();
    model.put("schemas", jaxbContext.getSchemas().values());
    model.put("baseUri", this.enunciate.getConfiguration().getApplicationRoot());
    model.put("generatedCodeLicense", this.enunciate.getConfiguration().readGeneratedCodeLicenseFile());
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
    model.put("file", new FileDirective(srcDir, this.enunciate.getLogger()));

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

          if (compileExectuable == null) {
            //try the "mcs" command (Mono)
            debug("Attempting to execute command \"mcs /help\" for the current environment (%s).", osName);
            try {
              Process process = new ProcessBuilder("mcs", "/help").redirectErrorStream(true).start();
              InputStream in = process.getInputStream();
              byte[] buffer = new byte[1024];
              int len = in.read(buffer);
              while (len >- 0) {
                len = in.read(buffer);
              }

              int exitCode = process.waitFor();
              if (exitCode != 0) {
                debug("Command \"mcs /help\" failed with exit code " + exitCode + ".");
              }
              else {
                compileExectuable = "mcs";
                debug("C# compile executable to be used: %s", compileExectuable);
              }
            }
            catch (Throwable e) {
              debug("Command \"mcs /help\" failed (" + e.getMessage() + ").");
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
      debug("Skipping C# compile because a compile executable was neither found nor provided.  The C# bundle will only include the sources.");
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

    URL res = CSharpXMLClientModule.class.getResource("library_description.fmt");
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
    configuration.setObjectWrapper(new CSharpXMLClientObjectWrapper());
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
    if (this.jaxwsModule != null && this.jaxwsModule.getJaxwsContext() != null) {
      for (EndpointInterface ei : this.jaxwsModule.getJaxwsContext().getEndpointInterfaces()) {
        for (WebMethod method : ei.getWebMethods()) {
          if (method.getAnnotation(DocumentationExample.class) != null && !method.getAnnotation(DocumentationExample.class).exclude()) {
            return method;
          }
          else if (method.getJavaDoc().get("documentationExample") != null) {
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
        if (SyntaxImpl.SYNTAX_LABEL.equals(mediaTypeDescriptor.getSyntax())) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean hasXmlRequestEntity(Method method) {
    if (method.getRequestEntity() != null) {
      for (MediaTypeDescriptor mediaTypeDescriptor : method.getRequestEntity().getMediaTypes()) {
        if (SyntaxImpl.SYNTAX_LABEL.equals(mediaTypeDescriptor.getSyntax())) {
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
    return this.config.getString("[@bundleFileName]", getSlug() + "-" + getName() + ".zip");
  }

  /**
   * The name of the generated C# dll.
   *
   * @return The name of the generated C# file.
   */
  protected String getDLLFileName() {
    return this.config.getString("[@DLLFileName]", getSlug() + ".dll");
  }

  /**
   * The name of the generated C# xml documentation.
   *
   * @return The name of the generated C# xml documentation.
   */
  protected String getDocXmlFileName() {
    return this.config.getString("[@docXmlFileName]", getSlug() + "-docs.xml");
  }

  /**
   * The name of the generated C# source file.
   *
   * @return The name of the generated C# source file.
   */
  protected String getSourceFileName() {
    return this.config.getString("[@sourceFileName]", getSlug() + ".cs");
  }

  /**
   * Get a template URL for the template of the given name.
   *
   * @param template The specified template.
   * @return The URL to the specified template.
   */
  protected URL getTemplateURL(String template) {
    return CSharpXMLClientModule.class.getResource(template);
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
  public String getSlug() {
    return this.config.getString("[@slug]", this.enunciate.getConfiguration().getSlug());
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
