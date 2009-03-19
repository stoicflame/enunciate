package net.java.ws.bookstore;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import net.java.ws.bookstore.service.BookStoreServiceTest;

@RunWith(Suite.class)
@SuiteClasses({
    BookStoreServiceTest.class
})
public class BookStoreSuiteTests {

}
