package net.java.ws.bookstore.dao.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import net.java.ws.bookstore.domain.Book;
import net.java.ws.bookstore.dao.BookDao;

/**
 * Implements BookDao, providing methods for
 * working with Book domain objects.
 * 
 * @author Ross McDonald
 *
 */
public class BookDaoImpl implements BookDao {

  private SessionFactory sessionFactory;

  public BookDaoImpl() {
  }

  public SessionFactory getSessionFactory() {
    return sessionFactory;
  }

  public void setSessionFactory(SessionFactory sf) {
    this.sessionFactory = sf;
  }

  /**
   * Load book with the given id.
   */
  @SuppressWarnings("unchecked")
  public Book load(long id) {
    return (Book) getCurrentSession().get(Book.class, id);
  }

  /**
   * Find all books.
   */
  @SuppressWarnings("unchecked")
  public List<Book> findAll() {
    Criteria criteria = getCurrentSession().createCriteria(Book.class);
    return (List<Book>) criteria.list();
  }

  public Session getCurrentSession() {
    // presumes a current session, which we have through the
    // OpenSessionInViewFilter; doesn't work without that
    return sessionFactory.getCurrentSession();
  }
}
