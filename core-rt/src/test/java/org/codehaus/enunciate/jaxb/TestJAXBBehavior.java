package org.codehaus.enunciate.jaxb;

import junit.framework.TestCase;

import javax.xml.bind.annotation.*;
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

  /**
   * tests the behavior of @XmlTransient on a field/property.
   */
  public void testTransientOnFieldOrProperty() throws Exception {
    TransientPerson person = new TransientPerson();
    person.field1 = "hi";
    person.field2 = "hello";

    Marshaller marshaller = JAXBContext.newInstance(TransientPerson.class).createMarshaller();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    marshaller.marshal(person, out);
    Unmarshaller unmarshaller = JAXBContext.newInstance(TransientPerson.class).createUnmarshaller();
    byte[] bytes = out.toByteArray();
//    System.out.println(new String(bytes, "utf-8"));
    TransientPerson deserializedPerson = (TransientPerson) unmarshaller.unmarshal(new ByteArrayInputStream(bytes));
    assertEquals("hi", deserializedPerson.field1);
    assertEquals("hello", deserializedPerson.field2);
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

  @XmlRootElement
  public static class TransientPerson {

    @XmlTransient
    private String field1;
    public String field2;

    public String getField1() {
      return field1;
    }

    public void setField1(String field1) {
      this.field1 = field1;
    }

    @XmlTransient
    public String getField2() {
      return field2;
    }

    public void setField2(String field2) {
      this.field2 = field2;
    }
  }

}
