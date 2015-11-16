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
import org.controlsfx.control.Notifications;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import pl.Constant;
import pl.Main;
import pl.MessageBox;
import pl.animations.FadeInLeftTransition;
import pl.animations.FadeInLeftTransition1;
import pl.animations.FadeInRightTransition;
import pl.animations.FadeInUpTransition;
import pl.bundles.Bundles;
import pl.jpa.SessionUtil;
import pl.model.Debit;
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
            MessageBox.showErrorMessage("Hiba", "Nem tï¿½lttï¿½l ki minden mez?t!", "Minden mez? kitï¿½ltï¿½se kï¿½telez?", false);
            return;
        }

        Session session = SessionUtil.getSession();
        try {
            Criteria criteria = session.createCriteria(User.class);
            User user = (User) criteria.add(Restrictions.eq("email", emailTextField.getText())).uniqueResult();
            session.close();

            Login login = null;
            if( user == null ) {
                login = new Login(null, InetAddress.getLocalHost().getHostAddress(), new Date(), false);
                MessageBox.showErrorMessage("Hiba", "Rossz bejelentkezï¿½si adatok", "Ilyen E-mail cï¿½m nem talï¿½lhatï¿½ a rendszerben!", false);
            } else {
                if( !(Main.getSHA512Hash( pswdTextField.getText(), user.getSalt() ).equals(user.getPassword())) ) {
                    login = new Login(user, InetAddress.getLocalHost().getHostAddress(), new Date(), false);
                    MessageBox.showErrorMessage("Hiba", "Rossz bejelentkezï¿½si adatok", "Hibï¿½s E-mail cï¿½m ï¿½s jelszï¿½ pï¿½ros!", false);
                } else {
                    login = new Login(user, InetAddress.getLocalHost().getHostAddress(), new Date(), true);
                    Main.login(user);
                    user.normalizeProfil();

                    if( Main.getLoggedUser().getLogins() != null && Main.getLoggedUser().getLogins().size() != 0 ) {
                        Notifications.create()
                                .title("Utolsó bejelentkezése")
                                .text(Constant.getDateTimeFormat().format(user.getLastLogin()))
                                .showInformation();
                    }

                    if( Main.getLoggedUser().getDebits() != null && Main.getLoggedUser().getDebits().size() != 0 ) {
                        for(Debit debit : Main.getLoggedUser().getDebits()) {
                            long nowTime = new Date().getTime();
                            long deadlineTime = debit.getDeadline().getTime();
                            int diffDays = (int)((deadlineTime - nowTime) / (1000 * 60 * 60 * 24));
                            if(diffDays == 0) {
                                Notifications.create()
                                        .title("Lejáró tartozás!")
                                        .text(debit.getName() + " tartozás ma lejár!")
                                        .showError();
                            } else if(diffDays < 3) {
                                Notifications.create()
                                        .title("Hamarosan lejáró tartozás!")
                                        .text(debit.getName() + " tartozás három napon belül lejár!")
                                        .showWarning();
                            } else if(diffDays < 7) {
                                Notifications.create()
                                        .title("Hamarosan lejáró tartozás!")
                                        .text(debit.getName() + " tartozás hét napon belül lejár!")
                                        .showInformation();
                            }
                        }
                    }

                }
            }

            session = SessionUtil.getSession();
            Transaction tx = session.beginTransaction();
            session.save(login);
            if( user != null ) user.getLogins().add(login);
            tx.commit();

        } catch (Throwable ex) {
            ex.printStackTrace();
        } finally {
            if(session.isConnected()) session.close();
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
            stage.setTitle("Regisztrï¿½ciï¿½");
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