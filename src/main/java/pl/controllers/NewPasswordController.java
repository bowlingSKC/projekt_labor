package pl.controllers;

/**
 * Created by andru on 9/17/2015.
 */

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import pl.MessageBox;


public class NewPasswordController {

    @FXML
    private TextField mailField;

    @FXML
    private void handleSend() {
        if(mailField.getText() == "") {
            System.out.println("Empty.");
        }
    }


}
