package net.sf.enunciate.modules.xfire_client;

import junit.framework.TestCase;
import org.codehaus.xfire.service.ServiceInfo;
import org.codehaus.xfire.service.OperationInfo;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.fault.XFireFault;
import org.codehaus.xfire.exchange.InMessage;
import org.codehaus.xfire.exchange.OutMessage;
import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.util.stax.JDOMStreamReader;
import org.codehaus.xfire.util.stax.JDOMStreamWriter;
import org.codehaus.xfire.aegis.AegisBindingProvider;
import org.codehaus.xfire.aegis.MessageReader;
import org.codehaus.xfire.aegis.MessageWriter;
import org.codehaus.xfire.aegis.type.DefaultTypeMappingRegistry;
import org.codehaus.xfire.aegis.type.Type;
import org.jdom.Element;

import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import java.lang.reflect.Method;
import java.beans.PropertyDescriptor;
import java.beans.Introspector;
import java.util.List;
import java.util.Arrays;
import java.util.Comparator;

import net.sf.enunciate.modules.xfire_client.annotations.RequestWrapperAnnotation;
import net.sf.enunciate.modules.xfire_client.annotations.ResponseWrapperAnnotation;
import net.sf.enunciate.modules.xfire_client.jaxws.DummyMethod;
import net.sf.enunciate.modules.xfire_client.jaxws.DummyMethodResponse;

/**
 * @author Ryan Heaton
 */
public class TestEnunciatedClientOperationBinding extends TestCase {

  public void dummyMethod() {
  }

  /**
   * Tests getting the request info for a method.
   */
  public void testGetRequestInfoWithAnnotation() throws Exception {
    Method dummyMethod = getClass().getMethod("dummyMethod");
    ServiceInfo serviceInfo = new ServiceInfo(null, null);
    OperationInfo op = serviceInfo.addOperation(new QName("urn:some-ns", "some-op"), dummyMethod);

    ExplicitWebAnnotations annotations = new ExplicitWebAnnotations();
    annotations.method2RequestWrapper.put(getClass().getName() + "." + dummyMethod.getName(), new RequestWrapperAnnotation("req", "urn:req", getClass().getName()));
    EnunciatedClientOperationBinding binding = new EnunciatedClientOperationBinding(annotations, null, null) {
      @Override
      protected PropertyDescriptor[] loadOrderedProperties(Class wrapperClass) throws XFireFault {
        return null;
      }
    };

    EnunciatedClientOperationBinding.OperationBeanInfo info = binding.getRequestInfo(op);
    assertEquals(getClass(), info.getBeanClass());
    assertNull(info.getPropertyOrder());
  }

  /**
   * Tests getting the request info for a method, without annotation.
   */
  public void testGetRequestInfoWithOutAnnotation() throws Exception {
    Method currentMethod = getClass().getMethod("dummyMethod");
    ServiceInfo serviceInfo = new ServiceInfo(null, null);
    OperationInfo op = serviceInfo.addOperation(new QName("urn:some-ns", "some-op"), currentMethod);

    ExplicitWebAnnotations annotations = new ExplicitWebAnnotations();
    EnunciatedClientOperationBinding binding = new EnunciatedClientOperationBinding(annotations, null, null) {
      @Override
      protected PropertyDescriptor[] loadOrderedProperties(Class wrapperClass) throws XFireFault {
        return null;
      }
    };

    EnunciatedClientOperationBinding.OperationBeanInfo info = binding.getRequestInfo(op);
    assertEquals(DummyMethod.class, info.getBeanClass());
    assertNull(info.getPropertyOrder());
  }

  /**
   * Tests getting the response info for a method.
   */
  public void testGetResponseInfoWithAnnotation() throws Exception {
    Method currentMethod = getClass().getMethod("dummyMethod");
    ServiceInfo serviceInfo = new ServiceInfo(null, null);
    OperationInfo op = serviceInfo.addOperation(new QName("urn:some-ns", "some-op"), currentMethod);

    ExplicitWebAnnotations annotations = new ExplicitWebAnnotations();
    annotations.method2ResponseWrapper.put(getClass().getName() + "." + currentMethod.getName(), new ResponseWrapperAnnotation("req", "urn:req", getClass().getName()));
    EnunciatedClientOperationBinding binding = new EnunciatedClientOperationBinding(annotations, null, null) {
      @Override
      protected PropertyDescriptor[] loadOrderedProperties(Class wrapperClass) throws XFireFault {
        return null;
      }
    };

    EnunciatedClientOperationBinding.OperationBeanInfo info = binding.getResponseInfo(op);
    assertEquals(getClass(), info.getBeanClass());
    assertNull(info.getPropertyOrder());
  }

  /**
   * Tests getting the response info for a method, without annotation.
   */
  public void testGetResponseInfoWithOutAnnotation() throws Exception {
    Method currentMethod = getClass().getMethod("dummyMethod");
    ServiceInfo serviceInfo = new ServiceInfo(null, null);
    OperationInfo op = serviceInfo.addOperation(new QName("urn:some-ns", "some-op"), currentMethod);

    ExplicitWebAnnotations annotations = new ExplicitWebAnnotations();
    EnunciatedClientOperationBinding binding = new EnunciatedClientOperationBinding(annotations, null, null) {
      @Override
      protected PropertyDescriptor[] loadOrderedProperties(Class wrapperClass) throws XFireFault {
        return null;
      }
    };

    EnunciatedClientOperationBinding.OperationBeanInfo info = binding.getResponseInfo(op);
    assertEquals(DummyMethodResponse.class, info.getBeanClass());
    assertNull(info.getPropertyOrder());
  }

