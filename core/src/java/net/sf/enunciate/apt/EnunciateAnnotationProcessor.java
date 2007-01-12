package net.sf.enunciate.apt;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Messager;
import com.sun.mirror.declaration.*;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModelException;
import net.sf.enunciate.EnunciateException;
import net.sf.enunciate.config.EnunciateConfiguration;
import net.sf.enunciate.contract.jaxb.*;
import net.sf.enunciate.contract.jaxws.EndpointInterface;
import net.sf.enunciate.contract.rest.RESTEndpoint;
import net.sf.enunciate.contract.validation.*;
import net.sf.enunciate.main.Enunciate;
import net.sf.enunciate.modules.DeploymentModule;
import net.sf.enunciate.template.freemarker.*;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import net.sf.jelly.apt.freemarker.FreemarkerProcessor;
import net.sf.jelly.apt.freemarker.FreemarkerTransform;

import javax.jws.WebService;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
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
  private final EnunciateConfiguration config;

  public EnunciateAnnotationProcessor() throws EnunciateException {
    this(new EnunciateConfiguration());
  }

  public EnunciateAnnotationProcessor(EnunciateConfiguration config) throws EnunciateException {
    super(null);

    if (config == null) {
      throw new EnunciateException("A configuration must be specified.");
    }

    this.config = config;
  }

  @Override
  public void process() {
    try {
      getRootModel();

      for (DeploymentModule module : this.config.getEnabledModules()) {
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

    Collection<TypeDeclaration> typeDeclarations = getTypeDeclarations();
    for (TypeDeclaration declaration : typeDeclarations) {
      if (isEndpointInterface(declaration)) {
        EndpointInterface endpointInterface = new EndpointInterface(declaration);

        if (isVerbose()) {
          System.out.println(declaration.getQualifiedName() + " to be considered as an endpoint interface.");
        }

        model.add(endpointInterface);
      }
      else if (isRESTEndpoint(declaration)) {
        //todo: support interfaces, too.
        RESTEndpoint restEndpoint = new RESTEndpoint((ClassDeclaration) declaration);

        if (isVerbose()) {
          System.out.println(declaration.getQualifiedName() + " to be considered as a REST endpoint.");
        }

        model.add(restEndpoint);
      }
      else if (isPotentialSchemaType(declaration)) {
        TypeDefinition typeDef = createTypeDefinition((ClassDeclaration) declaration);
        if (typeDef != null) {
          if (isVerbose()) {
            System.out.println(String.format("%s to be considered as a %s (qname:{%s}%s).",
                                             declaration.getQualifiedName(),
                                             typeDef.getClass().getSimpleName(),
                                             typeDef.getNamespace() == null ? "" : typeDef.getNamespace(),
                                             typeDef.getName()));
          }

          model.add(typeDef);

          RootElementDeclaration rootElement = createRootElementDeclaration((ClassDeclaration) declaration, typeDef);
          if (rootElement != null) {
            if (isVerbose()) {
              System.out.println(declaration.getQualifiedName() + " to be considered as a root element.");
            }

            model.add(rootElement);
          }
        }
      }
    }

    //override any namespace prefix mappings as specified in the config.
    for (String ns : this.config.getNamespacesToPrefixes().keySet()) {
      model.getNamespacesToPrefixes().put(ns, this.config.getNamespacesToPrefixes().get(ns));
    }

    //todo: read the config file for type declarations that aren't in the source base to preload as xml type definitions
    //todo: read the config file for packages of type declarations that aren't in the source base to preload as xml type definitions (use jaxb.index or ObjectFactory.class)

    validate(model);

    return model;
  }

  /**
   * Get the type declarations to consider for the root model.
   *
   * @return The list of type declarations to consider for the root model.
   */
  protected Collection<TypeDeclaration> getTypeDeclarations() {
    AnnotationProcessorEnvironment env = Context.getCurrentEnvironment();
    return env.getTypeDeclarations();
  }

  /**
   * Validate the model. This action uses the validator specified in the config as well as any
   * module-specific validators.  Errors and warnings are printed using the APT messager.
   *
   * @param model The model to validate.
   * @throws ModelValidationException If any validation errors are encountered.
   */
  protected void validate(EnunciateFreemarkerModel model) throws ModelValidationException {
    AnnotationProcessorEnvironment env = Context.getCurrentEnvironment();
    ValidatorChain validator = new ValidatorChain();
    validator.addValidator(this.config.getValidator());
    for (DeploymentModule module : this.config.getEnabledModules()) {
      Validator moduleValidator = module.getValidator();
      if (moduleValidator != null) {
        validator.addValidator(moduleValidator);
      }
    }

    ValidationResult validationResult = validate(model, validator);

    if (validationResult.hasWarnings()) {
      for (ValidationMessage warning : validationResult.getWarnings()) {
        env.getMessager().printWarning(warning.getPosition(), warning.getText());
      }
    }

    if (validationResult.hasErrors()) {
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
      return false;
    }

    if (declaration.getAnnotation(XmlTransient.class) != null) {
      return false;
    }

    Collection<AnnotationMirror> annotationMirrors = declaration.getAnnotationMirrors();
    for (AnnotationMirror mirror : annotationMirrors) {
      AnnotationTypeDeclaration annotationDeclaration = mirror.getAnnotationType().getDeclaration();
      if (annotationDeclaration != null) {
        if ((annotationDeclaration.getQualifiedName().startsWith("javax.xml.ws") || (annotationDeclaration.getQualifiedName().startsWith("javax.jws")))) {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * Whether the specified declaration is a REST endpoint.
   *
   * @param declaration The declaration.
   * @return Whether the declaration is a REST endpoint.
   */
  public boolean isRESTEndpoint(TypeDeclaration declaration) {
    return ((declaration instanceof ClassDeclaration) && (declaration.getAnnotation(net.sf.enunciate.rest.annotations.RESTEndpoint.class) != null));
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
   * Whether verbose output is requested.
   *
   * @return Whether verbose output is requested.
   */
  protected boolean isVerbose() {
    return Context.getCurrentEnvironment().getOptions().containsKey(EnunciateAnnotationProcessorFactory.VERBOSE_OPTION);
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
      validationResult.aggregate(validator.validateEndpointInterface(ei));
    }

    for (TypeDefinition typeDefinition : model.typeDefinitions) {
      validationResult.aggregate(typeDefinition.accept(validator));
    }

    for (RootElementDeclaration rootElement : model.rootElements) {
      validationResult.aggregate(validator.validateRootElement(rootElement));
    }

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

}
