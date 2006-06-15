package net.sf.enunciate.apt;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.InterfaceDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import net.sf.enunciate.config.SchemaInfo;
import net.sf.enunciate.config.WsdlInfo;
import net.sf.enunciate.contract.jaxb.RootElementDeclaration;
import net.sf.enunciate.contract.jaxb.TypeDefinition;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
    prefixMap.put("http://ws-i.org/profiles/basic/1.1/xsd", "wsi");

    Map<String, SchemaInfo> schemaMap = new HashMap<String, SchemaInfo>();
    Map<String, WsdlInfo> wsdlMap = new HashMap<String, WsdlInfo>();

    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel(prefixMap, schemaMap, wsdlMap);

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
    //todo: create a mechanism to report errors before doing any actions other than just throwing an exception on the first one.
    jaxwsValidator = new ExceptionThrowingJAXWSValidatorWrapper(jaxwsValidator);

    //todo: validate the jaxb types.
    //JAXBValidator jaxbValidator = null;

    Collection<TypeDeclaration> typeDeclarations = env.getTypeDeclarations();
    for (TypeDeclaration declaration : typeDeclarations) {
      if (isEndpointInterface(declaration)) {
        EndpointInterface endpointInterface = new EndpointInterface(declaration, jaxwsValidator);

        if (isVerbose()) {
          System.out.println(declaration.getQualifiedName() + " to be considered as an endpoint interface.");
        }

        model.add(endpointInterface);

      }
      else if (declaration instanceof ClassDeclaration) {
        //otherwise, treat it as a potential jaxb type.

        TypeDefinition typeDef = model.findOrCreateTypeDefinition((ClassDeclaration) declaration);
        if (typeDef != null) {
          if (isVerbose()) {
            System.out.println(declaration.getQualifiedName() + " to be considered as a type definition.");
          }

          model.add(typeDef);
          namespaces.add(typeDef.getTargetNamespace());
        }

        RootElementDeclaration rootElement = model.findOrCreateRootElementDeclaration((ClassDeclaration) declaration, typeDef);
        if (rootElement != null) {
          if (isVerbose()) {
            System.out.println(declaration.getQualifiedName() + " to be considered as a root element.");
          }

          model.add(rootElement);
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

}
