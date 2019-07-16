/*
 * © 2019 by Intellectual Reserve, Inc. All rights reserved.
 */
package org.springframework.samples.petclinic.model;

import java.util.List;

public class ResponseData {

  private List<?> rows;

  public List<?> getRows() {
    return rows;
  }

  public void setRows(List<?> rows) {
    this.rows = rows;
  }
}
