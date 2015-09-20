package pl.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.hibernate.Session;
import pl.Main;
import pl.MessageBox;
import pl.jpa.SessionUtil;
import pl.model.User;

import java.util.Optional;

public class NewPasswordController {

    private Stage dialogStage;

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
            MessageBox.showErrorMessage("Hiba", "A két jelszó nem egyezik!", "", false);
        } else if( newPasswordField.getText().length() < 8 || newPasswordField.getText().length() > 20 ) {
            MessageBox.showErrorMessage("Hiba", "Az új jelszó nem felel meg a feltételeknek!",
                    "A jelszavaknak 8 és 20 karakter közötti hosszúságúnak kell lennie.", false);
        } else if( confirm() ) {
            try {
                String newSalt = Main.getSalt();
                String newPass = Main.getSHA512Hash(newPasswordConfirmField.getText(), newSalt);
                changePassword(newPass, newSalt);
            } catch (Throwable ex) {
                ex.printStackTrace();
                MessageBox.showErrorMessage("Hiba", "Bels? hiba miatt nem lehetett megváltozatatni a jelszót!",
                        "Próbálja kés?bb.", false);
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

        MessageBox.showInformationMessage("Jelszó megváltoztatása", "A jelszó sikeresen megváltozott!",
                "Legközelebb az új jelszóval léphet be. Az új jelszó ki lett küldve a regisztrált E-mail címre.", false);

        dialogStage.close();
    }

    private boolean confirm() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Jelszó megváltoztatása");
        alert.setHeaderText("Biztosan meg szeretnéd változtatni a jelenlegi jelszavad?");
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

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
}
