/**
 * The original code for this sample model was taken from the samples
 * for spring framework.  See http://www.springframework.org.
 */
package net.sf.enunciate.samples.petclinic;

/**
 * An entity with an associated name.
 *
 * @author Ryan Heaton
 */
public class NamedEntity extends Entity {

	private String name;

  /**
   * The name of the entity.
   *
   * @param name The name of the entity.
   */
  public void setName(String name) {
		this.name = name;
	}

  /**
   * The name of the entity.
   *
   * @return The name of the entity.
   */
  public String getName() {
		return this.name;
	}

}
