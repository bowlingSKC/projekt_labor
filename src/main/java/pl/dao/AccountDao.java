package pl.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import pl.jpa.SessionUtil;
import pl.model.Account;

public class AccountDao {

    public static void createNewAccount(Account account) {
        Session session = SessionUtil.getSession();
        Transaction tx = session.beginTransaction();
        session.save(account);
        tx.commit();
        session.close();
    }

    public static void updateAccount(Account account) {
        Session session = SessionUtil.getSession();
        Transaction tx = session.beginTransaction();
        session.update(account);
        tx.commit();
        session.close();
    }

}
