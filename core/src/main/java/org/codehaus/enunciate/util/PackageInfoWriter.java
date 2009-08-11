package org.codehaus.enunciate.util;

import org.codehaus.enunciate.asm.Type;
import org.codehaus.enunciate.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

/**
 * Writes a package-info.java file for some bytecode.
 *
 * @author Ryan Heaton
 */
public class PackageInfoWriter {

  private final Writer writer;

  public PackageInfoWriter(Writer writer) {
    this.writer = writer;
  }

  public void write(InputStream bytecode) throws IOException {
    org.codehaus.enunciate.asm.ClassReader cr = new org.codehaus.enunciate.asm.ClassReader(bytecode);
    ClassNode cn = new org.codehaus.enunciate.asm.tree.ClassNode();
    cr.accept(cn, 0);
    if (cn.visibleAnnotations != null) {
      for (Object visibleAnnotation : cn.visibleAnnotations) {
        org.codehaus.enunciate.asm.tree.AnnotationNode annotation = (org.codehaus.enunciate.asm.tree.AnnotationNode) visibleAnnotation;
        writeAnnotationNode(annotation);
        writer.write('\n');
      }
    }
    writer.write("package ");
    String classname = org.codehaus.enunciate.asm.Type.getObjectType(cn.name).getClassName();
    writer.write(classname.substring(0, classname.length() - ".package-info".length()));
    writer.write(';');
  }

  /**
   * Write an annotation node.
   *
   * @param annotation The annotation.
   */
  protected void writeAnnotationNode(org.codehaus.enunciate.asm.tree.AnnotationNode annotation) throws IOException {
    writer.write('@');
    writer.write(Type.getType(annotation.desc).getClassName());
    writer.write('(');
    if (annotation.values != null) {
      boolean first = true;
      Iterator valuesIt = annotation.values.iterator();
      while (valuesIt.hasNext()) {
        if (!first) {
          writer.write(',');
        }

        writer.write((String) valuesIt.next());
        Object value = valuesIt.next();
        writer.write('=');
        writeAnnotationValue(value);
        first = false;
      }
    }
    writer.write(')');
  }

  /**
   * Write an annotation value.
   *
   * @param value The annotation value.
   */
  protected void writeAnnotationValue(Object value) throws IOException {
    if (value instanceof Character) {
      writer.write('\'');
      writer.write(value.toString());
      writer.write('\'');
    }
    else if (value instanceof String) {
      writer.write('\"');
      writer.write(value.toString());
      writer.write('\"');
    }
    else if (value instanceof org.codehaus.enunciate.asm.Type) {
      writer.write(((Type)value).getClassName());
      writer.write(".class");
    }
    else if (value instanceof String[]) {
      //enum case
      String[] enumValue = (String[]) value;
      writer.write(org.codehaus.enunciate.asm.Type.getType(enumValue[0]).getClassName());
      writer.write('.');
      writer.write(enumValue[1]);
    }
    else if (value instanceof org.codehaus.enunciate.asm.tree.AnnotationNode) {
      writeAnnotationNode((org.codehaus.enunciate.asm.tree.AnnotationNode) value);
    }
    else if (value instanceof List) {
      List valueList = (List) value;
      writer.write('{');
      boolean first = true;
      for (Object valueItem : valueList) {
        if (!first) {
          writer.write(',');
        }
        writeAnnotationValue(valueItem);
        first = false;
      }

      writer.write('}');
    }
    else {
      writer.write(value.toString());
    }
  }

  public void close() throws IOException {
    writer.flush();
    writer.close();
  }
}
