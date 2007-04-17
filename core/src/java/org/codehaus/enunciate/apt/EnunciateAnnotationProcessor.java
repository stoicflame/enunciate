/*
 * Copyright 2006 Web Cohesion
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

package org.codehaus.enunciate.apt;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Messager;
import com.sun.mirror.declaration.*;
import com.sun.mirror.type.ClassType;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModelException;
import org.codehaus.enunciate.EnunciateException;
import org.codehaus.enunciate.XmlTransient;
import org.codehaus.enunciate.config.EnunciateConfiguration;
import org.codehaus.enunciate.contract.jaxb.*;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.rest.RESTEndpoint;
import org.codehaus.enunciate.contract.validation.*;
import org.codehaus.enunciate.main.Enunciate;
import org.codehaus.enunciate.modules.DeploymentModule;
import org.codehaus.enunciate.template.freemarker.*;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.decorations.DeclarationDecorator;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import net.sf.jelly.apt.decorations.declaration.DecoratedTypeDeclaration;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import net.sf.jelly.apt.freemarker.FreemarkerProcessor;
import net.sf.jelly.apt.freemarker.FreemarkerTransform;

import javax.jws.WebService;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;

/**
 * Root annotation processor for enunciate.  Initializes the model and signals the modules to generate.
 * <p/>
 * Even though it extends <code>FreemarkerProcessor</code>, it does not process any Freemarker templates directly.  It extends
 * <code>FreemarkerProcessor</code> only to inherit certain functionality.
 *
 * @author Ryan Heaton
 */
public class EnunciateAnnotationProcessor extends FreemarkerProcessor {

  private EnunciateException ee = null;
  private IOException ioe = null;
  private RuntimeException re = null;
  private final Enunciate enunciate;

  public EnunciateAnnotationProcessor() throws EnunciateException {
    this(new Enunciate(new String[0], new EnunciateConfiguration()));
  }

  public EnunciateAnnotationProcessor(Enunciate enunciate) throws EnunciateException {
    super(null);

    if (enunciate == null) {
      throw new EnunciateException("An enunciate mechanism must be specified.");
    }
    else if (enunciate.getConfig() == null) {
      throw new EnunciateException("An enunciate mechanism must have a configuration (even if its the default configuration).");
    }

    this.enunciate = enunciate;

  }

  @Override
  public void process() {
    try {
      getRootModel();

      EnunciateConfiguration config = this.enunciate.getConfig();
      for (DeploymentModule module : config.getEnabledModules()) {
        debug("Invoking %s step for module %s", Enunciate.Target.GENERATE, module.getName());
        module.step(Enunciate.Target.GENERATE);
      }
    }
    catch (TemplateException e) {
      process(e);
    }
    catch (IOException e) {
      process(e);
    }
    catch (EnunciateException e) {
      process(e);
    }
    catch (RuntimeException re) {
      process(re);
    }
  }

  /**
   * Getting the root model pulls all endpoint interfaces and schema types out of the source
   * base, adds the classes specified to be included, and adds them to the model, then validates
   * the model.
   *
   * @return The root model.
   */
  @Override
  protected EnunciateFreemarkerModel getRootModel() throws TemplateModelException {
    EnunciateFreemarkerModel model = (EnunciateFreemarkerModel) super.getRootModel();

    AnnotationProcessorEnvironment env = Context.getCurrentEnvironment();
    Collection<TypeDeclaration> typeDeclarations = env.getTypeDeclarations();
    debug("Reading classes to enunciate...");
    for (TypeDeclaration declaration : typeDeclarations) {
      final boolean isEndpointInterface = isEndpointInterface(declaration);
      final boolean isRESTEndpoint = isRESTEndpoint(declaration);
      if (isEndpointInterface || isRESTEndpoint) {
        if (isEndpointInterface) {
          EndpointInterface endpointInterface = new EndpointInterface(declaration);
          info("%s to be considered as an endpoint interface.", declaration.getQualifiedName());
          model.add(endpointInterface);
        }

        if (isRESTEndpoint) {
          RESTEndpoint restEndpoint = new RESTEndpoint((ClassDeclaration) declaration);
          info("%s to be considered as a REST endpoint.", declaration.getQualifiedName());
          model.add(restEndpoint);
        }
      }
      else if (isPotentialSchemaType(declaration)) {
        TypeDefinition typeDef = createTypeDefinition((ClassDeclaration) declaration);
        loadTypeDef(typeDef, model);
      }
      else {
        debug("%s is neither an endpoint interface, rest endpoint, or JAXB schema type, so it'll be ignored.", declaration.getQualifiedName());
      }
    }

    //read in any JAXB import classes.
    EnunciateConfiguration config = this.enunciate.getConfig();
    debug("Reading JAXB import classes...");
    for (String jaxbClassImport : config.getJaxbClassImports()) {
      TypeDeclaration typeDeclaration = env.getTypeDeclaration(jaxbClassImport);
      if (typeDeclaration == null) {
        throw new IllegalStateException("JAXB import class not found: " + jaxbClassImport);
      }

      if (!(typeDeclaration instanceof ClassDeclaration)) {
        throw new IllegalStateException("Illegal JAXB import class (not a class): " + jaxbClassImport);
      }

      TypeDefinition typeDef = createTypeDefinition((ClassDeclaration) typeDeclaration);
      loadTypeDef(typeDef, model);
    }

    //todo: support jaxb package imports (use jaxb.index or ObjectFactory.class)

    //override any namespace prefix mappings as specified in the config.
    for (String ns : config.getNamespacesToPrefixes().keySet()) {
      String prefix = config.getNamespacesToPrefixes().get(ns);
      String old = model.getNamespacesToPrefixes().put(ns, prefix);
      debug("Replaced namespace prefix %s with %s for namespace %s as specified in the config.", old, prefix, ns);
    }

    validate(model);

    return model;
  }

