

import com.example.dao.UserDaoImpl;
import com.example.model.User;
import com.example.util.HibernateUtil;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserDaoImplIT {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("testdb")
                    .withUsername("postgres")
                    .withPassword("root");

    private UserDaoImpl userDao;

    @BeforeAll
    void setup() {
        System.setProperty("hibernate.connection.url", POSTGRES_CONTAINER.getJdbcUrl());
        System.setProperty("hibernate.connection.username", POSTGRES_CONTAINER.getUsername());
        System.setProperty("hibernate.connection.password", POSTGRES_CONTAINER.getPassword());

        HibernateUtil.getSessionFactory();
        userDao = new UserDaoImpl();
    }

    @AfterAll
    void teardown() {
        HibernateUtil.shutdown();
    }

    @Test
    void testCreateAndFind() throws Exception {
        User user = new User("Granny", "Granny@example.com", 69);
        User created = userDao.create(user);
        assertNotNull(created.getId());

        Optional<User> found = userDao.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals("Granny", found.get().getName());
        System.out.println("1");
    }

    @Test
    void testFindAll() throws Exception {
        userDao.create(new User("Kostya", "Kostya@example.com", 25));
        userDao.create(new User("Ira", "Ira@example.com", 28));

        List<User> users = userDao.findAll();
        assertTrue(users.size() >= 2);
        System.out.println("2");
    }

    @Test
    void testUpdate() throws Exception {
        User user = userDao.create(new User("Tom", "Tom@example.com", 22));
        user.setName("Tommy");
        User updated = userDao.update(user);
        assertEquals("Tommy", updated.getName());
        System.out.println("3");
    }

    @Test
    void testDelete() throws Exception {
        User user = userDao.create(new User("Eva", "Eva@example.com", 26));
        boolean deleted = userDao.delete(user.getId());
        assertTrue(deleted);

        Optional<User> found = userDao.findById(user.getId());
        assertTrue(found.isEmpty());
        System.out.println("4");
    }
}
