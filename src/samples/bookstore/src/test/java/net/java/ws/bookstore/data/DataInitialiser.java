package net.java.ws.bookstore.data;

import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

import net.java.ws.bookstore.domain.Book;

public final class DataInitialiser implements InitializingBean,
    ApplicationContextAware {
  
  private static final Logger logger = LoggerFactory.getLogger(DataInitialiser.class);

  private SessionFactory sessionFactory;

  private ApplicationContext context;

  public DataInitialiser() {
    
  }

  public void afterPropertiesSet() throws Exception {

    // setup database
    LocalSessionFactoryBean sessionFactoryBean = findSessionFactoryBean(context);
    sessionFactoryBean.createDatabaseSchema();

    Session session = null;
    Transaction tx = null;
    
    try {

      session = sessionFactory.openSession();
      tx = session.beginTransaction();

      // fill with test data
      Book book = new Book();
      book.setName("The Lord of the Rings");
      session.save(book);
      
      tx.commit();
      
      logger.info("Data has been initialised successfully");
    } catch (Exception e) {
      logger.error("An exception has occurred : " + e.getMessage());
      e.printStackTrace();
      tx.rollback();
    } finally {
      session.close();
    }
  }

  public SessionFactory getSessionFactory() {
    return sessionFactory;
  }

  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  public void setApplicationContext(
      ApplicationContext applicationContext) throws BeansException {
    this.context = applicationContext;
  }

  @SuppressWarnings("unchecked")
  private LocalSessionFactoryBean findSessionFactoryBean(
      ApplicationContext context) {
    Map beans = context.getBeansOfType(LocalSessionFactoryBean.class);
    if (beans.size() > 1) {
      throw new IllegalStateException(
          "more than one local session factory bean found");
    } else if (beans.size() == 0) {
      throw new IllegalStateException(
          "session factory bean not found");
    }
    return (LocalSessionFactoryBean) beans.values().iterator().next();
  }
}
