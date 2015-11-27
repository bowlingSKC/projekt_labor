package pl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.model.Account;
import pl.model.Currency;
import pl.model.Debit;
import pl.model.ReadyCash;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CurrencyExchange {

    private static final String WEB_URL = "http://www.webservicex.com/currencyconvertor.asmx/ConversionRate?";

    // replace karakterek: <!>
    private static final String JSON_URL = "http://globalcurrencies.xignite.com/xGlobalCurrencies.json/ConvertRealTimeValue?From=<!>&To=<!>&Amount=1&_fields=FromCurrencySymbol,ToCurrencySymbol,Rate&_token=E843B6BA874C4283B6147F12D78544B6";

    private static Map<Currency, Float> currencies = new HashMap<>();

    public static synchronized void addCurrenciesToMap(Currency currency, Float value) {
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

    public static void updateStoredCurrency() {
        for(Currency currency : new HashSet<>(currencies.keySet())) {
            currencies.put(currency, toHuf(currency));
        }
    }

    public static void updateZeroCurrencies() {
        new HashSet<>(currencies.keySet()).stream().filter(currency -> currencies.get(currency) == 0.0f).forEach(currency -> {
            currencies.put(currency, toHuf(currency));
        });
    }

    public static float toHuf(Currency currency, float amount) {
        return xToY(currency, Constant.getHufCurrency(), amount);
    }

    public static float toHuf(Currency currency) {
        return toHuf(currency, 1.0f);
    }

    /*
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
    */

    public static float xToY(Currency from, Currency to, float amount) {
        if( from.equals(to) ) {
            return 1.0f;
        }
        float rate = 1.0f;
        try {
            String newJsonUrl = JSON_URL.replaceFirst("<!>", from.getCode());
            newJsonUrl = newJsonUrl.replaceFirst("<!>", to.getCode());
            System.out.println(newJsonUrl);

            ObjectMapper mapper = new ObjectMapper();
            Result result = mapper.readValue(new URL(newJsonUrl), Result.class);

            System.out.println(result);

            if( result.getRate() != null ) {
                rate = result.getRate();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return rate;
    }

    public static float xToY(Currency from, Currency to) {
        return xToY(from, to, 1);
    }

    private static class Result {
        @JsonProperty("FromCurrencySymbol")
        private String FromCurrencySymbol;
        @JsonProperty("ToCurrencySymbol")
        private String ToCurrencySymbol;
        @JsonProperty("Rate")
        private Float Rate;

        public Result() {
        }

        public Result(String fromCurrencySymbol, String toCurrencySymbol, Float rate) {
            FromCurrencySymbol = fromCurrencySymbol;
            ToCurrencySymbol = toCurrencySymbol;
            Rate = rate;
        }

        public String getFromCurrencySymbol() {
            return FromCurrencySymbol;
        }

        public void setFromCurrencySymbol(String fromCurrencySymbol) {
            FromCurrencySymbol = fromCurrencySymbol;
        }

        public String getToCurrencySymbol() {
            return ToCurrencySymbol;
        }

        public void setToCurrencySymbol(String toCurrencySymbol) {
            ToCurrencySymbol = toCurrencySymbol;
        }

        public Float getRate() {
            return Rate;
        }

        public void setRate(Float rate) {
            Rate = rate;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "FromCurrencySymbol='" + FromCurrencySymbol + '\'' +
                    ", ToCurrencySymbol='" + ToCurrencySymbol + '\'' +
                    ", Rate=" + Rate +
                    '}';
        }
    }
}
