package pl.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.hibernate.Session;
import pl.Main;
import pl.MessageBox;
import pl.bundles.Bundles;
import pl.jpa.SessionUtil;
import pl.model.User;

import java.util.Optional;

public class NewPasswordController {

    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField newPasswordConfirmField;
    @FXML
    private PasswordField currentPasswordField;
    @FXML
    private Button saveButton;

    @FXML
    private void handleSave() {
        if( !newPasswordField.getText().equals(newPasswordConfirmField.getText()) ) {
            MessageBox.showErrorMessage(Bundles.getString("error.nodb.title"), Bundles.getString("passwordmismatch"), "", false);
        } else if( newPasswordField.getText().length() < 8 || newPasswordField.getText().length() > 20 ) {
            MessageBox.showErrorMessage(Bundles.getString("error.nodb.title"), Bundles.getString("passwordlen"),
                    Bundles.getString("passwordlen"), false);
        } else if( confirm() ) {
            try {
                String newSalt = Main.getSalt();
                String newPass = Main.getSHA512Hash(newPasswordConfirmField.getText(), newSalt);
                changePassword(newPass, newSalt);
            } catch (Throwable ex) {
                ex.printStackTrace();
                MessageBox.showErrorMessage(Bundles.getString("error.nodb.title"), Bundles.getString("error.processing"),
                        Bundles.getString("trylater"), false);
            }
        }
    }

    private void changePassword(String newPass, String newSalt) {
        User loggedUser = Main.getLoggedUser();
        loggedUser.setSalt(newSalt);
        loggedUser.setPassword(newPass);
        Session session = SessionUtil.getSession();
        org.hibernate.Transaction tx = session.beginTransaction();
        session.update(loggedUser);
        tx.commit();
        session.close();

        MessageBox.showInformationMessage(Bundles.getString("changepassword"), Bundles.getString("changepwok"),
                Bundles.getString("pwsent"), false);
    }

    private boolean confirm() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(Bundles.getString("changepassword"));
        alert.setHeaderText(Bundles.getString("changepwsure"));
        Optional<ButtonType> result = alert.showAndWait();
        if( result.get() == ButtonType.OK ) {
            return true;
        }
        return false;
    }

    @FXML
    public void initialize() {
        saveButton.setDisable(true);
        currentPasswordField.textProperty().addListener((obs, oldValue, newValue) -> {
            try {
                if( Main.getSHA512Hash(currentPasswordField.getText(), Main.getLoggedUser().getSalt()).equals(Main.getLoggedUser().getPassword()) ) {
                    saveButton.setDisable(false);
                } else {
                    saveButton.setDisable(true);
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        });
    }

}