  /**
   * Loads the specified type definition into the specified model.
   *
   * @param typeDef The type definition to load.
   * @param model The model into which to load the type definition.
   */
  protected void loadTypeDef(TypeDefinition typeDef, EnunciateFreemarkerModel model) {
    if (typeDef != null) {
      info("%s to be considered as a %s (qname:{%s}%s).",
           typeDef.getQualifiedName(), typeDef.getClass().getSimpleName(),
           typeDef.getNamespace() == null ? "" : typeDef.getNamespace(),
           typeDef.getName() == null ? "(anonymous)" : typeDef.getName());

      model.add(typeDef);

      RootElementDeclaration rootElement = createRootElementDeclaration((ClassDeclaration) typeDef.getDelegate(), typeDef);
      if (rootElement != null) {
        info("%s to be considered as a root element", typeDef.getQualifiedName());
        model.add(rootElement);
      }
    }
  }

  /**
   * Validate the model. This action uses the validator specified in the config as well as any
   * module-specific validators.  Errors and warnings are printed using the APT messager.
   *
   * @param model The model to validate.
   * @throws ModelValidationException If any validation errors are encountered.
   */
  protected void validate(EnunciateFreemarkerModel model) throws ModelValidationException {
    debug("Validating the model...");
    AnnotationProcessorEnvironment env = Context.getCurrentEnvironment();
    ValidatorChain validator = new ValidatorChain();
    EnunciateConfiguration config = this.enunciate.getConfig();
    validator.addValidator(config.getValidator());
    debug("Default validator added to the chain.");
    for (DeploymentModule module : config.getEnabledModules()) {
      Validator moduleValidator = module.getValidator();
      if (moduleValidator != null) {
        validator.addValidator(moduleValidator);
        debug("Validator for module %s added to the chain.", module.getName());
      }
    }

    ValidationResult validationResult = validate(model, validator);

    if (validationResult.hasWarnings()) {
      info("Validation result has warnings.");
      for (ValidationMessage warning : validationResult.getWarnings()) {
        env.getMessager().printWarning(warning.getPosition(), warning.getText());
      }
    }

    if (validationResult.hasErrors()) {
      info("Validation result has errors.");
      for (ValidationMessage error : validationResult.getErrors()) {
        env.getMessager().printError(error.getPosition(), error.getText());
      }

      throw new ModelValidationException();
    }
  }

  //Inherited.
  @Override
  protected FreemarkerModel newRootModel() {
    return new EnunciateFreemarkerModel();
  }

