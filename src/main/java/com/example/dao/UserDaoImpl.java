package com.example.dao;

import com.example.model.User;
import com.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserDaoImpl implements UserDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDaoImpl.class);

    @Override
    public User create(User user) throws Exception {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(user);
            tx.commit();
            return user;
        } catch (ConstraintViolationException cve) {
            if (tx != null) tx.rollback();
            LOGGER.error("Constraint violation on create", cve);
            throw new Exception("Constraint violation: " + cve.getMessage(), cve);
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            LOGGER.error("Error creating user", e);
            throw e;
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.find(User.class, id);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            LOGGER.error("Error finding user by id {}", id, e);
            return Optional.empty();
        }
    }


    @Override
    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from User", User.class).list();
        }
    }

    @Override
    public User update(User user) throws Exception {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(user);
            tx.commit();
            return user;
        } catch (ConstraintViolationException cve) {
            if (tx != null) tx.rollback();
            LOGGER.error("Constraint violation on update", cve);
            throw new Exception("Constraint violation: " + cve.getMessage(), cve);
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            LOGGER.error("Error updating user", e);
            throw e;
        }
    }

    @Override
    public boolean delete(Long id) throws Exception {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            User user = session.find(User.class, id);
            if (user == null) {
                return false;
            }
            session.remove(user);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            LOGGER.error("Error deleting user", e);
            throw e;
        }
    }
}