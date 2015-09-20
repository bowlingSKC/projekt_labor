package pl.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import pl.Main;
import pl.model.Account;
import pl.model.Bank;

public class AccountListController {

    @FXML
    private TableView<Account> accountTableView;
    @FXML
    private TableColumn<Account, String> accountNoColumn;
    @FXML
    private TableColumn<Account, Bank> accountBankColumn;
    @FXML
    private TableColumn<Account, Float> accountMoneyColumn;
    @FXML
    private TableColumn<Account, String> accountNameColumn;

    @FXML
    private void handleAddNewAccount() {

    }

    @FXML
    public void initialize() {

        // Table init
        accountNoColumn.setCellValueFactory(new PropertyValueFactory<Account, String>("accountNumber"));
        accountNameColumn.setCellValueFactory(new PropertyValueFactory<Account, String>("name"));
        accountBankColumn.setCellValueFactory(new PropertyValueFactory<Account, Bank>("bank"));
        accountMoneyColumn.setCellValueFactory(new PropertyValueFactory<Account, Float>("money"));

        accountTableView.getItems().setAll(Main.getLoggedUser().getAccounts() );
    }

}