  /**
   * Whether the specified declaration is a potential schema type.
   *
   * @param declaration The declaration to determine whether it's a potential schema type.
   * @return Whether the specified declaration is a potential schema type.
   */
  protected boolean isPotentialSchemaType(TypeDeclaration declaration) {
    if (!(declaration instanceof ClassDeclaration)) {
      debug("%s isn't a potential schema type because it's not a class.", declaration.getQualifiedName());
      return false;
    }

    Collection<AnnotationMirror> annotationMirrors = declaration.getAnnotationMirrors();
    boolean explicitXMLTypeOrElement = false;
    for (AnnotationMirror mirror : annotationMirrors) {
      AnnotationTypeDeclaration annotationDeclaration = mirror.getAnnotationType().getDeclaration();
      if (annotationDeclaration != null) {
        String fqn = annotationDeclaration.getQualifiedName();
        //exclude all XmlTransient types and all jaxws types.
        if ("org.codehaus.enunciate.XmlTransient".equals(fqn)
          || "javax.xml.bind.annotation.XmlTransient".equals(fqn)
          || (fqn.startsWith("javax.xml.ws")
          || (fqn.startsWith("javax.jws")))) {
          debug("%s isn't a potential schema type because of annotation %s.", declaration.getQualifiedName(), fqn);
          return false;
        }
        else {
          explicitXMLTypeOrElement = ("javax.xml.bind.annotation.XmlType".equals(fqn))
            || ("javax.xml.bind.annotation.XmlRootElement".equals(fqn));
        }
      }
    }

    return explicitXMLTypeOrElement || !isThrowable(declaration);
  }

  /**
   * Whether the specified declaration is throwable.
   *
   * @param declaration The declaration to determine whether it is throwable.
   * @return Whether the specified declaration is throwable.
   */
  protected boolean isThrowable(TypeDeclaration declaration) {
    if (!(declaration instanceof ClassDeclaration)) {
      return false;
    }
    else if (Throwable.class.getName().equals(declaration.getQualifiedName())) {
      return false;
    }
    else {
      ClassType superClass = ((ClassDeclaration) declaration).getSuperclass();
      return ((DecoratedTypeMirror) TypeMirrorDecorator.decorate(superClass)).isInstanceOf(Throwable.class.getName());
    }
  }

  /**
   * Whether the specified declaration is a REST endpoint.
   *
   * @param declaration The declaration.
   * @return Whether the declaration is a REST endpoint.
   */
  public boolean isRESTEndpoint(TypeDeclaration declaration) {
    return ((declaration instanceof ClassDeclaration) && (declaration.getAnnotation(org.codehaus.enunciate.rest.annotations.RESTEndpoint.class) != null));
  }

  /**
   * A quick check to see if a declaration is an endpoint interface.
   */
  public boolean isEndpointInterface(TypeDeclaration declaration) {
    WebService ws = declaration.getAnnotation(WebService.class);
    return (ws != null) && ((declaration instanceof InterfaceDeclaration)
      //if this is a class declaration, then it has an implicit endpoint interface if it doesn't reference another.
      || (ws.endpointInterface() == null) || ("".equals(ws.endpointInterface())));
  }

  /**
   * Find the type definition for a class given the class's declaration, or null if the class is xml transient.
   *
   * @param declaration The declaration.
   * @return The type definition.
   */
  public TypeDefinition createTypeDefinition(ClassDeclaration declaration) {
    if (isEnumType(declaration)) {
      return new EnumTypeDefinition((EnumDeclaration) declaration);
    }
    else if (isSimpleType(declaration)) {
      return new SimpleTypeDefinition(declaration);
    }
    else {
      //assume its a complex type.
      return new ComplexTypeDefinition(declaration);
    }
  }

  /**
   * Find or create the root element declaration for the specified type definition.
   *
   * @param declaration    The class declaration
   * @param typeDefinition The specified type definition.
   * @return The root element declaration.
   */
  public RootElementDeclaration createRootElementDeclaration(ClassDeclaration declaration, TypeDefinition typeDefinition) {
    if (!isRootSchemaElement(declaration)) {
      return null;
    }
    else {
      return new RootElementDeclaration(declaration, typeDefinition);
    }
  }

  /**
   * A quick check to see if a declaration defines a complex schema type.
   */
  protected boolean isComplexType(TypeDeclaration declaration) {
    return !(declaration instanceof InterfaceDeclaration) && !isEnumType(declaration) && !isSimpleType(declaration);
  }

  /**
   * A quick check to see if a declaration defines a enum schema type.
   */
  protected boolean isEnumType(TypeDeclaration declaration) {
    return (declaration instanceof EnumDeclaration);
  }

  /**
   * A quick check to see if a declaration defines a simple schema type.
   */
  protected boolean isSimpleType(TypeDeclaration declaration) {
    if (declaration instanceof InterfaceDeclaration) {
      if (declaration.getAnnotation(XmlType.class) != null) {
        throw new ValidationException(declaration.getPosition(), "An interface must not be annotated with @XmlType.");
      }

      return false;
    }

    if (isEnumType(declaration)) {
      return false;
    }

    GenericTypeDefinition typeDef = new GenericTypeDefinition((ClassDeclaration) declaration);
    return ((typeDef.getValue() != null) && (typeDef.getAttributes().isEmpty()) && (typeDef.getElements().isEmpty()));
  }

