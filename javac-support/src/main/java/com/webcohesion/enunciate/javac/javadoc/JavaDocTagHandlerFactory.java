package com.webcohesion.enunciate.javac.javadoc;

/**
 * Handler to be used to define logic to perform for tags in JavaDoc comments.
 *
 * @author Ryan Heaton
 */
public class JavaDocTagHandlerFactory {

  private static JavaDocTagHandler INSTANCE;
  private static boolean CHECKED;

  /**
   * The tag handler instance.
   *
   * @return The tag handler instance.
   */
  public static synchronized JavaDocTagHandler getTagHandler() {
    if (!CHECKED) {
      String handlerClassname = System.getProperty(JavaDocTagHandler.class.getName());
      if (handlerClassname != null) {
        try {
          INSTANCE = (JavaDocTagHandler) Class.forName(handlerClassname).newInstance();
          CHECKED = true;
        }
        catch (Exception e) {
          System.getProperties().remove(JavaDocTagHandler.class.getName());
          e.printStackTrace(System.err);
        }
      }
      else {
        INSTANCE = new DefaultJavaDocTagHandler();
        CHECKED = true;
      }
    }

    return INSTANCE;
  }

  /**
   * The tag handler instance.
   *
   * @param tagHandler The tag handler instance.
   */
  public static synchronized void setTagHandler(JavaDocTagHandler tagHandler) {
    INSTANCE = tagHandler;
    CHECKED = true;
  }

}