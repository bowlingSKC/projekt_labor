package pl.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import pl.Constant;
import pl.Main;

public class PersonalSummaryController {

    @FXML
    private Label sumOfMoneyLabel;
    @FXML
    private Label inReadyCashLabel;
    @FXML
    private Label inAccountsLabel;
    @FXML
    private Label inPropertiesLabel;
    @FXML
    private Label debitLabel;

    @FXML
    public void initialize() {
        computeSumOfMoney();
        computePropertyCash();
        computeReadyCash();
        computeAccountsLabel();
        computeDepit();
    }

    private void computeSumOfMoney() {
        sumOfMoneyLabel.setText( Constant.getNumberFormat().format(Main.getLoggedUser().getAllMoney() - Main.getLoggedUser().getAllDebitsInValue()) );
    }

    private void computeAccountsLabel() {
        inAccountsLabel.setText( Constant.getNumberFormat().format(Main.getLoggedUser().getAllMoneyFromAccounts()) );
    }

    private void computeReadyCash() {
        inReadyCashLabel.setText(Constant.getNumberFormat().format(Main.getLoggedUser().getAllMoneyInReadyCash()));
    }

    private void computePropertyCash() {
        inPropertiesLabel.setText(Constant.getNumberFormat().format(Main.getLoggedUser().getAllMoneyInProperties()));
    }

    private void computeDepit() {
        debitLabel.setText( Constant.getNumberFormat().format(Main.getLoggedUser().getAllDebitsInValue()) );
    }

}
