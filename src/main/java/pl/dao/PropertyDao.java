package pl.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import pl.jpa.SessionUtil;
import pl.model.Property;

public class PropertyDao {

    public static void createProperty(Property newProperty) {

    }

    public static void updateProperty(Property property) {

    }

    public static void sellProperty(Property property) {

    }

    public static void deleteProperty(Property property) {
        Session session = SessionUtil.getSession();
        Transaction tx = session.beginTransaction();
        session.delete(property);
        tx.commit();
        session.close();
    }

}
