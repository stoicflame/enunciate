/*
 * © 2020 by Intellectual Reserve, Inc. All rights reserved.
 */
package com.webcohesion.enunciate.util.freemarker;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.Version;

public class FreemarkerUtil {

  public static final Version VERSION = Configuration.VERSION_2_3_30;
  private static final BeansWrapper WRAPPER = new BeansWrapperBuilder(VERSION).build();

  public static Object unwrap(TemplateModel from) throws TemplateModelException {
    return WRAPPER.unwrap(from);
  }

}
