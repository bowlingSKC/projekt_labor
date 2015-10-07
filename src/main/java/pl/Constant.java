package pl;

import org.hibernate.Query;
import org.hibernate.Session;
import pl.jpa.SessionUtil;
import pl.model.Bank;
import pl.model.Currency;
import pl.model.TransactionType;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class Constant {

    private static final SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyy. MMMM dd");
    private static final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());

    private static List<Bank> banks = new LinkedList<>();
    private static List<Currency> currencies = new LinkedList<>();
    private static List<TransactionType> transactionTypes = new LinkedList<>();

    public static void init() {
        Session session = SessionUtil.getSession();
        getBanksFromDatabase(session);
        getCurrenciesFromDatabase(session);
        getTransactionTypes(session);
        session.close();
    }

    private static void getTransactionTypes(Session session) {
        Query query = session.createQuery("from TransactionType");
        transactionTypes.addAll(query.list());
    }

    private static void getBanksFromDatabase(Session session) {
        Query query = session.createQuery("from Bank order by name asc");
        banks.addAll(query.list());
    }

    private static void getCurrenciesFromDatabase(Session session) {
        Query query = session.createQuery("from Currency order by name asc");
        currencies.addAll(query.list());
    }

    // ======== GETTER ========
    public static SimpleDateFormat getDateFormat() {
        return yyyyMMdd;
    }

    public static NumberFormat getNumberFormat() {
        return numberFormat;
    }

    public static List<Bank> getBanks() {
        return banks;
    }

    public static List<Currency> getCurrencies() {
        return currencies;
    }

    public static List<TransactionType> getTransactionTypes() {
        return transactionTypes;
    }
}
