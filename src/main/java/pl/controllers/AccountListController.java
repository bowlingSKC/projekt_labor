package pl.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import pl.Main;
import pl.model.Account;
import pl.model.Bank;
import pl.model.Transaction;

import java.io.IOException;

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
    public void initialize() {

        // Table init
        accountNoColumn.setCellValueFactory(new PropertyValueFactory<Account, String>("accountNumber"));
        accountNameColumn.setCellValueFactory(new PropertyValueFactory<Account, String>("name"));
        accountBankColumn.setCellValueFactory(new PropertyValueFactory<Account, Bank>("bank"));
        accountMoneyColumn.setCellValueFactory(new PropertyValueFactory<Account, Float>("money"));

        accountTableView.setRowFactory(tv -> {
            TableRow<Account> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if( event.getClickCount() == 2 && (!row.isEmpty()) ) {
                    try {
                        Stage dialogStage = new Stage();

                        FXMLLoader loader = new FXMLLoader(Main.class.getResource("../layout/ShowAccount.fxml"));
                        BorderPane pane = loader.load();

                        Scene scene = new Scene(pane);
                        dialogStage.setScene(scene);

                        ShowAccountController showAccountController = loader.getController();
                        showAccountController.setAccount(row.getItem());

                        dialogStage.show();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            return row;
        });

        accountTableView.getItems().setAll(Main.getLoggedUser().getAccounts() );
    }

}