  /**
   * tests loading the ordered properties of a bean.
   */
  public void testLoadOrderedProperties() throws Exception {
    ExplicitWebAnnotations annotations = new ExplicitWebAnnotations();
    EnunciatedClientOperationBinding binding = new EnunciatedClientOperationBinding(annotations, null, null);
    try {
      binding.loadOrderedProperties(getClass());
      fail("should have thrown a fault because the property order wasn't set.");
    }
    catch (XFireFault xFireFault) {
      //fall through..
    }

    String[] propOrder = new String[] {"out", "of", "order", "and", "back", "in"};
    annotations.class2PropertyOrder.put(DummyMethod.class.getName(), propOrder);
    PropertyDescriptor[] pds = binding.loadOrderedProperties(DummyMethod.class);
    assertEquals(propOrder.length, pds.length);
    for (int i = 0; i < pds.length; i++) {
      assertEquals(propOrder[i], pds[i].getName());
    }

    propOrder = new String[] {"of", "order", "and", "back", "in"};
    annotations.class2PropertyOrder.put(DummyMethod.class.getName(), propOrder);
    try {
      binding.loadOrderedProperties(getClass());
      fail("should have thrown a fault because an unknown property was found.");
    }
    catch (XFireFault xFireFault) {
      //fall through..
    }
  }

  /**
   * tests reading the message.
   */
  public void testReadMessage() throws Exception {
    final DummyMethod messageObject = new DummyMethod();
    messageObject.setAnd((short) 1);
    messageObject.setBack(2);
    messageObject.setIn(3);
    messageObject.setOf(true);
    messageObject.setOrder("order");
    messageObject.setOut(4);

    ExplicitWebAnnotations annotations = new ExplicitWebAnnotations();
    EnunciatedClientOperationBinding.OperationBeanInfo operationInfo = new EnunciatedClientOperationBinding.OperationBeanInfo(DummyMethod.class, Introspector.getBeanInfo(DummyMethod.class, Object.class).getPropertyDescriptors());
    EnunciatedClientOperationBinding binding = new EnunciatedClientOperationBinding(annotations, null, operationInfo);
    MessageContext context = new MessageContext();
    Service service = new Service(new ServiceInfo(null, null));
    DefaultTypeMappingRegistry registry = new DefaultTypeMappingRegistry(true);
    service.setBindingProvider(new AegisBindingProvider(registry));
    service.setProperty(AegisBindingProvider.TYPE_MAPPING_KEY, registry.getDefaultTypeMapping());
    context.setService(service);
    registry.getDefaultTypeMapping().register(DummyMethod.class, new QName("hi"), new Type() {
      public Object readObject(MessageReader reader, MessageContext context) throws XFireFault {
        return messageObject;
      }

      public void writeObject(Object object, MessageWriter writer, MessageContext context) throws XFireFault {
      }
    });

    InMessage message = new InMessage();
    message.setXMLStreamReader(new JDOMStreamReader(new Element("none", "urn:none")));
    binding.readMessage(message, context);
    List params = (List) message.getBody();
    assertEquals(6, params.size());
    assertTrue(params.remove(new Short((short) 1)));
    assertTrue(params.remove(new Float(2)));
    assertTrue(params.remove(new Double(3)));
    assertTrue(params.remove(new Integer(4)));
    assertTrue(params.remove(new Boolean(true)));
    assertTrue(params.remove("order"));
  }

  /**
   * tests writing the message.
   */
  public void testWriteMessage() throws Exception {
    ExplicitWebAnnotations annotations = new ExplicitWebAnnotations();
    PropertyDescriptor[] pds = Introspector.getBeanInfo(DummyMethod.class, Object.class).getPropertyDescriptors();
    Arrays.sort(pds, new Comparator<PropertyDescriptor>() {
      public int compare(PropertyDescriptor o1, PropertyDescriptor o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    //property order will be: and, back, in, of, order, out

    EnunciatedClientOperationBinding.OperationBeanInfo operationInfo = new EnunciatedClientOperationBinding.OperationBeanInfo(DummyMethod.class, pds);
    EnunciatedClientOperationBinding binding = new EnunciatedClientOperationBinding(annotations, operationInfo, null);
    OutMessage outMessage = new OutMessage("uri:out");
    outMessage.setBody(new Object[] {new Short((short) 10), new Float(9), new Double(8), Boolean.FALSE, "dummy", new Integer(7)});
    MessageContext context = new MessageContext();
    Service service = new Service(new ServiceInfo(null, null));
    DefaultTypeMappingRegistry registry = new DefaultTypeMappingRegistry(true);
    service.setBindingProvider(new AegisBindingProvider(registry));
    service.setProperty(AegisBindingProvider.TYPE_MAPPING_KEY, registry.getDefaultTypeMapping());
    context.setService(service);
    final Holder<DummyMethod> holder = new Holder<DummyMethod>();
    registry.getDefaultTypeMapping().register(DummyMethod.class, new QName("hi"), new Type() {
      public Object readObject(MessageReader reader, MessageContext context) throws XFireFault {
        fail();
        return null;
      }

      public void writeObject(Object object, MessageWriter writer, MessageContext context) throws XFireFault {
        holder.value = (DummyMethod) object;
      }
    });

    binding.writeMessage(outMessage, new JDOMStreamWriter(new Element("message", "urn:testWriteMessage")), context);
    assertNotNull(holder.value);
    assertEquals(10, holder.value.getAnd());
    assertEquals(new Float(9), holder.value.getBack());
    assertEquals(new Double(8), holder.value.getIn());
    assertEquals(false, holder.value.isOf());
    assertEquals("dummy", holder.value.getOrder());
    assertEquals(7, holder.value.getOut());
  }
}
