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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Constant {

    private static final SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyy. MMMM dd.");
    private static final SimpleDateFormat yyyyMMddHHssmm = new SimpleDateFormat("yyy. MMMM dd. HH:ss:mm");
    private static final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());

    private static List<Bank> banks = new LinkedList<>();
    private static List<Currency> currencies = new LinkedList<>();
    private static List<TransactionType> transactionTypes = new LinkedList<>();

    private static Currency hufCurrency;

    private static TransactionType cashInType;
    private static TransactionType cashOutType;

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
        for(TransactionType type : transactionTypes) {
            if( type.getId() == 4 ) {
                cashOutType = type;
            } else if( type.getId() == 5 ) {
                cashInType = type;
            }
        }
    }

    private static void getBanksFromDatabase(Session session) {
        Query query = session.createQuery("from Bank order by name asc");
        banks.addAll(query.list());
    }

    private static void getCurrenciesFromDatabase(Session session) {
        Query query = session.createQuery("from Currency order by id asc");
        currencies.addAll(query.list());

        for(Currency currency : currencies) {
            if( currency.getCode().equals("HUF") ) {
                hufCurrency = currency;
                break;
            }
        }
    }

    public static boolean isValidEmail(String email) {
        final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    // ======== GETTER ========
    public static SimpleDateFormat getDateFormat() {
        return yyyyMMdd;
    }

    public static SimpleDateFormat getDateTimeFormat() {
        return yyyyMMddHHssmm;
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

    public static Date dateFromLocalDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDate localDateFromDate(Date date) {
        Instant instant = Instant.ofEpochMilli(date.getTime());
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate();
    }

    public static Currency getHufCurrency() {
        return hufCurrency;
    }

    public static TransactionType getCashInType() {
        return cashInType;
    }

    public static TransactionType getCashOutType() {
        return cashOutType;
    }

}
