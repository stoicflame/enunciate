/**
 * The original code for this sample model was taken from the samples
 * for spring framework.  See http://www.springframework.org.
 */
package net.sf.enunciate.samples.petclinic;

/**
 * An object identifiable by id.
 *
 * @author Ryan Heaton
 */
public class Entity {

	private Integer id;

  /**
   * The id of the entity.
   *
   * @param id The id of the entity.
   */
  public void setId(Integer id) {
		this.id = id;
	}

  /**
   * The id of the entity.
   *
   * @return The id of the entity.
   */
  public Integer getId() {
		return id;
	}

  /**
   * Whether this entity is new.
   *
   * @return Whether this entity is new.
   */
  public boolean isNew() {
		return (this.id == null);
	}

}
