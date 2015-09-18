package pl;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.hibernate.Session;
import org.hibernate.Transaction;
import pl.controllers.RootLayoutController;
import pl.jpa.SessionUtil;
import pl.model.Account;
import pl.model.Bank;
import pl.model.User;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Ide ki kéne találni valami jót ...");

        initLayout();
    }

    private void initLayout() {
        try {
            FXMLLoader loader = new FXMLLoader( Main.class.getResource("../layout/RootLayout.fxml") );
            AnchorPane pane = (AnchorPane) loader.load();

            Scene scene = new Scene(pane);
            this.primaryStage.setScene(scene);

            RootLayoutController rootLayoutController = loader.getController();

            this.primaryStage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String getSHA512Hash(String pswd, String salt) throws NoSuchAlgorithmException {
        String generatedPassword = null;
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(salt.getBytes());
        byte[] bytes = md.digest(pswd.getBytes());
        StringBuilder sb = new StringBuilder();
        for( int i = 0; i < bytes.length; i++ ) {
            sb.append( Integer.toString( (bytes[i] & 0xff) + 0x100 ).substring(1) );
        }
        generatedPassword = sb.toString();
        return generatedPassword;
    }

    public static String getSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt.toString();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
