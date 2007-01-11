package net.sf.enunciate.modules.docs;

import junit.framework.TestCase;
import static net.sf.enunciate.EnunciateTestUtil.getAllJavaFiles;
import static net.sf.enunciate.InAPTTestCase.getInAPTClasspath;
import static net.sf.enunciate.InAPTTestCase.getSamplesDir;
import net.sf.enunciate.config.EnunciateConfiguration;
import net.sf.enunciate.main.Enunciate;
import net.sf.enunciate.modules.DeploymentModule;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.Arrays;

/**
 * @author Ryan Heaton
 */
public class TestGenerateDocsXml extends TestCase {

  /**
   * Tests the generation of the documentation.
   */
  public void testGenerateDocsXML() throws Exception {
    DocumentationDeploymentModule module = new DocumentationDeploymentModule();

    EnunciateConfiguration config = new EnunciateConfiguration(Arrays.asList((DeploymentModule) module));
    Enunciate enunciate = new Enunciate(getAllJavaFiles(getSamplesDir()));
    enunciate.setConfig(config);
    enunciate.setTarget(Enunciate.Target.GENERATE);
    enunciate.setClasspath(getInAPTClasspath());
    enunciate.execute();

    File docsXml = new File(enunciate.getGenerateDir(), "docs/docs.xml");
    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(docsXml);
    XPath xpath = XPathFactory.newInstance().newXPath();

    String packageDocsXPath = "/documentation/data/schema[@namespace='%s']/packages/package[@id='%s']/documentation";
    assertEquals("Here is some package documentation.", xpath.evaluate(String.format(packageDocsXPath, "urn:pckg1", "net.sf.enunciate.samples.docs.pckg1"), document).trim());
    assertEquals("Here is some more package documentation.", xpath.evaluate(String.format(packageDocsXPath, "urn:pckg1", "net.sf.enunciate.samples.docs.pckg2"), document).trim());

    String packageTagsXPath = "/documentation/data/schema[@namespace='%s']/packages/package[@id='%s']/tag[@name='%s']";
    assertEquals("sometag value", xpath.evaluate(String.format(packageTagsXPath, "urn:pckg1", "net.sf.enunciate.samples.docs.pckg2", "sometag"), document).trim());

    String typeDocsXPath = "/documentation/data/schema[@namespace='%s']/types/type[@id='%s']/documentation";
    assertEquals("Text for EnumOne", xpath.evaluate(String.format(typeDocsXPath, "urn:pckg1", "net.sf.enunciate.samples.docs.pckg1.EnumOne"), document).trim());
    
    //todo: finish up this testing...
  }

