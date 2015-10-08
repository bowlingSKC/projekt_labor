package pl.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.hibernate.Query;
import org.hibernate.Session;
import pl.Constant;
import pl.Main;
import pl.jpa.SessionUtil;
import pl.model.Account;
import pl.model.Transaction;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

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
    public void initialize() {
        computeSumOfMoney();
        computePropertyCash();
        computeReadyCash();
        computeAccountsLabel();
    }

    private void computeSumOfMoney() {
        sumOfMoneyLabel.setText( Constant.getNumberFormat().format(Main.getLoggedUser().getAllMoney()) );
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

}
