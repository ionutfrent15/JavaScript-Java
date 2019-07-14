package persistence;

import model.Imagine;
import model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class ImagineRepo {
    private SessionFactory sessionFactory;

    public ImagineRepo(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public List<Imagine> findAll() {
        try(Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                List<Imagine> u = session.createQuery("from Imagine ", Imagine.class)
                        .list();
                return u;
            } catch (RuntimeException ex) {
                if (tx != null) {
                    tx.rollback();
                }
            }
        }
        return new ArrayList<>();
    }

    public void save(Imagine imagine) {
        try(Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                session.save(imagine);
                tx.commit();
            } catch (RuntimeException ex) {
                if (tx != null) {
                    tx.rollback();
                }
            }
        }
    }

    public Imagine findOne(String nume) {
        try(Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                Imagine u = session.createQuery("from Imagine where nume = :nume", Imagine.class)
                        .setString("nume", nume)
                        .getSingleResult();
                return u;
            } catch (RuntimeException ex) {
                if (tx != null) {
                    tx.rollback();
                }
            }
        }
        return null;
    }
}
