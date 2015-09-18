package pl.controllers;

/**
 * Created by andru on 9/17/2015.
 */

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import pl.model.SendMail;

import javax.mail.MessagingException;
import java.math.BigInteger;
import java.security.SecureRandom;


public class NewPasswordController {

    @FXML
    private TextField mailField;

    String username = "projekt.penzugy";                //our email address
    String password = "projektlabor";                   //our password
    String recipiantEmail;                              //destination
    String title = "Password remainder";                //subject
    String message = "Your new password: ";                    //message

    private SecureRandom random;
    public String nextSessionId() {
        return new BigInteger(130, random).toString(32);
    }

    @FXML
    private void handleSend() {
        recipiantEmail = mailField.getText();

        random = new SecureRandom();                    //generate random number
        String newpassword = nextSessionId();           //save password to a String
        System.out.println(newpassword);
        message = message + newpassword;

        SendMail.Send(username, password, recipiantEmail, title, message);


    }


}
