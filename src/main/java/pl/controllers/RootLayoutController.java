package pl.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import pl.Main;
import pl.MessageBox;
import pl.animations.FadeInLeftTransition;
import pl.animations.FadeInLeftTransition1;
import pl.animations.FadeInRightTransition;
import pl.animations.FadeInUpTransition;
import pl.bundles.Bundles;
import pl.jpa.SessionUtil;
import pl.model.Login;
import pl.model.User;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;
import java.util.ResourceBundle;

public class RootLayoutController {

    @FXML
    private Label titleLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label pswdLabel;
    @FXML
    private TextField emailTextField;
    @FXML
    private PasswordField pswdTextField;
    @FXML
    private Label windowCloseLabel;
    @FXML
    private Button loginButton;
    @FXML
    private Button regButton;
    @FXML
    private Button forgetPswd;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            new FadeInLeftTransition(emailLabel).play();
            new FadeInLeftTransition(titleLabel).play();
            new FadeInLeftTransition1(pswdLabel).play();
            new FadeInLeftTransition1(emailTextField).play();
            new FadeInLeftTransition1(pswdTextField).play();

            new FadeInRightTransition(loginButton).play();
            new FadeInUpTransition(regButton).play();
            new FadeInUpTransition(forgetPswd).play();
            windowCloseLabel.setOnMouseClicked((MouseEvent event) -> {
                Platform.exit();
                System.exit(0);
            });
        });
    }

    @FXML
    private void handleLogin() {
        if( emailTextField.getText().length() == 0 || pswdTextField.getText().length() == 0 ) {
            MessageBox.showErrorMessage("Hiba", "Nem tölttél ki minden mez?t!", "Minden mez? kitöltése kötelez?", false);
            return;
        }

        Session session = SessionUtil.getSession();
        try {
            Criteria criteria = session.createCriteria(User.class);
            User user = (User) criteria.add(Restrictions.eq("email", emailTextField.getText())).uniqueResult();

            Login login = null;
            if( user == null ) {
                login = new Login(null, InetAddress.getLocalHost().getHostAddress(), new Date(), false);
                MessageBox.showErrorMessage("Hiba", "Rossz bejelentkezési adatok", "Ilyen E-mail cím nem található a rendszerben!", false);
            } else {
                if( !(Main.getSHA512Hash( pswdTextField.getText(), user.getSalt() ).equals(user.getPassword())) ) {
                    login = new Login(user, InetAddress.getLocalHost().getHostAddress(), new Date(), false);
                    MessageBox.showErrorMessage("Hiba", "Rossz bejelentkezési adatok", "Hibás E-mail cím és jelszó páros!", false);
                } else {
                    login = new Login(user, InetAddress.getLocalHost().getHostAddress(), new Date(), true);
                    Main.login(user);
                }
            }
            Transaction tx = session.beginTransaction();
            session.save(login);
            tx.commit();
        } catch (Throwable ex) {
            ex.printStackTrace();
        } finally {
            session.close();
        }
    }

    @FXML
    private void handleNewPassword() {
        try {
            Stage stage = new Stage();
            stage.initOwner( Main.getPrimaryStage() );
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);

            FXMLLoader loader = new FXMLLoader( Main.class.getResource("../layout/ForgotPassword.fxml") );
            AnchorPane pane = loader.load();

            Scene scene = new Scene(pane);
            stage.setScene(scene);

            ForgotPasswordController controller = loader.getController();
            controller.setDialogStage(stage);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error!");
        }

    }

    @FXML
    private void handleRegistration() {
        try {
            Stage stage = new Stage();
            stage.setTitle("Regisztráció");
            stage.initOwner( Main.getPrimaryStage() );
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);

            FXMLLoader loader = new FXMLLoader( Main.class.getResource("../layout/Registration.fxml"), Bundles.getBundle() );
            AnchorPane pane = (AnchorPane) loader.load();

            RegistrationController controller = loader.getController();
            controller.setDialogStage(stage);

            Scene scene = new Scene(pane);
            stage.setScene(scene);

            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}