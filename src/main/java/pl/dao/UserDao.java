package pl.dao;

import pl.Main;
import pl.exceptions.EmailIsAlreadyInDatabaseException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import pl.jpa.SessionUtil;
import pl.model.Account;
import pl.model.Login;
import pl.model.User;

import java.util.List;

public class UserDao {

    public static void register(User user) throws Exception {

        Session session = SessionUtil.getSession();
        Transaction tx = session.beginTransaction();

        Query query = session.createQuery("from User where email = :email");
        query.setParameter("email", user.getEmail());
        User dbUser = (User) query.uniqueResult();

        if( dbUser != null ) {
            session.close();
            throw new EmailIsAlreadyInDatabaseException();
        }

        tx.commit();
        session.close();
    }

    public static void deleteUser(User user) {
        Session session = SessionUtil.getSession();
        Transaction tx = session.beginTransaction();

        // delete logins
        Query loginsQuery = session.createQuery("from Login where user = :user");
        loginsQuery.setParameter("user", Main.getLoggedUser());
        loginsQuery.list().forEach(session::delete);

        // readycash
        Query readyCash = session.createQuery("from ReadyCash where owner = :user");
        readyCash.setParameter("user", Main.getLoggedUser());
        readyCash.list().forEach(session::delete);

        // accounts
        Query accountsQuery = session.createQuery("from Account where owner = :user");
        accountsQuery.setParameter("user", Main.getLoggedUser());
        for( Account acc : (List<Account>)accountsQuery.list() ) {
            Query transActionQuery = session.createQuery("from Transaction where account = :acc");
            transActionQuery.setParameter("acc", acc);
            transActionQuery.list().forEach(session::delete);
            session.delete(acc);
        }

        // debit
        Query debitQuery = session.createQuery("from Debit where owner = :user");
        debitQuery.setParameter("user", Main.getLoggedUser());
        debitQuery.list().forEach(session::delete);

        // pocket
        Query pocketQuery = session.createQuery("from Pocket where owner = :user");
        pocketQuery.setParameter("user", Main.getLoggedUser());
        pocketQuery.list().forEach(session::delete);

        // property
        Query propertyQuery = session.createQuery("from Property where owner = :user");
        propertyQuery.setParameter("user", Main.getLoggedUser());
        propertyQuery.list().forEach(session::delete);

        tx.commit();
        session.close();
    }

}
