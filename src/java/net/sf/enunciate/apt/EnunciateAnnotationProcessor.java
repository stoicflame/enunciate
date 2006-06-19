package net.sf.enunciate.apt;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.InterfaceDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import net.sf.enunciate.contract.jaxb.RootElementDeclaration;
import net.sf.enunciate.contract.jaxb.TypeDefinition;
import net.sf.enunciate.contract.jaxb.validation.JAXBValidator;
import net.sf.enunciate.contract.jaxws.EndpointInterface;
import net.sf.enunciate.contract.jaxws.validation.DefaultJAXWSValidator;
import net.sf.enunciate.contract.jaxws.validation.ExceptionThrowingJAXWSValidatorWrapper;
import net.sf.enunciate.contract.jaxws.validation.JAXWSValidator;
import net.sf.enunciate.template.freemarker.*;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import net.sf.jelly.apt.freemarker.FreemarkerProcessor;
import net.sf.jelly.apt.freemarker.FreemarkerTransform;

import javax.jws.WebService;
import java.net.URL;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
public class EnunciateAnnotationProcessor extends FreemarkerProcessor {

  public EnunciateAnnotationProcessor(URL template) {
    super(template);
  }

  //Inherited.
  @Override
  protected FreemarkerModel newRootModel() {
    //todo: read the jaxwsValidator types from the config.
    JAXWSValidator jaxwsValidator = new DefaultJAXWSValidator();
    //todo: create a mechanism to report errors before doing any actions other than just throwing an exception on the first one.
    jaxwsValidator = new ExceptionThrowingJAXWSValidatorWrapper(jaxwsValidator);

    //todo: validate the jaxb types.
    JAXBValidator jaxbValidator = null;

    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel(jaxbValidator, jaxwsValidator);
    model.put("prefix", new PrefixMethod());
    model.put("qname", new QNameMethod());

    AnnotationProcessorEnvironment env = Context.getCurrentEnvironment();
    Collection<TypeDeclaration> typeDeclarations = env.getTypeDeclarations();
    for (TypeDeclaration declaration : typeDeclarations) {
      if (isEndpointInterface(declaration)) {
        EndpointInterface endpointInterface = new EndpointInterface(declaration);

        if (isVerbose()) {
          System.out.println(declaration.getQualifiedName() + " to be considered as an endpoint interface.");
        }

        model.add(endpointInterface);
      }
      else if (declaration instanceof ClassDeclaration) {
        //otherwise, if it's a class, consider it a potential jaxb type.
        TypeDefinition typeDef = model.findOrCreateTypeDefinition((ClassDeclaration) declaration);
        if (typeDef != null) {
          if (isVerbose()) {
            System.out.println(declaration.getQualifiedName() + " to be considered as a type definition.");
          }

          model.add(typeDef);
        }

        RootElementDeclaration rootElement = model.findOrCreateRootElementDeclaration((ClassDeclaration) declaration, typeDef);
        if (rootElement != null) {
          if (isVerbose()) {
            System.out.println(declaration.getQualifiedName() + " to be considered as a root element.");
          }

          model.add(rootElement);
        }
      }
    }

/*
    todo: read the config file into the prefixMap and schemaMap
    todo: but don't overwrite the constant namespaces
    String configFile = env.getOptions().get(EnunciateAnnotationProcessorFactory.CONFIG_OPTION);
    if (configFile != null) {
      try {
        FileInputStream stream = new FileInputStream(configFile);
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
*/

    return model;
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

  //Inherited.
  @Override
  protected Collection<FreemarkerTransform> getTransforms() {
    String namespace = Context.getCurrentEnvironment().getOptions().get(EnunciateAnnotationProcessorFactory.FM_LIBRARY_NS_OPTION);
    Collection<FreemarkerTransform> transforms = super.getTransforms();
    transforms.add(new ForEachBindingTypeTransform(namespace));
    transforms.add(new ForEachEndpointInterfaceTransform(namespace));
    transforms.add(new ForEachThrownWebFaultTransform(namespace));
    transforms.add(new ForEachWebFaultTransform(namespace));
    transforms.add(new ForEachWebMessageTransform(namespace));
    transforms.add(new ForEachWebMethodTransform(namespace));
    transforms.add(new ForEachWsdlTransform(namespace));
    transforms.add(new ForEachSchemaTransform(namespace));
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

}
