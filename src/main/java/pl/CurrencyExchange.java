package pl;

import pl.model.Currency;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CurrencyExchange {

    private static final String WEB_URL = "http://www.webservicex.com/currencyconvertor.asmx/ConversionRate?";

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
