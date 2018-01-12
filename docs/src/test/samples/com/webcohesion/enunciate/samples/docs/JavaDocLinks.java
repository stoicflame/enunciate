package com.webcohesion.enunciate.samples.docs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.webcohesion.enunciate.metadata.json.JsonSeeAlso;

@JsonInclude
@JsonSeeAlso({JavaDocLinkClass.class, JavaDocLinkEnum.class})
public class JavaDocLinks {
  @JsonProperty("a_b_c_d_e")
  public String abcde;

  /**
   * Status of {@link #abcde}
   */
  public String status;

  /**
   * {@link  #status  foo bar}
   */
  public String description;
}
