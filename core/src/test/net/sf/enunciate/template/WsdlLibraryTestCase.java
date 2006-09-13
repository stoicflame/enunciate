package net.sf.enunciate.template;

import com.sun.mirror.declaration.TypeDeclaration;
import freemarker.template.TemplateException;
import net.sf.enunciate.EnunciateException;
import net.sf.enunciate.config.WsdlInfo;
import net.sf.enunciate.contract.jaxws.EndpointInterface;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Test case for the wsdl libraries.
 *
 * @author Ryan Heaton
 */
public class WsdlLibraryTestCase extends EnunciateTemplateLibraryTestCase {

  /**
   * Invokes a libWsdl method.
   *
   * @param methodName The method name.
   * @param args       The args.
   * @return The output.
   */
  public String invokeLibWsdlMethod(String methodName, HashMap<String, Object> args) throws IOException, TemplateException, EnunciateException {
    return invokeLibraryMethod("wsdl.fmt", methodName, args);
  }

  /**
   * Invokes "processWsdl" method on the given wsdl info.
   *
   * @param wsdl The wsdl to output.
   * @return The output.
   */
  public String processWsdl(WsdlInfo wsdl) throws IOException, TemplateException, EnunciateException {
    HashMap<String, Object> args = new HashMap<String, Object>();
    args.put("wsdl", wsdl);

    return invokeLibWsdlMethod("processWsdl", args);
  }

  @Test
  public void testProcessWsdl() throws IOException, TemplateException, EnunciateException {
    WsdlInfo wsdl = new WsdlInfo() {
      @Override
      public Set<String> getImportedNamespaces() {
        return new HashSet<String>(Arrays.asList("http://that.com/", "http://theother.com"));
      }
    };
    TypeDeclaration declaration = getDeclaration("net.sf.enunciate.samples.services.SuperNoNamespaceWebServiceImpl");
    wsdl.setTargetNamespace("http://this.com");
    wsdl.getEndpointInterfaces().add(new EndpointInterface(declaration));
    System.out.println(processWsdl(wsdl));
  }

  protected HashMap<String, String> getMethodMocks() {
    HashMap<String, String> mocks = new HashMap<String, String>();
    mocks.put("processWsdl", "<processWsdl/>\n");
    mocks.put("processSchema", "<processSchema/>\n");
    mocks.put("processMessage", "<processMessage/>\n");
    mocks.put("processPortType", "<processPortType/>\n");
    mocks.put("processOperation", "<processOperation/>\n");
    mocks.put("processBindings", "<processBindings/>\n");
    mocks.put("processHTTPBinding", "<processHTTPBinding/>\n");
    mocks.put("processSOAPBinding", "<processSOAPBinding/>\n");
    mocks.put("processServices", "<processServices/>\n");
    mocks.put("processSOAPServicePort", "<processSOAPServicePort/>\n");
    mocks.put("processHTTPServicePort", "<processHTTPServicePort/>\n");
    return mocks;
  }


}
