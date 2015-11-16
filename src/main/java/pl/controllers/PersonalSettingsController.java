package pl.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.hibernate.Session;
import org.hibernate.Transaction;
import pl.Constant;
import pl.Main;
import pl.MessageBox;
import pl.bundles.Bundles;
import pl.jpa.SessionUtil;
import pl.model.User;

public class PersonalSettingsController {

    @FXML
    private TextField firstnameField;
    @FXML
    private TextField lastnameField;
    @FXML
    private TextField emailField;
    @FXML
    private ComboBox<String> languageBox;

    @FXML
    public void initialize() {
        languageBox.getItems().setAll("magyar", "angol");

        firstnameField.setText(Main.getLoggedUser().getFirstname());
        lastnameField.setText(Main.getLoggedUser().getLastname());
        emailField.setText(Main.getLoggedUser().getEmail());

        if( Main.getLoggedUser().getLanguage().equals("hu") ) {
            languageBox.getSelectionModel().select(0);
        } else {
            languageBox.getSelectionModel().select(1);
        }
    }

    @FXML
    private void handleSave() {
        try {
            checkAllFields();

            User user = Main.getLoggedUser();
            user.setFirstname(firstnameField.getText());
            user.setLastname(lastnameField.getText());
            user.setEmail(emailField.getText());

            if( languageBox.getSelectionModel().getSelectedIndex() == 0 ) {
                user.setLanguage("hu");
                Bundles.setLanguage("hu");
            } else if(languageBox.getSelectionModel().getSelectedIndex() == 1) {
                user.setLanguage("en");
                Bundles.setLanguage("en");
            }

            Session session = SessionUtil.getSession();
            Transaction tx = session.beginTransaction();
            session.update(user);
            tx.commit();
            session.close();

            Main.logout();
        } catch (Exception ex) {
            MessageBox.showErrorMessage("Hiba", "Hiba az adatok feldolgozása közben!", ex.getMessage(), false);
        }
    }

    private void checkAllFields() throws Exception {
        StringBuilder builder = new StringBuilder();

        if( firstnameField.getText().length() == 0 ) {
            builder.append("A vezetéknév mező kitöltése közelező!\n");
        }

        if( lastnameField.getText().length() == 0 ) {
            builder.append("A keresztnév mező kitöltése kötelező!\n");
        }

        if( emailField.getText().length() == 0 ) {
            builder.append("Az E-mail cím mező kitöltése kötelező!\n");
        }

        if(!Constant.isValidEmail(emailField.getText().trim())) {
            builder.append("Az E-mail cím felépítése nem megfelelő!\n");
        }

        if( builder.toString().length() != 0 ) {
            throw new Exception(builder.toString());
        }
    }

}
