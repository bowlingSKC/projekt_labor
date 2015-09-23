package pl.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import pl.model.Account;

import java.text.SimpleDateFormat;

public class ShowAccountController {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy. MM. dd.");

    @FXML
    private Label accNoLabel;
    @FXML
    private Label bankLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private Label regDateLabel;
    @FXML
    private Label sumLabel;


    public void setAccount(Account account) {
        accNoLabel.setText( account.getAccountNumber() );
        bankLabel.setText( account.getBank().getName() );
        nameLabel.setText( account.getName() );
        regDateLabel.setText( sdf.format(account.getCreatedDate()) );
        sumLabel.setText( Float.toString(account.getMoney()) );
    }

}
