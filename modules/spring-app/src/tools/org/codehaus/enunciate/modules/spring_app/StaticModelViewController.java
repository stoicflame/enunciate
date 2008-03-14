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
