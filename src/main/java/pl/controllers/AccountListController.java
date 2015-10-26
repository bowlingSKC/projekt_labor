package pl.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;

public class AccountListController {

    // ============= TÁBLÁZAT =============
    @FXML
    private AnchorPane tablePane;
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
    private AnchorPane editPane;
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


    // ============= MÓDOSÍTÁS/TÖRLÉS ============

    @FXML
    public void initialize() {
        tablePane.setOpacity(1);

        initAccountListTable();
        accountTableView.getItems().setAll(Main.getLoggedUser().getAccounts());
        computeSumMoneyOnAccounts();
        initNewAccountPanel();
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

        accountNoColumn.setCellFactory(t -> new TableCell<Account, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if( item != null && !empty ) {
                   setText( item.substring(0, 8) + "-" + item.substring(8, 16) + "-" + item.substring(17) );
                } else {
                    setText("");
                }
            }
        });

        // dupla katt, TODO: szerkeszteni valahogy
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
        tablePane.setOpacity(0);
        new FadeInUpTransition(editPane).play();
    }

    @FXML
    private void handleBackToTableView() {
        refershTableItems();
        editPane.setOpacity(0);
        new FadeInUpTransition(tablePane).play();
    }

    @FXML
    private void handleSaveAccount() {
        try {
            checkAllFields();

            Account result = getAccountFromFields();
            checkAccountExistsInDatabase(result);

            saveAccountToDatabase(result);
            clearAllNewAccountField();

            refershTableItems();
            editPane.setOpacity(0);
            new FadeInUpTransition(tablePane).play();
        } catch (Throwable ex) {
            MessageBox.showErrorMessage("Hiba", "Hiba a számla létrehozásakkor", ex.getMessage(), false);
        }
    }

    private void checkAllFields() throws Exception {
        StringBuilder buffer = new StringBuilder();

        if( newAccBankComboBox.getSelectionModel().getSelectedItem() == null ) {
            buffer.append("Bank választása kötelező!\n");
        }

        if( newAccNum1.getText().length() != 8 || newAccNum2.getText().length() != 8 || newAccNum3.getText().length() != 8 ) {
            buffer.append("A számlaszámnak háromszor 8 számból kell állnia!\n");
        }
        // TODO: számokat ellenőrizni, ne legyenek benne betűk

        if( newAcccurrencyComboBox.getSelectionModel().getSelectedItem() == null ) {
            buffer.append("Közelező valutát választani!\n");
        }

        if( buffer.toString().length() != 0 ) {
            throw new Exception( buffer.toString() );
        }
    }

    public Account getAccountFromFields() {
        Account account = new Account();
        account.setOwner(Main.getLoggedUser());
        account.setBank( newAccBankComboBox.getSelectionModel().getSelectedItem() );
        account.setCurrency(newAcccurrencyComboBox.getSelectionModel().getSelectedItem());
        account.setAccountNumber( newAccNum1.getText().trim() + newAccNum2.getText().trim() + newAccNum3.getText().trim() );

        if( newAccMoney.getText().length() == 0 ) {
            account.setMoney(0.0f);
        } else {
            account.setMoney(Float.valueOf(newAccMoney.getText()));
        }

        if( newAccName.getText().length() == 0 ) {
            account.setName("Számlám");
        } else {
            account.setName( newAccName.getText() );
        }

        if( newAccDatePicker.getValue() == null ) {
            account.setCreatedDate( new Date() );
        } else {
            account.setCreatedDate( Constant.dateFromLocalDate(newAccDatePicker.getValue()) );
        }

        return account;
    }

    private void refershTableItems() {
        accountTableView.getItems().clear();
        accountTableView.getItems().addAll(Main.getLoggedUser().getAccounts());

        computeSumMoneyOnAccounts();
    }

    private void saveAccountToDatabase(Account account) throws Exception {
        Session session = SessionUtil.getSession();
        org.hibernate.Transaction tx = session.beginTransaction();
        session.save(account);
        tx.commit();
        session.close();

        Main.getLoggedUser().getAccounts().add(account);
    }

    private void checkAccountExistsInDatabase(Account account) throws Exception {
        Session session = SessionUtil.getSession();
        Query query = session.createQuery("from Account where accountNumber = :number");
        query.setParameter("number", account.getAccountNumber());
        Account db = (Account) query.uniqueResult();
        session.close();

        if( db != null ) {
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

    public void handleToCSV(){
        FileWriter writer = null;
        try {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter =
                    new FileChooser.ExtensionFilter("CSV file (*.csv)", "*.csv");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showSaveDialog(Main.getPrimaryStage());
            if(file != null){

                writer = new FileWriter(file);

                for(int i = 0; i < accountTableView.getItems().size(); i++){
                    writer.append(accountTableView.getItems().get(i).getName());
                    writer.append(';');
                    writer.append(accountTableView.getItems().get(i).getAccountNumber());
                    writer.append(';');
                    writer.append(String.valueOf(accountTableView.getItems().get(i).getMoney()));
                    writer.append(';');
                    writer.append(accountTableView.getItems().get(i).getCurrency().toString());
                    writer.append(';');
                    writer.append(accountTableView.getItems().get(i).getBank().toString());
                    writer.append(';');
                    writer.append(accountTableView.getItems().get(i).getCreatedDate().toString());
                    writer.append('\n');
                    writer.flush();
                }
                writer.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
