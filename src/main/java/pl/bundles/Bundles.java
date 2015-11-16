package pl.bundles;


import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;


public final class Bundles {

    private static ResourceBundle bundle;
    private static Locale locale = new Locale(Locale.getDefault().getLanguage(), Locale.getDefault().getCountry());
//    private static Locale locale = new Locale("en", "EN");
//    private static Locale locale = new Locale("hu", "HU");

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
            bundle = ResourceBundle.getBundle(packageName + ".bundle", locale, new UTF8Control()); //NOI18N
        }
        return bundle;
    }

    public static synchronized String getDefaultLanguage() {
        return Locale.getDefault().getLanguage();
    }

    public static void setLanguage(String lang) {
        bundle = null;
        if( lang.equals("hu") ) {
            locale = new Locale(lang, "HU");
        } else {
            locale = new Locale(lang, "EN");
        }
    }
}