  /*
Sample xml generated:
<?xml version="1.0" encoding="UTF-8"?>
<documentation>
  <data>
    <schema namespace="urn:pckg1">
      <packages>
        <package id="net.sf.enunciate.samples.docs.pckg1">
          <documentation>
            <![CDATA[Here is some package documentation.]]>
          </documentation>
        </package>
        <package id="net.sf.enunciate.samples.docs.pckg2">
          <documentation>
            <![CDATA[Here is some more package documentation.]]>
          </documentation>
          <tag name="sometag"><![CDATA[sometag value]]></tag>
        </package>
      </packages>
      <types>
        <type name="enumOne" id="net.sf.enunciate.samples.docs.pckg1.EnumOne">
          <documentation>
            <![CDATA[Text for EnumOne]]>
          </documentation>
          <tag name="author"><![CDATA[Ryan Heaton]]></tag>
          <values>
            <item value="one">
              <documentation>
                 <![CDATA[description for enum value one]]>
              </documentation>
            </item>
            <item value="two">
              <documentation>
                 <![CDATA[description for enum value two]]>
              </documentation>
            </item>
            <item value="three">
              <documentation>
                 <![CDATA[description for enum value three]]>
              </documentation>
            </item>
          </values>
        </type>
        <type name="beanOne" id="net.sf.enunciate.samples.docs.pckg1.BeanOne">
          <documentation>
            <![CDATA[some text that can be used to describe
a class on multiple lines <a href="#hi">with some markup</a>
that should be included:
<ul>
<li>as raw text
<li>in the xml
<ul>]]>
          </documentation>
          <tag name="sometag"><![CDATA[some <b>value<b> that has some markup]]></tag>
          <tag name="author"><![CDATA[Ryan Heaton]]></tag>
          <elements>
            <element name="property1" typeNamespace="http://www.w3.org/2001/XMLSchema" typeName="string">
              <![CDATA[the text for property 1]]>
            </element>
            <element name="property2" typeNamespace="http://www.w3.org/2001/XMLSchema" typeName="string">
              <![CDATA[the text for property 2]]>
            </element>
            <element name="property3" typeNamespace="http://www.w3.org/2001/XMLSchema" typeName="string">
              <![CDATA[the text for property 3]]>
            </element>
          </elements>
        </type>
        <type name="beanTwo" id="net.sf.enunciate.samples.docs.pckg2.BeanTwo">
          <documentation>
            <![CDATA[Text for BeanTwo]]>
          </documentation>
          <tag name="author"><![CDATA[Ryan Heaton]]></tag>
          <elements>
            <element name="property1" typeNamespace="http://www.w3.org/2001/XMLSchema" typeName="string">
              <![CDATA[the text for property 1]]>
            </element>
            <element name="property2" typeNamespace="http://www.w3.org/2001/XMLSchema" typeName="string">
              <![CDATA[the text for property 2]]>
            </element>
            <element name="property3" typeNamespace="http://www.w3.org/2001/XMLSchema" typeName="string">
              <![CDATA[the text for property 3]]>
            </element>
          </elements>
        </type>
        <type name="beanThree" id="net.sf.enunciate.samples.docs.pckg2.BeanThree">
          <documentation>
            <![CDATA[Text for BeanThree]]>
          </documentation>
          <tag name="author"><![CDATA[Ryan Heaton]]></tag>
          <elements>
            <element name="property1" typeNamespace="http://www.w3.org/2001/XMLSchema" typeName="string">
              <![CDATA[the text for property 1]]>
            </element>
            <element name="property2" typeNamespace="http://www.w3.org/2001/XMLSchema" typeName="string">
              <![CDATA[the text for property 2]]>
            </element>
            <element name="property3" typeNamespace="http://www.w3.org/2001/XMLSchema" typeName="string">
              <![CDATA[the text for property 3]]>
            </element>
          </elements>
        </type>
      </types>
      <elements>
        <element type="net.sf.enunciate.samples.docs.pckg1.BeanOne">
          <documentation>
            <![CDATA[some text that can be used to describe
a class on multiple lines <a href="#hi">with some markup</a>
that should be included:
<ul>
<li>as raw text
<li>in the xml
<ul>]]>
          </documentation>
          <tag name="sometag"><![CDATA[some <b>value<b> that has some markup]]></tag>
          <tag name="author"><![CDATA[Ryan Heaton]]></tag>
        </element>
        <element type="net.sf.enunciate.samples.docs.pckg2.BeanTwo">
          <documentation>
            <![CDATA[Text for BeanTwo]]>
          </documentation>
          <tag name="author"><![CDATA[Ryan Heaton]]></tag>
        </element>
      </elements>
    </schema>
    <schema namespace="urn:pckg3">
      <packages>
        <package id="net.sf.enunciate.samples.docs.pckg3">
          <documentation>
            <![CDATA[]]>
          </documentation>
        </package>
      </packages>
      <types>
        <type name="beanFour" id="net.sf.enunciate.samples.docs.pckg3.BeanFour">
          <documentation>
            <![CDATA[Text for BeanFour]]>
          </documentation>
          <tag name="author"><![CDATA[Ryan Heaton]]></tag>
          <elements>
            <element name="property1" typeNamespace="http://www.w3.org/2001/XMLSchema" typeName="string">
              <![CDATA[the text for property 1]]>
            </element>
            <element name="property2" typeNamespace="http://www.w3.org/2001/XMLSchema" typeName="string">
              <![CDATA[the text for property 2]]>
            </element>
            <element name="property3" typeNamespace="http://www.w3.org/2001/XMLSchema" typeName="string">
              <![CDATA[the text for property 3]]>
            </element>
          </elements>
        </type>
      </types>
      <elements>
      </elements>
    </schema>
  </data>
  <soap>
    <endpointInterfaces>
      <endpointInterface id="net.sf.enunciate.samples.docs.pckg3.EIOne">
        <documentation>
          <![CDATA[documentation for EIOne.]]>
        </documentation>
        <tag name="author"><![CDATA[Ryan Heaton]]></tag>
        <method name="method1">
          <documentation>
            <![CDATA[docs for method1]]>
          </documentation>
        </method>
        <method name="method2">
          <documentation>
            <![CDATA[docs for method2
<i>should</i> be marked up however you like.]]>
          </documentation>
          <tag name="someother"><![CDATA[someother value]]></tag>
          <parameter name="param1" input="true" output="false">
            <![CDATA[docs for method2.param1]]>
          </parameter>
          <parameter name="param2" input="true" output="false">
            <![CDATA[docs for method2.param2]]>
          </parameter>
          <result>
            <![CDATA[return docs for method2]]>
          </result>
          <fault name="FaultOne">
            <![CDATA[]]>
          </fault>
        </method>
      </endpointInterface>
      <endpointInterface id="net.sf.enunciate.samples.docs.pckg3.RESTAndEI">
        <documentation>
          <![CDATA[docs for RESTAndEI]]>
        </documentation>
        <tag name="author"><![CDATA[Ryan Heaton]]></tag>
        <method name="getBeanOne">
          <documentation>
            <![CDATA[docs for getBeanOne]]>
          </documentation>
          <tag name="sometag"><![CDATA[sometag value]]></tag>
          <parameter name="id" input="true" output="false">
            <![CDATA[The id]]>
          </parameter>
          <result>
            <![CDATA[The bean
that has the return
value one multiple lines]]>
          </result>
        </method>
      </endpointInterface>
    </endpointInterfaces>
  </soap>
  <rest>
    <resources>
      <resource name="method1">
        <operation type="create" requiresResourceId="true">
          <documentation>
            <![CDATA[documentation for <span>method1</span>]]>
          </documentation>
          <parameter name="arg2">
            <![CDATA[docs for param1]]>
          </parameter>
          <parameter name="param2">
            <![CDATA[docs for param2]]>
          </parameter>
          <inValue typeNamespace="urn:pckg1" typeName="beanTwo">
            <![CDATA[docs for two]]>
          </inValue>
          <outValue typeNamespace="urn:pckg1" typeName="beanTwo">
            <![CDATA[docs for return]]>
          </outValue>
          <error code="500">
            <![CDATA[if something bad happens]]>
          </error>
        </operation>
      </resource>
    </resources>
  </rest>
</documentation>

   */

}
