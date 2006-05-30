package net.sf.enunciate.apt;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.*;
import net.sf.enunciate.config.SchemaInfo;
import net.sf.enunciate.config.WsdlInfo;
import net.sf.enunciate.contract.ValidationException;
import net.sf.enunciate.contract.jaxb.ComplexTypeDefinition;
import net.sf.enunciate.contract.jaxb.GlobalElementDeclaration;
import net.sf.enunciate.contract.jaxb.SimpleTypeDefinition;
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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.net.URL;
import java.util.*;

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
    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("http://schemas.xmlsoap.org/wsdl/", "wsdl");
    prefixMap.put("http://schemas.xmlsoap.org/wsdl/http/", "http");
    prefixMap.put("http://schemas.xmlsoap.org/wsdl/mime/", "mime");
    prefixMap.put("http://schemas.xmlsoap.org/wsdl/soap/", "soap");
    prefixMap.put("http://schemas.xmlsoap.org/soap/encoding/", "soapenc");
    prefixMap.put("http://www.w3.org/2001/XMLSchema", "xsd");

    Map<String, SchemaInfo> schemaMap = new HashMap<String, SchemaInfo>();
    Map<String, WsdlInfo> wsdlMap = new HashMap<String, WsdlInfo>();

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

    HashSet<String> namespaces = new HashSet<String>();
    AnnotationProcessorEnvironment env = Context.getCurrentEnvironment();

    //todo: read the jaxwsValidator types from the config.
    JAXWSValidator jaxwsValidator = new DefaultJAXWSValidator();
    JAXBValidator jaxbValidator = null;

    //todo: create a mechanism to report errors before doing any actions other than just throwing an exception on the first one.
    jaxwsValidator = new ExceptionThrowingJAXWSValidatorWrapper(jaxwsValidator);

    Collection<TypeDeclaration> typeDeclarations = env.getTypeDeclarations();
    for (TypeDeclaration declaration : typeDeclarations) {
      if (isEndpointInterface(declaration)) {
        EndpointInterface endpointInterface = new EndpointInterface(declaration, jaxwsValidator);
        String namespace = endpointInterface.getTargetNamespace();

        if (isVerbose()) {
          System.out.println(declaration.getQualifiedName() + " to be considered as an endpoint interface.");
        }

        WsdlInfo wsdlInfo = wsdlMap.get(namespace);
        if (wsdlInfo == null) {
          wsdlInfo = new WsdlInfo();
          wsdlMap.put(namespace, wsdlInfo);
          wsdlInfo.setTargetNamespace(namespace);

          //todo: configure the schema info.
          //wsdlInfo.setSchemaInfo();

          //todo: configure whether to generate.
          //wsdlInfo.setGenerate();
        }
        wsdlInfo.getEndpointInterfaces().add(endpointInterface);

        //if it's a web service, add its referenced namespaces to the list of namespaces to consider.
        namespaces.addAll(endpointInterface.getReferencedNamespaces());
      }
      else {
        //otherwise, treat it as a potential jaxb type.

        TypeDefinition typeDef = null;
        if (isComplexType(declaration)) {
          ComplexTypeDefinition complexType = new ComplexTypeDefinition((ClassDeclaration) declaration, jaxbValidator);
          typeDef = complexType;
          String namespace = complexType.getTargetNamespace();

          if (isVerbose()) {
            System.out.println(declaration.getQualifiedName() + " to be considered as a complex type definition.");
          }

          SchemaInfo schemaInfo = schemaMap.get(namespace);
          if (schemaInfo == null) {
            schemaInfo = new SchemaInfo();
            schemaMap.put(namespace, schemaInfo);
            schemaInfo.setNamespace(namespace);
          }
          schemaInfo.getComplexTypes().add(complexType);

          //todo: add all referenced namespaces, too?
          //namespaces.addAll(complexType.getReferencedNamespaces());

          namespaces.add(complexType.getTargetNamespace());
        }
        else if (isSimpleType(declaration)) {
          SimpleTypeDefinition simpleType = new SimpleTypeDefinition((ClassDeclaration) declaration, jaxbValidator);
          typeDef = simpleType;
          String namespace = simpleType.getTargetNamespace();

          if (isVerbose()) {
            System.out.println(declaration.getQualifiedName() + " to be considered as a simple type definition.");
          }

          SchemaInfo schemaInfo = schemaMap.get(namespace);
          if (schemaInfo == null) {
            schemaInfo = new SchemaInfo();
            schemaMap.put(namespace, schemaInfo);
            schemaInfo.setNamespace(namespace);
          }
          schemaInfo.getSimpleTypes().add(simpleType);
          namespaces.add(simpleType.getTargetNamespace());
        }

        if ((typeDef != null) && (isRootSchemaElement(declaration))) {
          GlobalElementDeclaration rootElement = new GlobalElementDeclaration((ClassDeclaration) declaration, typeDef, jaxbValidator);

          if (isVerbose()) {
            System.out.println(declaration.getQualifiedName() + " to be considered as a simple type definition.");
          }

          String namespace = rootElement.getTargetNamespace();

          SchemaInfo schemaInfo = schemaMap.get(namespace);
          if (schemaInfo == null) {
            schemaInfo = new SchemaInfo();
            schemaMap.put(namespace, schemaInfo);
            schemaInfo.setNamespace(namespace);
          }

          schemaInfo.getGlobalElements().add(rootElement);
          namespaces.add(rootElement.getTargetNamespace());
        }
      }
    }

    int index = 0;
    for (String namespace : namespaces) {
      String prefix = "ns" + (++index);
      while (prefixMap.values().contains(prefix)) {
        prefix = "ns" + (++index);
      }

      if (!prefixMap.containsKey(namespace)) {
        prefixMap.put(namespace, prefix);
      }

      if (schemaMap.containsKey(namespace)) {
        SchemaInfo schemaInfo = schemaMap.get(namespace);

        //todo: set the file according to the config?
        schemaInfo.setFile(prefix + ".xsd");

        //todo: set the location according to the config?
        schemaInfo.setLocation(schemaInfo.getFile());

        //todo: set whether to generate?
        //schemaInfo.setGenerate();
      }

      if (wsdlMap.containsKey(namespace)) {
        wsdlMap.get(namespace).setFile(prefix + ".wsdl");
      }
    }

    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel(prefixMap, schemaMap, wsdlMap);
    model.put("prefix", new PrefixMethod());
    model.put("qname", new QNameMethod());
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

  /**
   * A quick check to see if a declaration defines a complex schema type.
   */
  public boolean isComplexType(TypeDeclaration declaration) {
    return !(declaration instanceof InterfaceDeclaration) && !isSimpleType(declaration);
  }

  /**
   * A quick check to see if a declaration defines a simple schema type.
   */
  public boolean isSimpleType(TypeDeclaration declaration) {
    if (declaration instanceof InterfaceDeclaration) {
      if (declaration.getAnnotation(XmlType.class) != null) {
        throw new ValidationException("An interface must not be annotated with @XmlType.");
      }

      return false;
    }

    Collection<MemberDeclaration> particles = new ArrayList<MemberDeclaration>();
    particles.addAll(declaration.getMethods());
    particles.addAll(declaration.getFields());

    return false;
  }

  /**
   * A quick check to see if a declaration defines a root schema element.
   */
  public boolean isRootSchemaElement(TypeDeclaration declaration) {
    return false;
  }

  /**
   * Whether a declaration is xml transient.
   *
   * @param declaration The declaration on which to determine xml transience.
   * @return Whether a declaration is xml transient.
   */
  protected boolean isXmlTransient(Declaration declaration) {
    return (declaration.getAnnotation(XmlTransient.class) != null);
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
   * Internal class used to inherit some functionality for determining whether a declaration is a simple type
   * or a complex type.
   */
  protected static class GenericTypeDefinition extends TypeDefinition {

    protected GenericTypeDefinition(ClassDeclaration delegate) {
      super(delegate);
    }
  }

}
