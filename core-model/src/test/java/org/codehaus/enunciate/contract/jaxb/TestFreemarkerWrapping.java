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

package org.codehaus.enunciate.contract.jaxb;

import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import junit.framework.TestCase;
import net.sf.jelly.apt.freemarker.APTJellyObjectWrapper;
import org.codehaus.enunciate.contract.jaxb.types.KnownXmlType;

/**
 * @author Ryan Heaton
 */
public class TestFreemarkerWrapping extends TestCase {

  public void testWrapEnums() throws Exception {
    APTJellyObjectWrapper wrapper = new APTJellyObjectWrapper();
    TemplateModel wrapped = wrapper.wrap(KnownXmlType.STRING);
    assertTrue(wrapped instanceof TemplateHashModel);
    TemplateHashModel hash = ((TemplateHashModel) wrapped);
    assertNotNull(hash.get("anonymous"));

    wrapped = wrapper.wrap(ContentType.COMPLEX);
    assertTrue(wrapped instanceof TemplateHashModel);
    hash = ((TemplateHashModel) wrapped);
    TemplateModel complex = hash.get("complex");
    assertNotNull(complex);
    assertTrue(complex instanceof TemplateBooleanModel);
    assertTrue(((TemplateBooleanModel) complex).getAsBoolean());
  }
}
