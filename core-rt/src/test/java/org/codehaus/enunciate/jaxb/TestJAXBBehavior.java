package org.codehaus.enunciate.jaxb;

import junit.framework.TestCase;

import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

/**
 * @author Ryan Heaton
 */
public class TestJAXBBehavior extends TestCase {

  /**
   * tests the behavior of XML id/idref.
   */
  public void testXMLIDIDREF() throws Exception {
    Person person = new Person();
    person.id = "me";
    person.name = "me";
    person.father = new Person();
    person.father.id = "father";
    person.father.name = "father";
    person.mother = new Person();
    person.mother.id = "mother";
    person.mother.name = "mother";

    NonRefPerson nonRefperson = new NonRefPerson();
    nonRefperson.id = "me";
    nonRefperson.name = "me";
    nonRefperson.father = "father";
    nonRefperson.mother = "mother";

    Marshaller marshaller = JAXBContext.newInstance(NonRefPerson.class).createMarshaller();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    marshaller.marshal(nonRefperson, out);
    Unmarshaller unmarshaller = JAXBContext.newInstance(Person.class).createUnmarshaller();
    byte[] bytes = out.toByteArray();
//    System.out.println(new String(bytes, "utf-8"));
    Person deserializedPerson = (Person) unmarshaller.unmarshal(new ByteArrayInputStream(bytes));
    assertEquals("me", deserializedPerson.id);
    assertEquals("me", deserializedPerson.name);
    assertNull("The XMLID/IDREF tests have failed, meaning JAXB's doing some inference, and you may need add back the ID/IDREF warnings.", deserializedPerson.father);
    assertNull("The XMLID/IDREF tests have failed, meaning JAXB's doing some inference, and you may need add back the ID/IDREF warnings.", deserializedPerson.mother);
  }
  
  @XmlRootElement
  public static class Person {
    @XmlID
    public String id;
    @XmlElement
    public String name;
    @XmlIDREF
    public Person father;
    @XmlIDREF
    public Person mother;
  }

  @XmlRootElement(name = "person")
  public static class NonRefPerson {
    @XmlElement
    public String id;
    @XmlElement
    public String name;
    @XmlElement
    public String father;
    @XmlElement
    public String mother;
  }

}
