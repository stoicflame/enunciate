namespace Com.Webcohesion.Enunciate.CSharp.Test {

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
      if (o is Com.Webcohesion.Enunciate.Samples.Csharp_client.Schema.Structures.House) {
        ((Com.Webcohesion.Enunciate.Samples.Csharp_client.Schema.Structures.House)o).KnownType = ((Com.Webcohesion.Enunciate.Samples.Csharp_client.Schema.Structures.House)o).KnownType;
        ((Com.Webcohesion.Enunciate.Samples.Csharp_client.Schema.Structures.House)o).KnownStyle = ((Com.Webcohesion.Enunciate.Samples.Csharp_client.Schema.Structures.House)o).KnownStyle;
      }
      tr.Close();

      TextWriter tw = new StreamWriter(args[2]);
      serializer.Serialize(tw, o);
      tw.Close();
    }
  }
}