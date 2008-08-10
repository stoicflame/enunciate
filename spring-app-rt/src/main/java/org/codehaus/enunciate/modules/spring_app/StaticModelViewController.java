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

package org.codehaus.enunciate.modules.spring_app;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Controller for a single view-with-model.
 *
 * @author Ryan Heaton
 */
public class StaticModelViewController extends AbstractController {

  private Map model;
  private View view;

  protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
    return new ModelAndView(getView(), getModel());
  }

  /**
   * The view.
   *
   * @return The view.
   */
  public View getView() {
    return view;
  }

  /**
   * The view.
   *
   * @param view The view.
   */
  public void setView(View view) {
    this.view = view;
  }

  /**
   * The model for the view.
   *
   * @return The model for the view.
   */
  public Map getModel() {
    return model;
  }

  /**
   * The model for the view.
   *
   * @param model The model for the view.
   */
  public void setModel(Map model) {
    this.model = model;
  }
}
