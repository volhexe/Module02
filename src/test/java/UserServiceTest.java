
import com.example.dao.UserDao;
import com.example.model.User;
import com.example.service.UserService;
import com.example.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserDao userDao;
    private UserService userService;

    @BeforeEach
    void setup() {
        userDao = mock(UserDao.class);
        userService = new UserServiceImpl(userDao);
    }

    @Test
    void testCreateUser() throws Exception {
        User user = new User("Anna", "Anna@example.com", 29);
        when(userDao.create(any(User.class))).thenReturn(user);

        User created = userService.createUser("Anna", "Anna@example.com", 29);
        assertEquals("Anna", created.getName());
        verify(userDao).create(any(User.class));

    }

    @Test
    void testGetUserById() {
        User user = new User("Mike", "Mike@example.com", 35);
        when(userDao.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> found = userService.getUserById(1L);
        assertTrue(found.isPresent());
        assertEquals("Mike", found.get().getName());
    }

    @Test
    void testGetAllUsers() {
        List<User> users = List.of(
                new User("Alina", "Alina@example.com", 20),
                new User("Arina", "Arina@example.com", 25)
        );
        when(userDao.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();
        assertEquals(2, result.size());
    }

    @Test
    void testUpdateUser() throws Exception {
        User user = new User("Old", "old@example.com", 40);
        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        when(userDao.update(any(User.class))).thenReturn(user);

        User updated = userService.updateUser(1L, "New", "new@example.com", 45);
        assertEquals("New", updated.getName());
        assertEquals("new@example.com", updated.getEmail());
        assertEquals(45, updated.getAge());
    }

    @Test
    void testDeleteUser() throws Exception {
        when(userDao.delete(1L)).thenReturn(true);
        boolean deleted = userService.deleteUser(1L);
        assertTrue(deleted);
    }


    @Test
    void testCreateUserThrowsException() throws Exception {
        when(userDao.create(any(User.class))).thenThrow(new RuntimeException("Database error"));
        assertThrows(RuntimeException.class, () -> {
            userService.createUser("Anna", "Anna@example.com", 29);
        });
    }


    @Test
    void testGetUserByIdNotFound() {
        when(userDao.findById(1L)).thenReturn(Optional.empty());
        Optional<User> found = userService.getUserById(1L);
        assertFalse(found.isPresent());
    }


    @Test
    void testUpdateUserNotFound() {
        when(userDao.findById(1L)).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> {
            userService.updateUser(1L, "New", "new@example.com", 45);
        });
    }

    @Test
    void testDeleteUserThrowsException() throws Exception {
        when(userDao.delete(1L)).thenThrow(new RuntimeException("Delete failed"));
        assertThrows(RuntimeException.class, () -> {
            userService.deleteUser(1L);
        });
    }

}
