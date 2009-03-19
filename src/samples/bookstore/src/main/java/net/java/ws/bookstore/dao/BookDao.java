package net.java.ws.bookstore.dao;

import java.util.List;

import net.java.ws.bookstore.domain.Book;

/**
 * An interface defining operations for working with
 * domain objects.
 * 
 * @author Ross McDonald
 *
 */
public interface BookDao {

  /**
   * Load a book with the
   * given id from the database.
   * 
   * @param id
   * @return book
   */
  Book load(long id);

  /**
   * Find all books in the database.
   * 
   * @return list of books
   */
  List<Book> findAll();

}
