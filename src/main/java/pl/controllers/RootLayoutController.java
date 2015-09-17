package pl.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import pl.Main;
import pl.jpa.SessionUtil;
import pl.model.User;

import java.io.IOException;

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

        Session session = SessionUtil.getSession();
        Criteria criteria = session.createCriteria(User.class);
        User user = (User) criteria.add(Restrictions.eq("email", emailTextField.getText())).uniqueResult();
        session.close();

        if( user == null || !user.getPassword().equals(pswdTextField.getText()) ) {
            System.out.println("Rossz bejelentkez�si adatok!");
        } else {
            System.out.println("Be vagy jelentkezve");
        }
    }

    @FXML
    private void handleNewPassword() {
        try {
            Stage stage = new Stage();
            stage.initOwner( Main.getPrimaryStage() );
            stage.initModality(Modality.WINDOW_MODAL);

            FXMLLoader loader = new FXMLLoader( Main.class.getResource("../layout/NewPassword.fxml") );
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

            Scene scene = new Scene(pane);
            stage.setScene(scene);

            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}