package pl;

import javafx.application.Platform;
import pl.model.Account;
import pl.model.Currency;
import pl.model.Debit;
import pl.model.ReadyCash;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CurrencyExchange {

    private static final String WEB_URL = "http://www.webservicex.com/currencyconvertor.asmx/ConversionRate?";

    private static Map<Currency, Float> currencies = new HashMap<>();

    private static synchronized void addCurrenciesToMap(Currency currency, Float value) {
        currencies.put(currency, value);
    }

    public static synchronized boolean isContainsKey(Currency currency) {
        return currencies.containsKey(currency);
    }

    public static synchronized float getValue(Currency currency) {
        return currencies.get(currency);
    }

    public static void updateCurrencies() {
        Set<Currency> currencySet = Main.getLoggedUser().getReadycash().stream().map(ReadyCash::getCurrency).collect(Collectors.toSet());
        currencySet.addAll(Main.getLoggedUser().getAccounts().stream().map(Account::getCurrency).collect(Collectors.toList()));
        currencySet.addAll(Main.getLoggedUser().getDebits().stream().map(Debit::getCurrency).collect(Collectors.toList()));

        long start = System.currentTimeMillis();
        for(Currency currency : currencySet) {
            new Thread(() -> CurrencyExchange.addCurrenciesToMap(currency, toHuf(currency))).start();
        }
        System.out.println( (System.currentTimeMillis() - start) + " msec-ig tartott." );
    }

    public static float toHuf(Currency currency, float amount) {
        return xToY(currency, Constant.getHufCurrency(), amount);
    }

    public static float toHuf(Currency currency) {
        return toHuf(currency, 1.0f);
    }

    public static float xToY(Currency from, Currency to, float amount) {
        try {
            long start = System.currentTimeMillis();

            String actualWebUrl = WEB_URL + "FromCurrency="+ from.getCode() +"&ToCurrency=" + to.getCode();
            URL obj = new URL(actualWebUrl);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");

            StringBuffer buffer = new StringBuffer();
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            while( (line = br.readLine()) != null ) {
                buffer.append(line);
            }
            br.close();

            float value = Float.valueOf( buffer.toString().substring(82, buffer.toString().lastIndexOf("<")) );

            long end = System.currentTimeMillis();
            System.out.println("Egy arfolyam lekerdezes ideje: " + (end-start) + " ms");

            return value * amount;
        } catch (Exception ex) {
            ex.printStackTrace();
            return 1.0f;
        }
    }

    public static float xToY(Currency from, Currency to) {
        return xToY(from, to, 1);
    }

}
