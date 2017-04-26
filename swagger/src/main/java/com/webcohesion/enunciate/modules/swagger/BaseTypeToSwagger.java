package com.webcohesion.enunciate.modules.swagger;

import java.util.Map;
import java.util.EnumMap;

import com.webcohesion.enunciate.api.datatype.BaseTypeFormat;

public class BaseTypeToSwagger {

  private static final Map<BaseTypeFormat, String> baseformat2swaggerformat = new EnumMap<BaseTypeFormat, String>(BaseTypeFormat.class);
  static {
    baseformat2swaggerformat.put(BaseTypeFormat.INT32, "int32");
    baseformat2swaggerformat.put(BaseTypeFormat.INT64, "int64");
    baseformat2swaggerformat.put(BaseTypeFormat.FLOAT, "float");
    baseformat2swaggerformat.put(BaseTypeFormat.DOUBLE, "double");
  }

  public static String toSwaggerFormat(BaseTypeFormat format) {
    return format == null ? null : baseformat2swaggerformat.get(format);
  }
}
