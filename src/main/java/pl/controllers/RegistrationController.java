package pl.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import pl.Constant;
import pl.Main;
import pl.MessageBox;
import pl.SendMail;
import pl.bundles.Bundles;
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
    @FXML
    private Label closeStageLabel;

    private Stage dialogStage;

    @FXML
    public void initialize() {
        closeStageLabel.setOnMouseClicked((MouseEvent event) -> dialogStage.close());
    }

    @FXML
    private void handleRegistration() {
        try {
            checkFields();

            SendMail.Send(emailField.getText(), Bundles.getString("registration.label"), Bundles.getString("registration.label") + "\n" +  Bundles.getString("password") + pswdField.getText());

            User newUser = creteUserFromFields();
            Session session = SessionUtil.getSession();
            Transaction tx = session.beginTransaction();
            session.save(newUser);
            tx.commit();
            session.close();

            dialogStage.close();
            MessageBox.showInformationMessage(Bundles.getString("registration.label"), Bundles.getString("registration.label"), Bundles.getString("canlogin"), false);
        } catch (Exception ex) {
            MessageBox.showErrorMessage(Bundles.getString("error.nodb.title"), Bundles.getString("allfieldcorrectly"), ex.getMessage(), false);
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

            if(Bundles.getDefaultLanguage().toLowerCase().equals("hu")) {
                newUser.setLanguage("hu");
            } else {
                newUser.setLanguage("en");
            }

            return newUser;
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private void checkFields() throws Exception {
        StringBuffer buffer = new StringBuffer();

        if (firstnameField.getText().length() == 0) {
            buffer.append(Bundles.getString("missing input") + Bundles.getString("lastname") + "\n");
        }

        if (lastnameField.getText().length() == 0) {
            buffer.append(Bundles.getString("missing input") + Bundles.getString("firstname") + "\n");
        }

        if (emailField.getText().length() == 0) {
            buffer.append(Bundles.getString("missing input") +  "e-mail\n");
        }

        if (!Constant.isValidEmail(emailField.getText().trim())) {
            buffer.append(Bundles.getString("emailwrongformat") +"\n");
        }

        if (existEmail()) {
            buffer.append(Bundles.getString("emailcollosion") + "\n");
        }

        if (pswdField.getText().length() == 0) {
            buffer.append(Bundles.getString("missing input") + Bundles.getString("passwordC") + "\n");
        }

        if (pswdCField.getText().length() == 0) {
            buffer.append(Bundles.getString("missing input") + Bundles.getString("passwordC") + "\n");
        }

        if (pswdField.getText().length() < 8 || pswdField.getText().length() >= 20) {
            buffer.append(Bundles.getString("passwordlen") + "\n");
        }

        if (!(pswdField.getText().equals(pswdCField.getText()))) {
            buffer.append(Bundles.getString("passwordmismatch"));
        }

        if (buffer.toString().length() != 0) {
            throw new Exception(buffer.toString());
        }
    }

    private boolean existEmail() {
        Session session = SessionUtil.getSession();
        Criteria criteria = session.createCriteria(User.class);
        User mailUser = (User) criteria.add(Restrictions.eq("email", emailField.getText())).uniqueResult();
        if (mailUser == null) {
            return false;
        }
        return true;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
}
