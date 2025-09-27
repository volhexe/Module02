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

    private UserDaoImpl userDao;
    private UserServiceImpl userService;

    @BeforeAll
    static void beforeAll() {
        System.setProperty("hibernate.connection.url", POSTGRES_CONTAINER.getJdbcUrl());
        System.setProperty("hibernate.connection.username", POSTGRES_CONTAINER.getUsername());
        System.setProperty("hibernate.connection.password", POSTGRES_CONTAINER.getPassword());
        HibernateUtil.buildSessionFactory();
    }

    @BeforeEach
    void beforeEach() {
        userDao = new UserDaoImpl();
        userService = new UserServiceImpl(userDao);
        cleanDatabase();
    }

    @AfterAll
    static void afterAll() {
        HibernateUtil.shutdown();
    }

    private void cleanDatabase() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.createNativeQuery("TRUNCATE TABLE users RESTART IDENTITY CASCADE").executeUpdate();
            tx.commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to clean database before test", e);
        }
    }

    //idk...
    private void insertUser(String name, String email, Integer age) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            User user = new User(name, email, age);
            session.persist(user);
            tx.commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert user", e);
        }
    }

    @Test
    void testCreateAndFind() {
        assertDoesNotThrow(() -> {
            User user = userService.createUser("Granny", "Granny@example.com", 69);  // age как Integer
            assertNotNull(user.getId());
            assertNotNull(user.getCreatedAt());
            Optional<User> found = userService.getUserById(user.getId());
            assertTrue(found.isPresent());
            assertEquals("Granny", found.get().getName());
            assertEquals("Granny@example.com", found.get().getEmail());
            assertEquals(69, found.get().getAge());
        });
    }

    @Test
    void testFindAll() {
        insertUser("Kostya", "Kostya@example.com", 25);
        insertUser("Ira", "Ira@example.com", 28);
        List<User> users = userService.getAllUsers();
        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getName().equals("Kostya")));
        assertTrue(users.stream().anyMatch(u -> u.getName().equals("Ira")));
    }

    @Test
    void testUpdate() {
        insertUser("Tom", "Tom@example.com", 22);
        List<User> users = userService.getAllUsers();
        User existingUser = users.stream()
                .filter(u -> u.getEmail().equals("Tom@example.com"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found after insert"));
        Long userId = existingUser.getId();

        assertDoesNotThrow(() -> {
            User updated = userService.updateUser(userId, "Tommy", null, null);
            assertEquals("Tommy", updated.getName());
            assertEquals("Tom@example.com", updated.getEmail());
            assertEquals(22, updated.getAge());
        });
    }

    @Test
    void testDelete() {

        insertUser("Eva", "Eva@example.com", 26);
        List<User> users = userService.getAllUsers();
        User existingUser = users.stream()
                .filter(u -> u.getEmail().equals("Eva@example.com"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found after insert"));
        Long userId = existingUser.getId();

        assertDoesNotThrow(() -> {
            boolean deleted = userService.deleteUser(userId);
            assertTrue(deleted);

            Optional<User> found = userService.getUserById(userId);
            assertTrue(found.isEmpty());
        });
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
    void testUpdateNonExistentUser() {
        assertThrows(Exception.class, () -> {
            userService.updateUser(999L, "NewName", "new@email.com", 69);
        });
    }

    @Test
    void testDeleteNonExistentUser() throws Exception {
        boolean deleted = userService.deleteUser(999L);
        assertFalse(deleted);
    }
}