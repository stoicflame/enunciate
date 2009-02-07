namespace Jaxws.Ri.Rest {

  using NUnit.Framework;
  using System;
  using Org.Codehaus.Enunciate.Samples.Genealogy.Services;
  using Org.Codehaus.Enunciate.Samples.Genealogy.Cite;

  [TestFixture]
  public class FullAPITest {

    [Test]
    public void TestFullAPI() {
      SourceService sourceService = new SourceService();
      sourceService.Url = "http://localhost:8080/full/soap-services/sources/source";
      Source source = sourceService.GetSource("valid");
      Assert.IsNotNull(source);
      Assert.AreEqual("valid", source.Id);
    }
  }
}