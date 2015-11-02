package pl.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.hibernate.Session;
import pl.Constant;
import pl.Main;
import pl.jpa.SessionUtil;
import pl.model.Account;
import pl.model.Currency;
import pl.model.AccountTransaction;
import pl.model.TransactionType;

import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionController {

    private AccountTransaction accountTransaction = new AccountTransaction();

    @FXML
    private ComboBox<String> accountTypeComboBox;
    @FXML
    private ComboBox<Account> accountComboBox;
    @FXML
    private ComboBox<TransactionType> transactionTypeComboBox;
    @FXML
    private TextField amountField;
    @FXML
    private ComboBox<Currency> currencyComboBox;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField commentField;
    @FXML
    private GridPane readyToAccountPane;
    @FXML
    private GridPane fromOrToReadyCash;
    @FXML
    private GridPane accountToAccount;



    @FXML
    public void initialize() {
        hideAllExtraFields();

        accountTypeComboBox.getItems().add("Készpénz");
        accountTypeComboBox.getItems().add("Bankkártya");
        accountTypeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if( newValue.equals("Bankkártya") ) {
                accountComboBox.setVisible(true);
            } else {
                accountComboBox.setVisible(false);
            }
        });

        List<Account> accountList = Main.getLoggedUser().getAccounts().stream().collect(Collectors.toCollection(LinkedList::new));
        accountComboBox.getItems().setAll(accountList);
        transactionTypeComboBox.getItems().setAll(Constant.getTransactionTypes());
        currencyComboBox.getItems().setAll(Constant.getCurrencies());
    }

    @FXML
    private void handleSave() {

        if( !accountComboBox.getSelectionModel().getSelectedItem().getName().equals("Készpénz") ) {
            accountTransaction.setAccount(accountComboBox.getSelectionModel().getSelectedItem());
            accountTransaction.setType(transactionTypeComboBox.getSelectionModel().getSelectedItem());
            accountTransaction.setMoney( Float.valueOf(amountField.getText()) );
            accountTransaction.setDate( Date.from(datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()) );
            accountTransaction.setComment( commentField.getText() );
        }

        Session session = SessionUtil.getSession();
        org.hibernate.Transaction tx = session.beginTransaction();
        session.saveOrUpdate(accountTransaction);
        tx.commit();
        session.close();

    }

    private void hideAllExtraFields() {
//        readyToAccountPane.setVisible(false);
//        fromOrToReadyCash.setVisible(false);
//        accountToAccount.setVisible(false);
    }

    public void setAccountTransaction(AccountTransaction accountTransaction) {
        this.accountTransaction = accountTransaction;

        accountComboBox.getSelectionModel().select(accountTransaction.getAccount());
        transactionTypeComboBox.getSelectionModel().select(accountTransaction.getType());
        amountField.setText( String.valueOf(accountTransaction.getMoney()) );

        commentField.setText( accountTransaction.getComment() );
    }

}
