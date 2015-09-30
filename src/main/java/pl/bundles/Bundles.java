package pl.bundles;


import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;


public class Bundles {

    private static ResourceBundle bundle;
    private static Locale locale = new Locale(Locale.getDefault().getLanguage(), Locale.getDefault().getCountry());
//    private static Locale locale = new Locale("en", "EN");        // ha az angol lokaliz�ci�t szeretn�nk tesztelni
//    private static Locale locale = new Locale("hu", "HU");        // ha a magyar lokaliz�ci�t szeretn�nk tesztelni

    public static String getString(String key) {
        return getBundle().getString(key);
    }

    public static String getString(String key, Object... arguments) {
        final String pattern = getString(key);
        return MessageFormat.format(pattern, arguments);
    }

    public static synchronized ResourceBundle getBundle() {
        if (bundle == null) {
            final String packageName = Bundles.class.getPackage().getName();
            bundle = ResourceBundle.getBundle(packageName + ".bundle", locale); //NOI18N
        }
        return bundle;
    }
}
