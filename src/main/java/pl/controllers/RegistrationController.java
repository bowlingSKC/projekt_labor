package pl.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.hibernate.Session;
import org.hibernate.Transaction;
import pl.Main;
import pl.MessageBox;
import pl.jpa.SessionUtil;
import pl.model.User;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrationController {

    @FXML
    private TextField firstnameField;
    @FXML
    private TextField lastnameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField pswdField;
    @FXML
    private PasswordField pswdCField;

    private Stage dialogStage;

    @FXML
    private void handleRegistration() {
        try {
            checkFields();

            User newUser = creteUserFromFields();
            Session session = SessionUtil.getSession();
            Transaction tx = session.beginTransaction();
            session.save(newUser);
            tx.commit();
            session.close();

            dialogStage.close();
            MessageBox.showInformationMessage("Regisztráció", "Sikeres regisztráció!", "Most már bejelentkezhetsz az E-mail címeddel és jelzavaddal.", false);
        } catch (Exception ex) {
            MessageBox.showErrorMessage("Hiba", "Nem töltöttél ki minden mez?t helyesen!", ex.getMessage(), false);
        }
    }

    private User creteUserFromFields() {
        try {
            User newUser = new User();
            newUser.setFirstname( firstnameField.getText() );
            newUser.setLastname(lastnameField.getText());
            newUser.setEmail(emailField.getText());
            newUser.setSalt( Main.getSalt() );
            newUser.setPassword( Main.getSHA512Hash(pswdField.getText(), newUser.getSalt()) );
            newUser.setRegistredDate(new Date());
            return newUser;
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private void checkFields() throws Exception {
        StringBuffer buffer = new StringBuffer();

        if( firstnameField.getText().length() == 0 ) {
            buffer.append("Nem töltötted ki a \'Vezetéknév\' mez?t!\n");
        }

        if( lastnameField.getText().length() == 0 ) {
            buffer.append("Nem töltötted ki a \'Keresztnév\' mez?t!\n");
        }

        if( emailField.getText().length() == 0 ) {
            buffer.append("Nem töltötted ki a \'E-mail\' mez?t!\n");
        }

        if( !isValidEmail() ) {
            buffer.append("Helytelen E-mail cím formátumot adtál meg!\n");
        }

        if( pswdField.getText().length() == 0 ) {
            buffer.append("Nem töltötted ki a \'Jelszó\' mez?t!\n");
        }

        if( pswdCField.getText().length() == 0 ) {
            buffer.append("Nem töltötted ki a \'Jelszó meger?sítése\' mez?t!\n");
        }

        if( pswdField.getText().length() <= 8 || pswdField.getText().length() >= 20 ) {
            buffer.append("A jelszónak 8 és 20 karakter közötti hosszúságúnak kell lennie!\n");
        }

        if( !(pswdField.getText().equals(pswdCField.getText())) ) {
            buffer.append("A két begépelt jelszó nem egyezik!");
        }

        if( buffer.toString().length() != 0 ) {
            throw new Exception( buffer.toString() );
        }
    }

    private boolean isValidEmail() {
        // http://www.mkyong.com/regular-expressions/how-to-validate-email-address-with-regular-expression/
        final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(emailField.getText().trim());
        return matcher.matches();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
}
