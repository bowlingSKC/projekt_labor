package pl.controllers;

/**
 * Created by andru on 9/17/2015.
 */

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import pl.model.SendMail;

import javax.mail.MessagingException;


public class NewPasswordController {

    @FXML
    private TextField mailField;

    String username = "projekt.penzugy";                //our email address
    String password = "projektlabor";                   //our password
    String recipiantEmail;                              //destination
    String title = "Password remainder";                //subject
    String message = "message test";                    //message

    @FXML
    private void handleSend() {
        recipiantEmail = mailField.getText();
        if(mailField.getText() == "") {
            System.out.println("Empty");
        }
        SendMail.Send(username, password, recipiantEmail, title, message);


    }


}
