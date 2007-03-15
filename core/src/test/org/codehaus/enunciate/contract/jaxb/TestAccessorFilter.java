/*
 * Copyright 2006 Web Cohesion
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

package org.codehaus.enunciate.contract.jaxb;

import org.codehaus.enunciate.InAPTTestCase;
import net.sf.jelly.apt.decorations.declaration.DecoratedTypeDeclaration;
import net.sf.jelly.apt.decorations.declaration.PropertyDeclaration;

import javax.xml.bind.annotation.XmlAccessType;
import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;

import com.sun.mirror.declaration.MemberDeclaration;
import com.sun.mirror.declaration.FieldDeclaration;
import junit.framework.Test;

/**
 * @author Ryan Heaton
 */
public class TestAccessorFilter extends InAPTTestCase {

  /**
   * test the accept method.
   */
  public void testAccept() throws Exception {

    DecoratedTypeDeclaration declaration = new DecoratedTypeDeclaration(getDeclaration("org.codehaus.enunciate.samples.schema.AccessorFilterBean"));
    Collection<FieldDeclaration> fields = declaration.getFields();
    Collection<PropertyDeclaration> properties = declaration.getProperties();
    AccessorFilter filter = new AccessorFilter(XmlAccessType.PUBLIC_MEMBER);

    // From the JAXB 2.0 javadoc for PUBLIC_MEMBER:
    // Every public getter/setter pair and every public field will be automatically bound to
    // XML, unless annotated by XmlTransient. Fields or getter/setter pairs that are private,
    // protected, or defaulted to package-only access are bound to XML only when they are
    // explicitly annotated by the appropriate JAXB annotations.
    HashMap<String, Boolean> acceptedFields = new HashMap<String, Boolean>();
    acceptedFields.put("property1", false); //private
    acceptedFields.put("property2", false); //private
    acceptedFields.put("property3", false); //private
    acceptedFields.put("property4", false); //private
    acceptedFields.put("property5", false); //private
    acceptedFields.put("field1", false); //@XmlTransient
    acceptedFields.put("field2", true);
    acceptedFields.put("field3", false); //protected
    acceptedFields.put("field4", false); //package-private
    acceptedFields.put("field5", false); //static
    acceptedFields.put("field6", false); //transient
    acceptedFields.put("field7", true);
    HashMap<String, Boolean> acceptedProperties = new HashMap<String, Boolean>();
    acceptedProperties.put("property1", false); //@XmlTransient
    acceptedProperties.put("property2", false); //no setter
    acceptedProperties.put("property3", false); //no getter
    acceptedProperties.put("property4", true);
    acceptedProperties.put("property5", true);
    for (MemberDeclaration member : fields) {
      Boolean should = acceptedFields.get(member.getSimpleName());
      if (!should.equals(filter.accept(member))) {
        fail("Field " + member.getSimpleName() + " should" + (should ? "" : "n't") + " have been accepted with accessor type " + filter.getAccessType());
      }
    }
    for (MemberDeclaration member : properties) {
      Boolean should = acceptedProperties.get(member.getSimpleName());
      if (!should.equals(filter.accept(member))) {
        fail("Property " + member.getSimpleName() + " should" + (should ? "" : "n't") + " have been accepted with accessor type " + filter.getAccessType());
      }
    }

    // From the JAXB 2.0 javadoc for NONE:
    // None of the fields or properties is bound to XML unless they are specifically
    // annotated with some of the JAXB annotations.
    filter = new AccessorFilter(XmlAccessType.NONE);
    acceptedFields = new HashMap<String, Boolean>();
    acceptedFields.put("property1", false); //not annotated
    acceptedFields.put("property2", false); //not annotated
    acceptedFields.put("property3", false); //not annotated
    acceptedFields.put("property4", false); //not annotated
    acceptedFields.put("property5", false); //not annotated
    acceptedFields.put("field1", false); //not annotated
    acceptedFields.put("field2", false); //not annotated
    acceptedFields.put("field3", false); //not annotated
    acceptedFields.put("field4", false); //not annotated
    acceptedFields.put("field5", false); //not annotated
    acceptedFields.put("field6", false); //not annotated
    acceptedFields.put("field7", true);
    acceptedProperties = new HashMap<String, Boolean>();
    acceptedProperties.put("property1", false); //not annotated
    acceptedProperties.put("property2", false); //not annotated
    acceptedProperties.put("property3", false); //not annotated
    acceptedProperties.put("property4", false); //not annotated
    acceptedProperties.put("property5", true);
    for (MemberDeclaration member : fields) {
      Boolean should = acceptedFields.get(member.getSimpleName());
      if (!should.equals(filter.accept(member))) {
        fail("Field " + member.getSimpleName() + " should" + (should ? "" : "n't") + " have been accepted with accessor type " + filter.getAccessType());
      }
    }
    for (MemberDeclaration member : properties) {
      Boolean should = acceptedProperties.get(member.getSimpleName());
      if (!should.equals(filter.accept(member))) {
        fail("Property " + member.getSimpleName() + " should" + (should ? "" : "n't") + " have been accepted with accessor type " + filter.getAccessType());
      }
    }

    // From the JAXB 2.0 javadoc for FIELD:
    // None of the fields or properties is bound to XML unless they are specifically
    // annotated with some of the JAXB annotations.
    filter = new AccessorFilter(XmlAccessType.FIELD);
    acceptedFields = new HashMap<String, Boolean>();
    acceptedFields.put("property1", true);
    acceptedFields.put("property2", true);
    acceptedFields.put("property3", true);
    acceptedFields.put("property4", true);
    acceptedFields.put("property5", true);
    acceptedFields.put("field1", false); //@XmlTransient
    acceptedFields.put("field2", true);
    acceptedFields.put("field3", true);
    acceptedFields.put("field4", true);
    acceptedFields.put("field5", false); //static
    acceptedFields.put("field6", false); //transient
    acceptedFields.put("field7", true);
    acceptedProperties = new HashMap<String, Boolean>();
    acceptedProperties.put("property1", false);
    acceptedProperties.put("property2", false);
    acceptedProperties.put("property3", false);
    acceptedProperties.put("property4", false);
    acceptedProperties.put("property5", true); //explicitly annotated.
    for (MemberDeclaration member : fields) {
      Boolean should = acceptedFields.get(member.getSimpleName());
      if (!should.equals(filter.accept(member))) {
        fail("Field " + member.getSimpleName() + " should" + (should ? "" : "n't") + " have been accepted with accessor type " + filter.getAccessType());
      }
    }
    for (MemberDeclaration member : properties) {
      Boolean should = acceptedProperties.get(member.getSimpleName());
      if (!should.equals(filter.accept(member))) {
        fail("Property " + member.getSimpleName() + " should" + (should ? "" : "n't") + " have been accepted with accessor type " + filter.getAccessType());
      }
    }

    // From the JAXB 2.0 javadoc for PROPERTY:
    // None of the fields or properties is bound to XML unless they are specifically
    // annotated with some of the JAXB annotations.
    filter = new AccessorFilter(XmlAccessType.PROPERTY);
    acceptedFields = new HashMap<String, Boolean>();
    acceptedFields.put("property1", false);
    acceptedFields.put("property2", false);
    acceptedFields.put("property3", false);
    acceptedFields.put("property4", false);
    acceptedFields.put("property5", false);
    acceptedFields.put("field1", false);
    acceptedFields.put("field2", false);
    acceptedFields.put("field3", false);
    acceptedFields.put("field4", false);
    acceptedFields.put("field5", false);
    acceptedFields.put("field6", false);
    acceptedFields.put("field7", true); //explicitly annotated
    acceptedProperties = new HashMap<String, Boolean>();
    acceptedProperties.put("property1", false); //@XmlTransient
    acceptedProperties.put("property2", false); //no setter
    acceptedProperties.put("property3", false); //no getter
    acceptedProperties.put("property4", true);
    acceptedProperties.put("property5", true);
    for (MemberDeclaration member : fields) {
      Boolean should = acceptedFields.get(member.getSimpleName());
      if (!should.equals(filter.accept(member))) {
        fail("Field " + member.getSimpleName() + " should" + (should ? "" : "n't") + " have been accepted with accessor type " + filter.getAccessType());
      }
    }
    for (MemberDeclaration member : properties) {
      Boolean should = acceptedProperties.get(member.getSimpleName());
      if (!should.equals(filter.accept(member))) {
        fail("Property " + member.getSimpleName() + " should" + (should ? "" : "n't") + " have been accepted with accessor type " + filter.getAccessType());
      }
    }
  }

  public static Test suite() {
    return createSuite(TestAccessorFilter.class);
  }
}
