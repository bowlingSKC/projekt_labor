package pl;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.controlsfx.control.Notifications;
import pl.bundles.Bundles;
import pl.controllers.LoggedController;
import pl.controllers.SplashController;
import pl.model.Debit;
import pl.model.User;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

public class Main extends Application {

    private static Stage primaryStage;
    private static Stage loginStage;

    private Scene splashScene;
    private Scene loginScene;

    private static ObjectProperty<User> loggedUser = new SimpleObjectProperty<>();

    private static final File settingsFile = new File("settings.dat");

    @Override
    public void start(Stage primaryStage) throws Exception {
        Main.primaryStage = primaryStage;
        Main.primaryStage.getIcons().add(new Image("imgs/money_icon.png"));
        Main.primaryStage.setTitle( Bundles.getString("splash.title") );

        initLayout();
    }

    private void initLayout() {
        final SplashController controller;
        try {
            FXMLLoader loader = new FXMLLoader( Main.class.getResource("../layout/Splash.fxml"), Bundles.getBundle());
            AnchorPane pane = loader.load();

            controller = loader.getController();

            splashScene = new Scene(pane);
            primaryStage.setScene(splashScene);
            primaryStage.initStyle(StageStyle.UNDECORATED);

            primaryStage.sceneProperty().addListener((observable, oldValue, newValue) -> primaryStage.centerOnScreen());
            primaryStage.show();

            Service service = new Service() {
                @Override
                protected Task createTask() {
                    return new Task() {
                        @Override
                        protected Object call() throws Exception {
                            Constant.init();

                            readSettingsFromFile();

                            FXMLLoader loader = new FXMLLoader( Main.class.getResource("../layout/RootLayout.fxml"), Bundles.getBundle() );
                            AnchorPane pane = loader.load();
                            loginScene = new Scene(pane);

                            return null;
                        }
                    };
                }
            };

            service.start();
            service.setOnSucceeded(event -> {
                primaryStage.setScene(loginScene);
            });

            service.setOnFailed(event -> {
                MessageBox.showErrorMessage(
                        Bundles.getString("error.nodb.title"), Bundles.getString("error.nodb.header"), Bundles.getString("error.nodb.text"), true);
                Platform.exit();
                System.exit(0);
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void readSettingsFromFile() {
        if( !settingsFile.exists() ) {
            handleCreateSettingsFile();
        }
    }

    private void handleCreateSettingsFile() {
        try {
            settingsFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getSHA512Hash(String pswd, String salt) throws NoSuchAlgorithmException {
        String generatedPassword;
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(salt.getBytes());
        byte[] bytes = md.digest(pswd.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100).substring(1));
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

    public static void login(User user) {
        loggedUser.setValue(user);
        primaryStage.hide();


        try {
            loginStage = new Stage();

            FXMLLoader loader = new FXMLLoader(Main.class.getResource("../layout/Logged.fxml"), Bundles.getBundle());
            Parent loggedpane = loader.load();
            LoggedController controller = loader.getController();

            Scene scene = new Scene(loggedpane);
            loginStage.setScene(scene);
            controller.setDialogStage(loginStage);

            loginStage.initStyle(StageStyle.UNDECORATED);
            loginStage.show();

            Platform.runLater(CurrencyExchange::updateCurrencies);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void logout() {
        loggedUser.setValue(null);

        loginStage.close();
        primaryStage.show();
        System.gc();
    }

    public static Stage getPrimaryStage() {
        return loginStage;
    }

    public static User getLoggedUser() {
        return loggedUser.get();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
