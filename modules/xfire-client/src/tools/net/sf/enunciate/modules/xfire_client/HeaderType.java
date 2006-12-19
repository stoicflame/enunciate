package net.sf.enunciate.modules.xfire_client;

import org.codehaus.xfire.aegis.type.Type;
import org.codehaus.xfire.aegis.type.TypeMapping;
import org.codehaus.xfire.aegis.MessageReader;
import org.codehaus.xfire.aegis.MessageWriter;
import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.fault.XFireFault;
import org.jdom.Element;

import javax.xml.namespace.QName;
import java.util.Set;

/**
 * Just a delegating type that makes sure its "outer" is written...
 *
 * @author Ryan Heaton
 */
public class HeaderType extends Type {

  private final Type delegate;

  public HeaderType(Type delegate) {
    this.delegate = delegate;
    setWriteOuter(true);
  }

  public Object readObject(MessageReader reader, MessageContext context) throws XFireFault {
    return delegate.readObject(reader, context);
  }

  public void writeObject(Object object, MessageWriter writer, MessageContext context) throws XFireFault {
    delegate.writeObject(object, writer, context);
  }

  public void writeSchema(Element root) {
    delegate.writeSchema(root);
  }

  public TypeMapping getTypeMapping() {
    return delegate.getTypeMapping();
  }

  public void setTypeMapping(TypeMapping typeMapping) {
    delegate.setTypeMapping(typeMapping);
  }

  public Class getTypeClass() {
    return delegate.getTypeClass();
  }

  public void setTypeClass(Class typeClass) {
    delegate.setTypeClass(typeClass);
  }

  public boolean isComplex() {
    return delegate.isComplex();
  }

  public boolean isAbstract() {
    return delegate.isAbstract();
  }

  public void setAbstract(boolean abstrct) {
    delegate.setAbstract(abstrct);
  }

  public boolean isNillable() {
    return delegate.isNillable();
  }

  public void setNillable(boolean nillable) {
    delegate.setNillable(nillable);
  }

  public Set getDependencies() {
    return delegate.getDependencies();
  }

  public QName getSchemaType() {
    return delegate.getSchemaType();
  }

  public void setSchemaType(QName name) {
    delegate.setSchemaType(name);
  }
}
