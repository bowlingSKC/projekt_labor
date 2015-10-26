package pl.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import org.hibernate.Session;
import org.hibernate.Transaction;
import pl.Main;
import pl.dao.UserDao;
import pl.jpa.SessionUtil;

public class DeleteUserController {

    @FXML
    private PasswordField passfordField;
    @FXML
    private Button submitButton;

    @FXML
    public void initialize() {
        submitButton.setDisable(true);

        passfordField.setOnKeyReleased(event -> {
            try {
                final String typedPlainPassword = passfordField.getText();
                final String typedPassword = Main.getSHA512Hash(typedPlainPassword, Main.getLoggedUser().getSalt());
                if( Main.getLoggedUser().getPassword().equals(typedPassword) ) {
                    submitButton.setDisable(false);
                } else {
                    submitButton.setDisable(true);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    @FXML
    private void handleDelete() {
        UserDao.deleteUser(Main.getLoggedUser());
        Main.logout();
    }

}
