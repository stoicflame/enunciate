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

package org.codehaus.enunciate.modules.csharp;

import freemarker.template.*;
import org.codehaus.enunciate.EnunciateException;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.config.SchemaInfo;
import org.codehaus.enunciate.config.WsdlInfo;
import org.codehaus.enunciate.contract.jaxb.TypeDefinition;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.jaxws.WebFault;
import org.codehaus.enunciate.contract.jaxws.WebMethod;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.main.Enunciate;
import org.codehaus.enunciate.modules.FreemarkerDeploymentModule;
import org.codehaus.enunciate.modules.csharp.config.CSharpRuleSet;
import org.codehaus.enunciate.modules.csharp.config.PackageNamespaceConversion;
import org.codehaus.enunciate.template.freemarker.ClientPackageForMethod;
import org.codehaus.enunciate.template.freemarker.SimpleNameWithParamsMethod;
import org.codehaus.enunciate.template.freemarker.AccessorOverridesAnotherMethod;
import org.codehaus.enunciate.util.TypeDeclarationComparator;
import org.apache.commons.digester.RuleSet;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.net.URL;

import net.sf.jelly.apt.decorations.JavaDoc;
import net.sf.jelly.apt.freemarker.FreemarkerJavaDoc;

/**
 * <h1>C# Module</h1>
 *
 * <p>The C# module generates C# client code for accessing the SOAP endpoints and compiles the code for .NET.</p>
 *
 * <p>The order of the JAXWS deployment module is 0, as it doesn't depend on any artifacts exported
 * by any other module.</p>
 *
 * <ul>
 * <li><a href="#config">configuration</a></li>
 * </ul>
 *
 * <h1><a name="config">Configuration</a></h1>
 *
 * <p>The C# module is configured with the "csharp" element under the "modules" element of the enunciate configuration file. It supports the following
 * attributes:</p>
 *
 * <ul>
 *   <li>The "label" attribute is the label for the C# API.  This is the name by which the files will be identified, producing [label].cs and [label].dll.
 *       By default the label is the same as the Enunciate project label.</li>
 * </ul>
 *
 * <h3>The "package-conversions" element</h3>
 *
 * <p>The "package-conversions" subelement of the "csharp" element is used to map packages from
 * the original API packages to C# namespaces.  This element supports an arbitrary number of
 * "convert" child elements that are used to specify the conversions.  These "convert" elements support
 * the following attributes:</p>
 *
 * <ul>
 * <li>The "from" attribute specifies the package that is to be converted.  This package will match
 * all classes in the package as well as any subpackages of the package.  This means that if "org.enunciate"
 * were specified, it would match "org.enunciate", "org.enunciate.api", and "org.enunciate.api.impl".</li>
 * <li>The "to" attribute specifies what the package is to be converted to.  Only the part of the package
 * that matches the "from" attribute will be converted.</li>
 * </ul>
 *
 * @author Ryan Heaton
 * @docFileName module_csharp.html
 */
public class CSharpDeploymentModule extends FreemarkerDeploymentModule {

  private boolean require = false;
  private String label = null;
  private String compileExecutable = null;
  private String compileCommand = "%s /target:library /out:%s /r:System.Web.Services %s";
  private String generateXmlDocsCommand = null;
  private String generateHtmlDocsCommand = null;
  private final Map<String, String> packageToNamespaceConversions = new HashMap<String, String>();

  public CSharpDeploymentModule() {
  }

  /**
   * @return "csharp"
   */
  @Override
  public String getName() {
    return "csharp";
  }

