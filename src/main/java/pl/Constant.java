package pl;

import org.hibernate.Query;
import org.hibernate.Session;
import pl.jpa.SessionUtil;
import pl.model.Bank;
import pl.model.Currency;
import pl.model.TransactionType;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class Constant {

    private static final SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyy. MMMM dd.");
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
        Query query = session.createQuery("from Currency order by id asc");
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

    public static Currency getHufCurrency() {
        Currency currency = null;
        for(Currency currency1 : getCurrencies()) {
            if( currency1.getCode().equals("HUF") ) {
                currency = currency1;
                break;
            }
        }
        return currency;
    }

    public static List<TransactionType> getTransactionTypes() {
        return transactionTypes;
    }

    public static Date dateFromLocalDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDate localDateFromDate(Date date) {
        Instant instant = Instant.ofEpochMilli(date.getTime());
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate();
    }
}
