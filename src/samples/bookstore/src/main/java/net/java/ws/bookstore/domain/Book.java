package net.java.ws.bookstore.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * A book in the book store.
 *
 * @author Ross McDonald
 */
@Entity
@Table(name = "BOOK")
@XmlRootElement
public class Book implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "BOOK_ID")
  private Long id;

  @Column(name = "BOOK_NAME")
  private String name;

  public Book() {
	
  }

  /**
   * The id of the book.
   *
   * @return The id of the book.
   */
  public Long getId() {
    return id;
  }

  /**
   * The id of the book.
   *
   * @param id The id of the book.
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * The name of the book.
   *
   * @return The name of the book.
   */
  public String getName() {
    return name;
  }

  /**
   * The name of the book.
   *
   * @param name The name of the book.
   */
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public int hashCode() {
    if (id == null) {
      return super.hashCode();
    }
    return ObjectIdentifier.hashCode(id);
  }

  @Override
  public boolean equals(Object obj) {
    if (id == null) {
      return super.equals(obj);
    }
    if (obj instanceof Book) {
      return ObjectIdentifier.equal(id, ((Book) obj).id);
    }
    return false;
  }

  @Override
  public String toString() {
    return "Book[" + id + ", name " + name + "]";
  }
}
