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
import javafx.util.Callback;
import org.hibernate.Query;
import org.hibernate.Session;
import pl.Constant;
import pl.CurrencyExchange;
import pl.Main;
import pl.MessageBox;
import pl.animations.FadeInUpTransition;
import pl.bundles.Bundles;
import pl.jpa.SessionUtil;
import pl.model.Account;
import pl.model.AccountTransaction;
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
    private TableColumn inHufColumn;
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

    private Map<String,Float> hufvalues = new HashMap<>();

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

        inHufColumn.setCellFactory(new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableCell() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);

                        try {
                            if( this.getTableRow() != null ) {
                                Account account = accountTableView.getItems().get(this.getTableRow().getIndex());
                                if( account.getCurrency().getCode().equals("HUF") ) {
                                    setText( Constant.getNumberFormat().format(account.getMoney()) );
                                    hufvalues.put(account.getAccountNumber(), account.getMoney());
                                } else if(CurrencyExchange.isContainsKey(account.getCurrency())) {
                                    setText( Constant.getNumberFormat().format( CurrencyExchange.getValue(account.getCurrency()) * account.getMoney() ) );
                                    hufvalues.put(account.getAccountNumber(), CurrencyExchange.getValue(account.getCurrency()) * account.getMoney());
                                } else {
                                    setText("???");
                                }
                            }
                        } catch (Exception ex) {
                            // nem kell kezelni, csak nem éri el a listából, JavaFX hibája
                        }
                    }
                };
            }
        });

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
            if( account.getCurrency().equals(Constant.getHufCurrency()) ) {
                money += account.getMoney();
            } else {
                if( CurrencyExchange.isContainsKey( account.getCurrency() ) ) {
                    money += CurrencyExchange.getValue(account.getCurrency()) * account.getMoney();
                }
            }
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
        computeSumMoneyOnAccounts();
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
        AccountTransaction transaction = new AccountTransaction();
        transaction.setMoney(account.getMoney());
        transaction.setDate(account.getCreatedDate());
        transaction.setCurrency(account.getCurrency());
        transaction.setAccount(account);
        transaction.setType(Constant.getAccountInType());
        account.getAccountTransactions().add(transaction);

        Session session = SessionUtil.getSession();
        org.hibernate.Transaction tx = session.beginTransaction();
        session.save(transaction);
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
            FileChooser.ExtensionFilter extFilter1 =
                    new FileChooser.ExtensionFilter("CSV file (*.csv)", "*.csv");
            fileChooser.getExtensionFilters().add(extFilter1);
            FileChooser.ExtensionFilter extFilter2 = new FileChooser.ExtensionFilter("Excel file (*.xls)", "*.xls");
            fileChooser.getExtensionFilters().add(extFilter2);
            File file = fileChooser.showSaveDialog(Main.getPrimaryStage());
            if(file != null && fileChooser.getSelectedExtensionFilter() == extFilter1){
                writer = new FileWriter(file);
                writer.append("Name;Account number;Money;Currency;Bank;Created date;HUF value\n");
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
                    writer.append(';');
                    String tmp = accountTableView.getColumns().get(0).getCellData(i).toString();
                    writer.append(String.valueOf(hufvalues.get(tmp)));
                    writer.append('\n');
                    writer.flush();
                }
                writer.append('\n');
                writer.append('\n');
                writer.append("Total;");
                writer.append(sumLabel.getText());
                writer.flush();
                writer.close();
            }
            if(file != null && fileChooser.getSelectedExtensionFilter() == extFilter2){
                writer = new FileWriter(file);
                writer.append("Name\tAccount number\tMoney\tCurrency\tBank\tCreated date\tHUF value\n");
                for(int i = 0; i < accountTableView.getItems().size(); i++){
                    writer.append(accountTableView.getItems().get(i).getName());
                    writer.append('\t');
                    writer.append(accountTableView.getItems().get(i).getAccountNumber());
                    writer.append('\t');
                    writer.append(String.valueOf(accountTableView.getItems().get(i).getMoney()));
                    writer.append('\t');
                    writer.append(accountTableView.getItems().get(i).getCurrency().toString());
                    writer.append('\t');
                    writer.append(accountTableView.getItems().get(i).getBank().toString());
                    writer.append('\t');
                    writer.append(accountTableView.getItems().get(i).getCreatedDate().toString());
                    writer.append('\t');
                    String tmp = accountTableView.getColumns().get(0).getCellData(i).toString();
                    writer.append(String.valueOf(hufvalues.get(tmp)));
                    writer.append('\n');
                    writer.flush();
                }
                writer.append('\n');
                writer.append("Total\t");
                writer.append(sumLabel.getText());
                writer.flush();
                writer.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
