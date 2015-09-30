package pl.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import pl.Main;
import pl.model.Account;
import pl.model.Bank;
import pl.model.Transaction;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

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
    private Label sumLabel;

    @FXML
    public void initialize() {

        // Table init
        accountNoColumn.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        accountNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        accountBankColumn.setCellValueFactory(new PropertyValueFactory<>("bank"));
        accountMoneyColumn.setCellValueFactory(new PropertyValueFactory<>("money"));

        // duplakattra az adatok megjelenítése
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
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    }
                }
            });
            return row;
        });

        accountMoneyColumn.setCellFactory( cell -> new TableCell<Account, Float>() {
            @Override
            protected void updateItem(Float item, boolean empty) {
                super.updateItem(item, empty);

                if( item == null || empty ) {
                    setText(null);
                    setStyle("");
                } else {
                    NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
                    setText(numberFormat.format(item) + " Ft");
                }
            }
        });

        accountTableView.getItems().setAll(Main.getLoggedUser().getAccounts() );

        computeSumMoneyOnAccounts();
    }

    private void computeSumMoneyOnAccounts() {
        float money = 0;
        for(Account account : Main.getLoggedUser().getAccounts()) {
            money += account.getMoney();
        }

        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        sumLabel.setText( numberFormat.format(money) + " Ft"  );
    }

}
