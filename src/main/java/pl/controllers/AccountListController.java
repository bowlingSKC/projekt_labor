package pl.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.hibernate.Query;
import org.hibernate.Session;
import pl.Constant;
import pl.Main;
import pl.MessageBox;
import pl.animations.FadeInUpTransition;
import pl.bundles.Bundles;
import pl.jpa.SessionUtil;
import pl.model.Account;
import pl.model.Bank;
import pl.model.Currency;
import pl.model.Transaction;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class AccountListController {

    private final SearchListener searchListener = new SearchListener();

    // ============= TÁBLÁZAT =============
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
    private TableColumn<Account, String> valutaColumn;
    @FXML
    private Label sumLabel;
    // ============= TÁBLÁZAT VÉGE =============

    // ============= ÚJ SZÁMLA LÉTREHOZÁSA =============
    @FXML
    private ComboBox<Bank> newAccBankComboBox;
    @FXML
    private TextField newAccNum1;
    @FXML
    private TextField newAccNum2;
    @FXML
    private TextField newAccNum3;
    @FXML
    private TextField newAccName;
    @FXML
    private TextField newAccMoney;
    @FXML
    private ComboBox<Currency> newAcccurrencyComboBox;
    @FXML
    private DatePicker newAccDatePicker;
    // ============= ÚJ SZÁMLA LÉTREHOZÁSA VÉGE =============

    // ============= KERESÉS =============
    @FXML
    private TextField searchAccName;
    @FXML
    private TextField searchAccNum;
    @FXML
    private ComboBox<Bank> searchComboBox;
    // ============= KERESÉS VÉGE =============

    // ============= MÓDOSÍTÁS/TÖRLÉS ============

    @FXML
    public void initialize() {
        initAccountListTable();
        accountTableView.getItems().setAll(Main.getLoggedUser().getAccounts() );
        computeSumMoneyOnAccounts();
        initNewAccountPanel();
        initSearchPanel();
    }

    private void initSearchPanel() {
        Set<Bank> myBanks = new HashSet<>(0);
        myBanks.addAll(Main.getLoggedUser().getAccounts().stream().map(Account::getBank).collect(Collectors.toList()));
        searchComboBox.getItems().add(new Bank(Bundles.getString("cash.bankaccount.search.bank.all")));
        searchComboBox.getItems().addAll(myBanks);
        searchComboBox.getSelectionModel().select(0);

        // listeners
        searchAccName.textProperty().addListener(searchListener);
        searchAccNum.textProperty().addListener(searchListener);
        searchComboBox.getSelectionModel().selectedIndexProperty().addListener(searchListener);
    }

    private void initNewAccountPanel() {
        newAccBankComboBox.getItems().addAll(Constant.getBanks());
        newAcccurrencyComboBox.getItems().addAll(Constant.getCurrencies());

        for(Currency currency : Constant.getCurrencies()) {
            if( currency.getCode().equals("HUF") ) {
                newAcccurrencyComboBox.getSelectionModel().select(currency);
                return;
            }
        }
    }

    private void initAccountListTable() {
        accountNoColumn.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        accountNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        accountBankColumn.setCellValueFactory(new PropertyValueFactory<>("bank"));
        accountMoneyColumn.setCellValueFactory(new PropertyValueFactory<>("money"));
        valutaColumn.setCellValueFactory(new PropertyValueFactory<>("currency"));

        // dupla katt
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
                    setText(numberFormat.format(item));
                }
            }
        });
    }

    private void computeSumMoneyOnAccounts() {
        float money = 0;
        for(Account account : Main.getLoggedUser().getAccounts()) {
            money += account.getMoney();
        }

        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        sumLabel.setText( numberFormat.format(money) + " Ft"  );
    }

    @FXML
    private void handleNewAccount() {
        try {
            saveAccountToDatabase(newAccNum1.getText() + newAccNum2.getText() + newAccNum3.getText());
            refershTableItems();
            clearAllNewAccountField();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void refershTableItems() {
        accountTableView.getItems().clear();
        accountTableView.getItems().addAll(Main.getLoggedUser().getAccounts());

        searchComboBox.getItems().clear();
        initSearchPanel();
        computeSumMoneyOnAccounts();
    }

    private void saveAccountToDatabase(String newAccNo) throws Exception {
        Date date = Date.from(newAccDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
        Account account = new Account(newAccNo, newAccName.getText().trim(), Float.valueOf(newAccMoney.getText()),
                date, Main.getLoggedUser(), newAccBankComboBox.getSelectionModel().getSelectedItem(), newAcccurrencyComboBox.getSelectionModel().getSelectedItem());
        Session session = SessionUtil.getSession();
        org.hibernate.Transaction tx = session.beginTransaction();
        session.save(account);
        tx.commit();
        session.close();

        Main.getLoggedUser().getAccounts().add(account);
    }

    private void checkAccountExistsInDatabase(String newAccNo) throws Exception {
        Session session = SessionUtil.getSession();
        Query query = session.createQuery("from Account where accountNumber = :acc");
        query.setParameter("acc", newAccNo);
        Account account = (Account) query.uniqueResult();
        session.close();

        if( account != null ) {
            throw new Exception(Bundles.getString("error.newaccount.exists") );
        }
    }

    private boolean checkGiro() {
        String typeGiro = newAccNum1.getText().substring(0, 3);
        Bank selectedBank = newAccBankComboBox.getSelectionModel().getSelectedItem();
        if( !selectedBank.getGiro().equals(typeGiro) ) {
            if( getBankByGiro(typeGiro) != null ) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle( Bundles.getString("error.warning") );
                alert.setHeaderText("A kiválasztott bank és a számlaszám nem összeegyeztethető!\nBiztosan így akarja elmenteni?");
                alert.setContentText("A rendszer ehhez a számlaszámhoz a " + getBankByGiro(typeGiro).getName() + " bankot ajánlja.");

                Optional<ButtonType> result = alert.showAndWait();
                if( result.get() == ButtonType.OK ) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    private void clearAllNewAccountField() {
        newAccBankComboBox.getSelectionModel().select(null);
        newAccNum1.setText("");
        newAccNum2.setText("");
        newAccNum3.setText("");
        newAccName.setText("");
        newAccMoney.setText("");
        newAccDatePicker.setValue(null);
    }

    private Bank getBankByGiro(String giro) {
        for( Bank bank : Constant.getBanks() ) {
            if( bank.getGiro().equals(giro) ) {
                return bank;
            }
        }
        return null;
    }

    private void checkFields() throws Exception {
        StringBuffer buffer = new StringBuffer();

        if( newAccBankComboBox.getSelectionModel().getSelectedItem() == null ) {
            buffer.append("Bank kiválasztása kötelező!\n");
        }

        if( newAccNum1.getText().equals("") || newAccNum2.getText().equals("") || newAccNum3.getText().equals("") ) {
            buffer.append("A számlaszám mindegyik mezőjének kitöltése kötelező!\n");
        } else {
            if( newAccNum1.getText().length() != 8 || newAccNum2.getText().length() != 8 || newAccNum3.getText().length() != 8 ) {
                buffer.append("A számlaszám mindegyik mezőjének 8 db számból kell állnia!\n");
            }
        }

        try {
            float money = Float.valueOf( newAccMoney.getText().trim() );
            if( money < 0 ) {
                buffer.append("Az összeg 0-nál nagyobb számnak kell lennie!\n");
            }
        } catch (Exception e) {
            buffer.append("Összegnek csak számot lehet beírni!\n");
        }

        if( buffer.toString().length() != 0 ) {
            throw new Exception(buffer.toString());
        }
    }

    private class SearchListener implements ChangeListener {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {

            Set<Account> accounts = new HashSet<>(Main.getLoggedUser().getAccounts());

            if(searchAccName.getText().length() > 0) {
                Iterator<Account> iter = accounts.iterator();
                while(iter.hasNext()) {
                    Account acc = iter.next();
                    if (!acc.getName().toUpperCase().contains(searchAccName.getText().trim().toUpperCase())) {
                        iter.remove();
                    }
                }
            }

            if( searchAccNum.getText().length() > 0 ) {
                Iterator<Account> iter = accounts.iterator();
                while(iter.hasNext()) {
                    Account acc = iter.next();
                    if (!acc.getAccountNumber().contains(searchAccNum.getText().trim())) {
                        iter.remove();
                    }
                }
            }

            if( searchComboBox.getSelectionModel().getSelectedItem() != null ) {
                Bank selectedBank = searchComboBox.getSelectionModel().getSelectedItem();
                if( !selectedBank.getName().equals(Bundles.getString("cash.bankaccount.search.bank.all")) ) {
                    Iterator<Account> iter = accounts.iterator();
                    while(iter.hasNext()) {
                        Account account = iter.next();
                        if (!account.getBank().equals(selectedBank)) {
                            iter.remove();
                        }
                    }
                }
            }

            accountTableView.getItems().setAll(accounts);
        }
    }

}
