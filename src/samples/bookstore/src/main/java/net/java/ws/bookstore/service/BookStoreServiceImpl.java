package net.java.ws.bookstore.service;

import java.util.List;

import javax.ws.rs.Path;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import net.java.ws.bookstore.domain.Book;
import net.java.ws.bookstore.dao.BookDao;

/**
 * An implementation of our bookstore service for working with
 * book objects.
 * 
 * @author Ross McDonald
 *
 */
@Path("/books")
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class BookStoreServiceImpl implements BookStoreService {

  private BookDao bookDao;

  public BookStoreServiceImpl() {
    
  }

  /**
   * Load a book with the specified id.
   * 
   * @param id the book id
   * @return book
   */
  public Book getBook(long id) {
    return bookDao.load(id);
  }

  /**
   * Get a list of all books.
   *
   * @return books list
   */
  public List<Book> getBooks() {
    return bookDao.findAll();
  }

  public BookDao getBookDao() {
    return bookDao;
  }

  public void setBookDao(BookDao bookDao) {
    this.bookDao = bookDao;
  }
}
