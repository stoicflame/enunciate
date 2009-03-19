package net.java.ws.bookstore.service;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import net.java.ws.bookstore.domain.Book;

/**
 * An interface defining a bookstore service for
 * working with books.
 * 
 * @author Ross McDonald
 *
 */
public interface BookStoreService {

 /**
  * Load a book with the specified id.
  * 
  * @param id the book id
  * @return domain book
  */
  @GET
  @Path("/book/{id}")
  Book getBook(@PathParam( "id" ) long id);

  /**
   * Get a list of all books.
   * 
   * @return books list
   */
  List<Book> getBooks();

}
