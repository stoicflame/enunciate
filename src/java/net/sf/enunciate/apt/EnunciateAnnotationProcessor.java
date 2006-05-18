package net.sf.enunciate.apt;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.TypeDeclaration;
import net.sf.enunciate.config.SchemaInfo;
import net.sf.enunciate.config.WsdlInfo;
import net.sf.enunciate.contract.jaxws.EndpointInterface;
import net.sf.enunciate.contract.jaxws.validation.DefaultJAXWSValidator;
import net.sf.enunciate.contract.jaxws.validation.JAXWSValidator;
import net.sf.enunciate.template.freemarker.*;
import net.sf.enunciate.util.NamespaceUtils;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import net.sf.jelly.apt.freemarker.FreemarkerProcessor;
import net.sf.jelly.apt.freemarker.FreemarkerTransform;

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

    //todo: read the type of validator from the config.
    JAXWSValidator validator = new DefaultJAXWSValidator();

    Collection<TypeDeclaration> typeDeclarations = env.getTypeDeclarations();
    for (TypeDeclaration declaration : typeDeclarations) {
      if (validator.isEndpointInterface(declaration)) {
        EndpointInterface endpointInterface = new EndpointInterface(declaration, validator);
        String namespace = endpointInterface.getTargetNamespace();

        if (isVerbose()) {
          System.out.println(declaration.getQualifiedName() + " to be considered as an endpoint interface.");
        }

        WsdlInfo wsdlInfo = wsdlMap.get(namespace);
        if (wsdlInfo == null) {
          wsdlInfo = new WsdlInfo();
          wsdlMap.put(namespace, wsdlInfo);
          wsdlInfo.setTargetNamespace(namespace);
          wsdlInfo.setEndpointInterfaces(new ArrayList<EndpointInterface>());

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
        //otherwise, treat is as a potential jaxb type.
        String namespaceURI = NamespaceUtils.getNamespaceURI(declaration);

        if (isVerbose()) {
          System.out.println(String.format("Namespace {%s} found for %s", namespaceURI, declaration.getQualifiedName()));
        }

        if ((namespaceURI != null) && (!"".equals(namespaceURI))) {
          namespaces.add(namespaceURI);
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

      if (!schemaMap.containsKey(namespace)) {
        SchemaInfo info = new SchemaInfo();
        info.setNamespace(namespace);
        schemaMap.put(namespace, info);
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