  @Override
  public void init(Enunciate enunciate) throws EnunciateException {
    super.init(enunciate);

    if (!super.isDisabled()) { //if we're explicitly disabled, we can ignore this...
      String compileExectuable = getCompileExecutable();
      if (compileExectuable == null) {
        String osName = System.getProperty("os.name");
        if (osName != null && osName.toUpperCase().contains("WINDOWS")) {
          //try the "csc" command on Windows environments.
          info("Attempting to execute command \"csc /help\" for the current environment (%s).", osName);
          try {
            Process process = new ProcessBuilder("csc", "/help").redirectErrorStream(true).start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
              info("Command \"csc /help\" failed with exit code " + exitCode + ".");
            }
            else {
              compileExectuable = "csc";
              info("C# compile executable to be used: csc");
            }
          }
          catch (Throwable e) {
            info("Command \"csc /help\" failed (" + e.getMessage() + ").");
          }
        }

        if (compileExectuable == null) {
          //try the "gmcs" command (Mono)
          info("Attempting to execute command \"gmcs /help\" for the current environment (%s).", osName);
          try {
            Process process = new ProcessBuilder("gmcs", "/help").redirectErrorStream(true).start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
              info("Command \"gmcs /help\" failed with exit code " + exitCode + ".");
            }
            else {
              compileExectuable = "gmcs";
              info("C# compile executable to be used: %s", compileExectuable);
            }
          }
          catch (Throwable e) {
            info("Command \"gmcs /help\" failed (" + e.getMessage() + ").");
          }
        }

        if (compileExectuable == null && isRequire()) {
          throw new EnunciateException("C# client code generation is required, but there was no valid compile executable found. " +
            "Please supply one in the configuration file, or set it up on your system path.");
        }
        
        setCompileExecutable(compileExectuable);
      }
    }
  }

  @Override
  public void initModel(EnunciateFreemarkerModel model) {
    super.initModel(model);

    if (!isDisabled()) {
      TreeSet<WebFault> allFaults = new TreeSet<WebFault>(new TypeDeclarationComparator());
      for (WsdlInfo wsdlInfo : model.getNamespacesToWSDLs().values()) {
        for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
          String pckg = ei.getPackage().getQualifiedName();
          if (!this.packageToNamespaceConversions.containsKey(pckg)) {
            this.packageToNamespaceConversions.put(pckg, packageToNamespace(pckg));
          }
          for (WebMethod webMethod : ei.getWebMethods()) {
            for (WebFault webFault : webMethod.getWebFaults()) {
              allFaults.add(webFault);
            }
          }
        }
      }

      for (WebFault webFault : allFaults) {
        String pckg = webFault.getPackage().getQualifiedName();
        if (!this.packageToNamespaceConversions.containsKey(pckg)) {
          this.packageToNamespaceConversions.put(pckg, packageToNamespace(pckg));
        }
      }
      
      for (SchemaInfo schemaInfo : model.getNamespacesToSchemas().values()) {
        for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
          String pckg = typeDefinition.getPackage().getQualifiedName();
          if (!this.packageToNamespaceConversions.containsKey(pckg)) {
            this.packageToNamespaceConversions.put(pckg, packageToNamespace(pckg));
          }
        }
      }
    }
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

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException {
    File genDir = getGenerateDir();
    if (!enunciate.isUpToDateWithSources(genDir)) {
      EnunciateFreemarkerModel model = getModel();
      model.put("namespaceFor", new ClientPackageForMethod(this.packageToNamespaceConversions));
      model.put("findRootElement", new FindRootElementMethod());
      ClientClassnameForMethod classnameFor = new ClientClassnameForMethod(this.packageToNamespaceConversions);
      model.put("classnameFor", classnameFor);
      model.put("simpleNameFor", new SimpleNameWithParamsMethod(classnameFor));
      model.put("csFileName", getSourceFileName());
      model.put("accessorOverridesAnother", new AccessorOverridesAnotherMethod());

      info("Generating the C# client classes...");
      URL apiTemplate = getTemplateURL("api.fmt");
      processTemplate(apiTemplate, model);
    }
    else {
      info("Skipping C# code generation because everything appears up-to-date.");
    }
  }

  @Override
  protected void doCompile() throws EnunciateException, IOException {
    File compileDir = getCompileDir();
    if (!enunciate.isUpToDateWithSources(compileDir)) {
      compileDir.mkdirs();
      String compileExecutable = getCompileExecutable();
      if (compileCommand == null) {
        throw new IllegalStateException("Somehow the \"compile\" step was invoked on the C# module without a valid compile executable.");
      }

      String compileCommand = getCompileCommand();
      if (compileCommand == null) {
        throw new IllegalStateException("Somehow the \"compile\" step was invoked on the C# module without a valid compile command.");
      }

      compileCommand = compileCommand.replace(' ', '\0'); //replace all spaces with the null character, so the command can be tokenized later.
      compileCommand = String.format(compileCommand, compileExecutable,
                                     new File(compileDir, getDLLFileName()).getAbsolutePath(),
                                     new File(getGenerateDir(), getSourceFileName()).getAbsolutePath());
      StringTokenizer tokenizer = new StringTokenizer(compileCommand, "\0"); //tokenize on the null character to preserve the spaces in the command.
      List<String> command = new ArrayList<String>();
      while (tokenizer.hasMoreElements()) {
        command.add((String) tokenizer.nextElement());
      }

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
    else {
      info("Skipping C# compile because everything appears up-to-date.");
    }
  }

  /**
   * The name of the generated C# dll.
   *
   * @return The name of the generated C# file.
   */
  protected String getDLLFileName() {
    String label = getLabel();
    if (label == null) {
      label = getEnunciate().getConfig().getLabel();
    }
    return label + ".dll";
  }

  /**
   * The name of the generated C# source file.
   *
   * @return The name of the generated C# source file.
   */
  protected String getSourceFileName() {
    String label = getLabel();
    if (label == null) {
      label = getEnunciate().getConfig().getLabel();
    }
    return label + ".cs";
  }

  @Override
  protected ObjectWrapper getObjectWrapper() {
    return new DefaultObjectWrapper() {
      @Override
      public TemplateModel wrap(Object obj) throws TemplateModelException {
        if (obj instanceof JavaDoc) {
          return new FreemarkerJavaDoc((JavaDoc) obj);
        }

        return super.wrap(obj);
      }
    };
  }

  /**
   * Get a template URL for the template of the given name.
   *
   * @param template The specified template.
   * @return The URL to the specified template.
   */
  protected URL getTemplateURL(String template) {
    return CSharpDeploymentModule.class.getResource(template);
  }

  /**
   * Whether the generate dir is up-to-date.
   *
   * @param genDir The generate dir.
   * @return Whether the generate dir is up-to-date.
   */
  protected boolean isUpToDate(File genDir) {
    return enunciate.isUpToDateWithSources(genDir);
  }

  /**
   * Whether to require the C# client code.
   *
   * @return Whether to require the C# client code.
   */
  public boolean isRequire() {
    return require;
  }

  /**
   * Whether to require the C# client code.
   *
   * @param require Whether to require the C# client code.
   */
  public void setRequire(boolean require) {
    this.require = require;
  }

  /**
   * The label for the C# API.
   *
   * @return The label for the C# API.
   */
  public String getLabel() {
    return label;
  }

  /**
   * The label for the C# API.
   *
   * @param label The label for the C# API.
   */
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * The path to the compile executable.
   *
   * @return The path to the compile executable.
   */
  public String getCompileExecutable() {
    return compileExecutable;
  }

  /**
   * The path to the compile executable.
   *
   * @param compileExecutable The path to the compile executable.
   */
  public void setCompileExecutable(String compileExecutable) {
    this.compileExecutable = compileExecutable;
  }

  /**
   * The C# compile command.
   *
   * @return The C# compile command.
   */
  public String getCompileCommand() {
    return compileCommand;
  }

  /**
   * The C# compile command.
   *
   * @param compileCommand The C# compile command.
   */
  public void setCompileCommand(String compileCommand) {
    this.compileCommand = compileCommand;
  }

  /**
   * The generate XML docs command.
   *
   * @return The generate XML docs command.
   */
  public String getGenerateXmlDocsCommand() {
    return generateXmlDocsCommand;
  }

  /**
   * The generate XML docs command.
   *
   * @param generateXmlDocsCommand The generate XML docs command.
   */
  public void setGenerateXmlDocsCommand(String generateXmlDocsCommand) {
    this.generateXmlDocsCommand = generateXmlDocsCommand;
  }

  /**
   * The generate HTML docs command.
   *
   * @return The generate HTML docs command.
   */
  public String getGenerateHtmlDocsCommand() {
    return generateHtmlDocsCommand;
  }

  /**
   * The generate HTML docs command.
   *
   * @param generateHtmlDocsCommand The generate HTML docs command.
   */
  public void setGenerateHtmlDocsCommand(String generateHtmlDocsCommand) {
    this.generateHtmlDocsCommand = generateHtmlDocsCommand;
  }

  /**
   * The package-to-namespace conversions.
   *
   * @return The package-to-namespace conversions.
   */
  public Map<String, String> getPackageToNamespaceConversions() {
    return packageToNamespaceConversions;
  }

  /**
   * Add a client package conversion.
   *
   * @param conversion The conversion to add.
   */
  public void addClientPackageConversion(PackageNamespaceConversion conversion) {
    String from = conversion.getFrom();
    String to = conversion.getTo();

    if (from == null) {
      throw new IllegalArgumentException("A 'from' attribute must be specified on a package-conversion element.");
    }

    if (to == null) {
      throw new IllegalArgumentException("A 'to' attribute must be specified on a package-conversion element.");
    }

    this.packageToNamespaceConversions.put(from, to);
  }

  @Override
  public RuleSet getConfigurationRules() {
    return new CSharpRuleSet();
  }

  @Override
  public Validator getValidator() {
    return new CSharpValidator();
  }

  // Inherited.
  @Override
  public boolean isDisabled() {
    if (super.isDisabled()) {
      return true;
    }
    else if (getModelInternal() != null && getModelInternal().getNamespacesToWSDLs().isEmpty() && getModelInternal().getNamespacesToSchemas().isEmpty()) {
      debug("C# module is disabled because there are no endpoint interfaces, nor any XML types.");
      return true;
    }
    else if (getCompileExecutable() == null) {
      debug("C# module is disabled there is no C# compile executable supplied, or Enunciate couldn't find one configured for the current environment.");
      return true;
    }

    return false;
  }
}
