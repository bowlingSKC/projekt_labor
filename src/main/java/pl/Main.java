package pl;

import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.hibernate.Session;
import pl.bundles.Bundles;
import pl.controllers.LoggedController;
import pl.controllers.RootLayoutController;
import pl.controllers.SplashController;
import pl.jpa.SessionUtil;
import pl.model.User;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Main extends Application {

    private static Stage primaryStage;
    private static Scene logoutScene;
    private static Scene loginScene;

    private static ObjectProperty<User> loggedUser = new SimpleObjectProperty<>();

    @Override
    public void start(Stage primaryStage) throws Exception {
        Main.primaryStage = primaryStage;
        Main.primaryStage.getIcons().add( new Image("imgs/money_icon.png") );
        Main.primaryStage.setTitle(" --- CÍM --- ");


        initLayout();
        loggedUser.addListener((obs, oldValue, newValue) -> {
            if( newValue == null ) {
                Main.primaryStage.setScene(logoutScene);
            } else {
                Main.primaryStage.setScene(loginScene);
            }
        });
    }

    private void initLayout() {
        try {
            FXMLLoader loader = new FXMLLoader( Main.class.getResource("../layout/Splash.fxml"), Bundles.getBundle());
            AnchorPane pane = loader.load();

            Scene scene = new Scene(pane);
            primaryStage.setScene(scene);
            primaryStage.setScene(scene);
            primaryStage.initStyle(StageStyle.UNDECORATED);

            primaryStage.sceneProperty().addListener((observable, oldValue, newValue) -> primaryStage.centerOnScreen());

            primaryStage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Service service = new Service() {
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected Object call() throws Exception {
                        Session session = SessionUtil.getSession();
                        session.close();

                        System.out.println("Hibernate ok");

                        FXMLLoader loader = new FXMLLoader( Main.class.getResource("../layout/RootLayout.fxml"), Bundles.getBundle() );
                        AnchorPane pane = loader.load();

                        logoutScene = new Scene(pane);
                        return null;
                    }
                };
            }
        };

        service.start();
        service.setOnSucceeded(event -> {
            System.out.println("Successed");
            primaryStage.setScene(logoutScene);
        });
    }

    public static String getSHA512Hash(String pswd, String salt) throws NoSuchAlgorithmException {
        String generatedPassword;
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

    public static void login(User user) {
        loggedUser.setValue(user);
        try {
            FXMLLoader loader2 = new FXMLLoader( Main.class.getResource("../layout/Logged.fxml") );
            BorderPane loggedpane = loader2.load();

            LoggedController controller = loader2.getController();
            controller.setLayout(loggedpane);
            controller.setDialogStage(primaryStage);

            Scene scene = new Scene(loggedpane);
            primaryStage.setScene(scene);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void logout() {
        loggedUser.setValue(null);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static User getLoggedUser() {
        return loggedUser.get();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
