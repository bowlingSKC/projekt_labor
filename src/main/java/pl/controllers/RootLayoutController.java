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
            System.out.println("Nem t�lt�tt�l ki minen mez?t!");
            return;
        }

        try {
            Session session = SessionUtil.getSession();
            Criteria criteria = session.createCriteria(User.class);
            User user = (User) criteria.add(Restrictions.eq("email", emailTextField.getText())).uniqueResult();

            if( user == null || !(Main.getSHA512Hash( pswdTextField.getText(), user.getSalt() ).equals(user.getPassword())) ) {

                if( !(Main.getSHA512Hash( pswdTextField.getText(), user.getSalt() ).equals(user.getPassword())) ) {
                    Login login = new Login(user, InetAddress.getLocalHost().getHostAddress(), new Date(), false);
                    Transaction tx = session.beginTransaction();
                    session.save(login);
                    tx.commit();
                }
                System.out.println("Rossz bejelentkez�si adatok!");
            } else {
                Login login = new Login(user, InetAddress.getLocalHost().getHostAddress(), new Date(), true);
                Transaction tx = session.beginTransaction();
                session.save(login);
                tx.commit();
                Main.login(user);
            }

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