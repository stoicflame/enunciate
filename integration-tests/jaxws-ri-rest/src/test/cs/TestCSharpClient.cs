namespace Jaxws.Ri.Rest {

  using System;
  using Org.Codehaus.Enunciate.Samples.Genealogy.Services;
  using Org.Codehaus.Enunciate.Samples.Genealogy.Cite;
  using Org.Codehaus.Enunciate.Samples.Genealogy.Data;
  using System.Web.Services.Protocols;
  using System.Collections;
  using System.Collections.Generic;

  public class FullAPITest {

    public static void Main(string[] args) {
      SourceService sourceService = new SourceService();
      sourceService.Url = "http://localhost:8080/full/soap-services/sources/source";
      Source source = sourceService.GetSource("valid");
      Assert.IsNotNull(source);
      Assert.AreEqual("valid", source.Id);
      Assert.AreEqual("uri:some-uri", source.Link);
      Assert.AreEqual("some-title", source.Title);
      Assert.IsNull(sourceService.GetSource("invalid"));

      try {
        sourceService.GetSource("throw");
        Assert.Fail();
      }
      catch (SoapException) {
        //fall through...
      }

      try {
        sourceService.GetSource("unknown");
        Assert.Fail();
      }
      catch (SoapException) {
        //fall through...
      }

      Assert.AreEqual("newid", sourceService.AddInfoSet("somesource", new InfoSet()));
      Assert.AreEqual("okay", sourceService.AddInfoSet("othersource", new InfoSet()));
      Assert.AreEqual("resourceId", sourceService.AddInfoSet("resource", new InfoSet()));
      try {
        sourceService.AddInfoSet("unknown", new InfoSet());
        Assert.Fail("Should have thrown the exception.");
      }
      catch (SoapException) {
        //fall through...
      }

      PersonService personService = new PersonService();
      personService.Url = "http://localhost:8080/full/soap-services/PersonServiceService";
      List<string> pids = new List<string>();
      pids.Add("id1");
      pids.Add("id2");
      pids.Add("id3");
      pids.Add("id4");
      Person[] persons = personService.ReadPersons(pids);
      Assert.AreEqual(4, persons.Length);
      foreach (Person pers in persons) {
        Assert.IsTrue(pids.Contains(pers.Id));
        Assert.IsNotNull(pers.Events);
        Assert.IsTrue(pers.Events.Count > 0);
        Assert.IsNotNull(pers.Events[0].Date);
//        Assert.AreEqual(1970, pers.Events[0].Date.Year);
      }

      Person[] empty = personService.ReadPersons(null);
      Assert.IsTrue(empty == null || empty.Length == 0);

      personService.DeletePerson("somebody");
      try {
        personService.DeletePerson(null);
        Assert.Fail("Should have thrown the exception.");
      }
      catch (SoapException e) {
        //fall through...
      }

      Person person = new Person();
      person.Id = "new-person";
      Assert.AreEqual("new-person", personService.StorePerson(person).Id);

      System.Text.UTF8Encoding encoding = new System.Text.UTF8Encoding();
      byte[] pixBytes = encoding.GetBytes("this is a bunch of bytes that I would like to make sure are serialized correctly so that I can prove out that attachments are working properly");
      person.Picture = pixBytes;

      byte[] returnedPix = personService.StorePerson(person).Picture;
      Assert.AreEqual("this is a bunch of bytes that I would like to make sure are serialized correctly so that I can prove out that attachments are working properly", encoding.GetString(returnedPix));

      RelationshipService relationshipService = new RelationshipService();
      relationshipService.Url = "http://localhost:8080/full/soap-services/RelationshipServiceService";
      Relationship[] list = relationshipService.GetRelationships("someid");
      for (int i = 0; i < list.Length; i++) {
        Relationship relationship = list[i];
        Assert.AreEqual(i.ToString(), relationship.Id);
      }

      try {
        relationshipService.GetRelationships("throw");
        Assert.Fail("Should have thrown the relationship service exception, even though it wasn't annotated with @WebFault.");
      }
      catch (SoapException e) {
        //fall through
      }

      relationshipService.Touch();
      AssertionService assertionService = new AssertionService();
      assertionService.Url = "http://localhost:8080/full/soap-services/AssertionServiceService";
      Assertion[] assertions = assertionService.ReadAssertions();
      Assertion gender = assertions[0];
      Assert.AreEqual("gender",gender.Id);
      Assert.IsTrue(gender is Gender);
      Assertion name = assertions[1];
      Assert.AreEqual("name",name.Id);
      Assert.IsTrue(name is Name);      	
    }
  }

  public class Assert {

    public static void IsTrue(bool flag) {
      if (!flag) {
        throw new Exception();
      }
    }

    public static void Fail() {
      throw new Exception();
    }

    public static void Fail(string message) {
      throw new Exception(message);
    }

    public static void IsNotNull(object obj) {
      if (obj == null) {
        throw new Exception();
      }
    }

    public static void IsNull(object obj) {
      if (obj != null) {
        throw new Exception();
      }
    }

    public static void AreEqual(object obj1, object obj2) {
      if (!Object.Equals(obj1, obj2)) {
        throw new Exception("Expected: " + obj1 + ", got " + obj2);
      }
    }

  }
}