  /**
   * A quick check to see if a declaration defines a root schema element.
   */
  protected boolean isRootSchemaElement(TypeDeclaration declaration) {
    return declaration.getAnnotation(XmlRootElement.class) != null;
  }

  //Inherited.
  @Override
  public Collection<FreemarkerTransform> getTransforms() {
    String namespace = Context.getCurrentEnvironment().getOptions().get(EnunciateAnnotationProcessorFactory.FM_LIBRARY_NS_OPTION);
    Collection<FreemarkerTransform> transforms = super.getTransforms();

    //jaxws transforms.
    transforms.add(new ForEachBindingTypeTransform(namespace));
    transforms.add(new ForEachEndpointInterfaceTransform(namespace));
    transforms.add(new ForEachThrownWebFaultTransform(namespace));
    transforms.add(new ForEachWebFaultTransform(namespace));
    transforms.add(new ForEachWebMessageTransform(namespace));
    transforms.add(new ForEachWebMethodTransform(namespace));
    transforms.add(new ForEachWsdlTransform(namespace));

    //jaxb transforms.
    transforms.add(new ForEachSchemaTransform(namespace));

    //rest transforms.
    transforms.add(new ForEachRESTEndpointTransform(namespace));

    //set up the enunciate file transform.
    EnunciateFileTransform fileTransform = new EnunciateFileTransform(namespace);
    transforms.add(fileTransform);
    return transforms;
  }

  /**
   * Validates the model given a validator.
   *
   * @param model     The model to validate.
   * @param validator The validator.
   * @return The results of the validation.
   */
  protected ValidationResult validate(EnunciateFreemarkerModel model, Validator validator) {
    ValidationResult validationResult = new ValidationResult();

    for (EndpointInterface ei : model.endpointInterfaces) {
      debug("Validating %s...", ei.getQualifiedName());
      debug("Validating %s...", ei.getQualifiedName());
      validationResult.aggregate(validator.validateEndpointInterface(ei));
    }

    for (TypeDefinition typeDefinition : model.typeDefinitions) {
      debug("Validating %s...", typeDefinition.getQualifiedName());
      validationResult.aggregate(typeDefinition.accept(validator));
    }

    for (RootElementDeclaration rootElement : model.rootElements) {
      debug("Validating %s...", rootElement.getQualifiedName());
      validationResult.aggregate(validator.validateRootElement(rootElement));
    }

    debug("Validating the REST API...");
    validationResult.aggregate(validator.validateRESTAPI(model.getNounsToRESTMethods()));

    return validationResult;
  }

  //Inherited.
  @Override
  protected void process(TemplateException e) {
    if (!(e instanceof ModelValidationException)) {
      Messager messager = Context.getCurrentEnvironment().getMessager();
      StringWriter stackTrace = new StringWriter();
      e.printStackTrace(new PrintWriter(stackTrace));
      messager.printError(stackTrace.toString());
    }

    this.ee = new EnunciateException(e);
  }

  protected void process(EnunciateException e) {
    this.ee = e;
  }

  protected void process(IOException e) {
    this.ioe = e;
  }

  protected void process(RuntimeException e) {
    this.re = e;
  }

  /**
   * Throws any errors that occurred during processing.
   */
  public void throwAnyErrors() throws EnunciateException, IOException {
    if (this.ee != null) {
      throw this.ee;
    }
    else if (this.ioe != null) {
      throw this.ioe;
    }
    else if (this.re != null) {
      throw re;
    }
  }

  /**
   * Handle an info-level message.
   *
   * @param message The info message.
   * @param formatArgs The format args of the message.
   */
  public void info(String message, Object... formatArgs) {
    this.enunciate.info(message, formatArgs);
  }

  /**
   * Handle a debug-level message.
   *
   * @param message The debug message.
   * @param formatArgs The format args of the message.
   */
  public void debug(String message, Object... formatArgs) {
    this.enunciate.debug(message, formatArgs);
  }

  /**
   * Handle a warn-level message.
   *
   * @param message The warn message.
   * @param formatArgs The format args of the message.
   */
  public void warn(String message, Object... formatArgs) {
    this.enunciate.warn(message, formatArgs);
  }

}
