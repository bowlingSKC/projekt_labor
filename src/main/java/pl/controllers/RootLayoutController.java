package pl.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import pl.Main;
import pl.MessageBox;
import pl.jpa.SessionUtil;
import pl.model.Login;
import pl.model.User;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;

public class RootLayoutController {

    @FXML
    private TextField emailTextField;
    @FXML
    private PasswordField pswdTextField;

    @FXML
    private void handleLogin() {
        if( emailTextField.getText().length() == 0 || pswdTextField.getText().length() == 0 ) {
            System.out.println("Nem tï¿½ltï¿½ttï¿½l ki minen mez?t!");
            return;
        }

        try {
            Session session = SessionUtil.getSession();
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
            session.close();

        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }
    @FXML
    public void handleEnter(KeyEvent e) {
        if(!e.isAltDown() && !e.isShiftDown() && !e.isControlDown() && !e.isMetaDown()){
            System.out.println("Enter pressed!");
            handleLogin();
        }

    }

    @FXML
    private void handleNewPassword() {
        try {
            Stage stage = new Stage();
            stage.initOwner( Main.getPrimaryStage() );
            stage.initModality(Modality.WINDOW_MODAL);

            FXMLLoader loader = new FXMLLoader( Main.class.getResource("../layout/ForgotPassword.fxml") );
            AnchorPane pane = pane = (AnchorPane) loader.load();

            Scene scene = new Scene(pane);
            stage.setScene(scene);

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

            FXMLLoader loader = new FXMLLoader( Main.class.getResource("../layout/Registration.fxml") );
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