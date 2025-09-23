import com.example.dao.UserDaoImpl;
import com.example.model.User;
import com.example.service.UserServiceImpl;
import com.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
class UserDaoImplIT {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("testdb")
                    .withUsername("postgres")
                    .withPassword("root");

    private  UserDaoImpl userDao;
    private  UserServiceImpl userService;

    @BeforeEach
    void beforeEach() {
        userDao = new UserDaoImpl();
        userService = new UserServiceImpl(userDao);
        System.setProperty("hibernate.connection.url", POSTGRES_CONTAINER.getJdbcUrl());
        System.setProperty("hibernate.connection.username", POSTGRES_CONTAINER.getUsername());
        System.setProperty("hibernate.connection.password", POSTGRES_CONTAINER.getPassword());
        System.setProperty("hibernate.hbm2ddl.auto", "update");
        //Or?
        //System.setProperty("hibernate.hbm2ddl.auto", "create-drop");?
        HibernateUtil.buildSessionFactory();
        //Tnen no need cleanDatabase()
        cleanDatabase();
    }

    private void cleanDatabase() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.createNativeQuery("TRUNCATE TABLE users CASCADE").executeUpdate();
            tx.commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to clean database before test", e);
        }
    }


    @AfterEach
     void afterEach() {
        HibernateUtil.shutdown();
    }

    @Test
    void testCreateAndFind() {
        assertDoesNotThrow(() -> {
            User user = userService.createUser("Granny", "Granny@example.com", 69);
            assertNotNull(user.getId());

            Optional<User> found = userService.getUserById(user.getId());
            assertEquals("Granny", found.get().getName());
        });

    }

    @Test
    void testFindAll() throws Exception {

        userService.createUser("Kostya", "Kostya@example.com", 25);
        userService.createUser("Ira", "Ira@example.com", 28);

        List<User> users = userService.getAllUsers();
        assertEquals(2, users.size());

    }

    @Test
    void testUpdate() throws Exception {
        User user = userService.createUser("Tom", "Tom@example.com", 22);
        User updated = userService.updateUser(user.getId(), "Tommy", null, null);
        assertEquals("Tommy", updated.getName());

    }

    @Test
    void testDelete() throws Exception {
        User user = userService.createUser("Eva", "Eva@example.com", 26);
        boolean deleted = userService.deleteUser(user.getId());
        assertTrue(deleted);

        Optional<User> found = userService.getUserById(user.getId());
        assertTrue(found.isEmpty());

    }


    @Test
    void testCreateWithDuplicateEmail() {
        assertDoesNotThrow(() -> {
            userService.createUser("UniqueUser", "unique@example.com", 40);
        });
        assertThrows(Exception.class, () -> {
            userService.createUser("AnotherUser", "unique@example.com", 40);
        });

    }

    @Test
    void testGetUserByIdNotFound() {
        Optional<User> found = userService.getUserById(999L);
        assertTrue(found.isEmpty());

    }

    @Test
    void testUpdateNonExistentUser()  {
        assertThrows(Exception.class,() -> {
            userService.updateUser(999L, "NewName", "new@email.com", 69);
        });

    }

    @Test
    void testDeleteNonExistentUser() throws Exception {
        boolean deleted = userService.deleteUser(999L);
        assertFalse(deleted);

    }
}
