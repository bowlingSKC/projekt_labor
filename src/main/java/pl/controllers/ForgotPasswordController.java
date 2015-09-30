package pl.controllers;

/**
 * Created by andru on 9/17/2015.
 */

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import pl.Main;
import pl.MessageBox;
import pl.SendMail;
import pl.jpa.SessionUtil;
import pl.model.User;

import java.math.BigInteger;
import java.security.SecureRandom;


public class ForgotPasswordController {

    private Stage dialogStage;

    @FXML
    private TextField mailField;
    @FXML
    private Label stageCloseLabel;

    String recipiantEmail;                              //destination
    String title = "Új jelszó";                //subject
    String message;                                     //message
    private SecureRandom random;
    public String nextSessionId() {
        return new BigInteger(130, random).toString(32);
    }

    @FXML
    public void initialize() {
        stageCloseLabel.setOnMouseClicked((MouseEvent event) -> dialogStage.close());
    }

    @FXML
    private void handleSend() {
        message = "Új jelszavad: ";
        recipiantEmail = mailField.getText();
        if(mailField.getText().length() == 0) {
            System.out.println("�res mez?.");
        }
        random = new SecureRandom();                    //generate random number
        String newpassword = nextSessionId();           //save password to a String
        newpassword = newpassword.subSequence(0,8).toString();
        System.out.println(newpassword);
        message = message + newpassword;

        try {
            String newSalt = Main.getSalt();
            String newPass = Main.getSHA512Hash(newpassword, newSalt);
            changePassword(newPass, newSalt, recipiantEmail);
            System.out.println(message);
            //mailField.setText("Kérlek várj!");
            SendMail.Send(recipiantEmail, title, message);
            mailField.setText("E-mail elküldve!");
        } catch (Throwable ex) {
            ex.printStackTrace();
            MessageBox.showErrorMessage("Hiba", "A jelsz�eml�keztet? elk�ld�se sikertelen volt!",
                    "Pr�b�lja k�s?bb.", false);
        }

    }

    private void changePassword(String newPass, String newSalt, String recipiantEmail) {
        Session session = SessionUtil.getSession();
        Criteria criteria = session.createCriteria(User.class);
        User mailUser = (User) criteria.add(Restrictions.eq("email", recipiantEmail)).uniqueResult();
        System.out.println(mailUser.getLastname() + " " + mailUser.getFirstname());
        mailUser.setSalt(newSalt);
        mailUser.setPassword(newPass);
        org.hibernate.Transaction tx = session.beginTransaction();
        session.update(mailUser);
        tx.commit();
        session.close();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
}
