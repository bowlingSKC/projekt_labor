package pl.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
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
    private Label closeDialogLabel;
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
            MessageBox.showErrorMessage("Hiba", "A k�t jelsz� nem egyezik!", "", false);
        } else if( newPasswordField.getText().length() < 8 || newPasswordField.getText().length() > 20 ) {
            MessageBox.showErrorMessage("Hiba", "Az �j jelsz� nem felel meg a felt�teleknek!",
                    "A jelszavaknak 8 �s 20 karakter k�z�tti hossz�s�g�nak kell lennie.", false);
        } else if( confirm() ) {
            try {
                String newSalt = Main.getSalt();
                String newPass = Main.getSHA512Hash(newPasswordConfirmField.getText(), newSalt);
                changePassword(newPass, newSalt);
            } catch (Throwable ex) {
                ex.printStackTrace();
                MessageBox.showErrorMessage("Hiba", "Bels? hiba miatt nem lehetett megv�ltozatatni a jelsz�t!",
                        "Pr�b�lja k�s?bb.", false);
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

        MessageBox.showInformationMessage("Jelsz� megv�ltoztat�sa", "A jelsz� sikeresen megv�ltozott!",
                "Legk�zelebb az �j jelsz�val l�phet be. Az �j jelsz� ki lett k�ldve a regisztr�lt E-mail c�mre.", false);

        dialogStage.close();
    }

    private boolean confirm() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Jelsz� megv�ltoztat�sa");
        alert.setHeaderText("Biztosan meg szeretn�d v�ltoztatni a jelenlegi jelszavad?");
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

        closeDialogLabel.setOnMouseClicked(event -> dialogStage.close());
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
}
