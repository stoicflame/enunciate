/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.modules.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;

/**
 * The XML pretty-printer.  Some important assumptions are made in this handler:
 *
 * <ol>
 *   <li>The SAX Parser is NOT namespace-aware.</li>
 *   <li>The XML contains no comments that are important</li>
 *   <li>The XML contains no processing instructions that are important</li>
 * </ol>
 *
 * @author Ryan Heaton
 */
public class PrettyPrinter extends DefaultHandler {

  private final StringBuilder indentation = new StringBuilder();
  private final LinkedList<Boolean> bodyStack = new LinkedList<Boolean>();
  private PrintWriter writer;
  private final File output;

  public PrettyPrinter(File output) {
    this.output = output;
  }

  @Override
  public void startDocument() throws SAXException {
    try {
      writer = new PrintWriter(new FileWriter(this.output));
    }
    catch (IOException e) {
      throw new SAXException(e);
    }

    bodyStack.add(true);
  }

  @Override
  public void endDocument() throws SAXException {
    writer.flush();
    writer.close();
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    if (!bodyStack.getFirst()) {
      writer.print('>');
      writer.println();
      bodyStack.removeFirst();
      bodyStack.addFirst(true);
    }

    writer.print(indentation.toString());
    writer.print('<');
    writer.print(qName);
    int i = 0;
    while (i < attributes.getLength()) {
      writer.print(' ');
      writer.print(attributes.getQName(i));
      writer.print('=');
      writer.print('\"');
      writer.print(attributes.getValue(i));
      writer.print('\"');
      i++;
    }

    bodyStack.addFirst(false);
    indentation.append("  ");
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    indentation.delete(0, 2);
    if (!bodyStack.removeFirst()) {
      writer.print('/');
      writer.print('>');
    }
    else {
      writer.print(indentation.toString());
      writer.print('<');
      writer.print('/');
      writer.print(qName);
      writer.print('>');
    }

    writer.println();
  }

  @Override
  public void characters(char ch[], int start, int length) throws SAXException {
    String chars = new String(ch, start, length).trim();
    if (chars.length() > 0) {
      if (!bodyStack.getFirst()) {
        writer.print('>');
        writer.println();
        writer.print(indentation.toString());
        bodyStack.removeFirst();
        bodyStack.addFirst(true);
      }

      writer.print("<![CDATA[");
      writer.print(chars);
      writer.print("]]>");
      writer.println();
    }
  }

}
