namespace Org.Codehaus.Enunciate.CSharp.Test {

  using System;
  using System.IO;
  using System.Xml.Serialization;

  public class ProcessXml {

    public static void Main(string[] args) {
      if (args.Length < 3) {
        Console.WriteLine("Usage: ProcessXml [classname-to-serialize] [infile] [outfile]");
        Environment.Exit(1);
      }

      Type type = Type.GetType(args[0], true);
      XmlSerializer serializer = new XmlSerializer(type);
      TextReader tr = new StreamReader(args[1]);
      object o = serializer.Deserialize(tr);
      if (o is Org.Codehaus.Enunciate.Examples.Csharp.Schema.Structures.House) {
        ((Org.Codehaus.Enunciate.Examples.Csharp.Schema.Structures.House)o).KnownType = ((Org.Codehaus.Enunciate.Examples.Csharp.Schema.Structures.House)o).KnownType;
        ((Org.Codehaus.Enunciate.Examples.Csharp.Schema.Structures.House)o).KnownStyle = ((Org.Codehaus.Enunciate.Examples.Csharp.Schema.Structures.House)o).KnownStyle;
      }
      tr.Close();

      TextWriter tw = new StreamWriter(args[2]);
      serializer.Serialize(tw, o);
      tw.Close();
    }
  }
}