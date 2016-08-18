/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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