package net.java.ws.bookstore.service;

import java.util.List;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import net.java.ws.bookstore.domain.Book;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
  "classpath:applicationContext-test.xml"})
@Transactional
public class BookStoreServiceTest {

  @Autowired
  BookStoreService bookStoreService = null;

  public BookStoreServiceTest() {

  }

  @Test
  public void getBooks() throws Exception {
    List<Book> books = bookStoreService.getBooks();
	assertEquals(1, books.size());	  
  }

  @Test
  public void getBook() throws Exception {
    Book book = bookStoreService.getBook(1);
    assertEquals("The Lord of the Rings", book.getName());
  }
}
