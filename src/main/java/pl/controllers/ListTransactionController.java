package pl.controllers;


import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.hibernate.*;
import pl.Constant;
import pl.Main;
import pl.MessageBox;
import pl.animations.FadeInUpTransition;
import pl.bundles.Bundles;
import pl.jpa.SessionUtil;
import pl.model.*;
import pl.model.AccountTransaction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class ListTransactionController {

    private final SearchListener listener = new SearchListener();
    private final List<AccountTransaction> allAccountTransactions = getAllAccountTransactions();

    @FXML
    private TableView<AccountTransaction> transactionTableView;
    @FXML
    private TableColumn crudColumn;
    @FXML
    private TableColumn<AccountTransaction, Account> accountTableColumn;
    @FXML
    private TableColumn<AccountTransaction, Float> moneyTableColumn;
    @FXML
    private TableColumn<AccountTransaction, Float> beforeMoneyColumn;
    @FXML
    private TableColumn<AccountTransaction, Date> dateTableColumn;
    @FXML
    private TableColumn<AccountTransaction, TransactionType> transactionTypeColumn;
    @FXML
    private TextField searcMoneyFromField;
    @FXML
    private TextField searcMoneyToField;
    @FXML
    private DatePicker searchFromDate;
    @FXML
    private DatePicker searchToDate;
    @FXML
    private TextField searchCommentField;
    @FXML
    private ScrollPane scrollPane;

    // ========= TYPE PANE =========
    @FXML
    private AnchorPane tablePane;
    @FXML
    private AnchorPane editPane;
    @FXML
    private AnchorPane transferPane;
    // ========= TYPE PANE VÉGE =========

    // ========= EDIT PANE =========
    @FXML
    private ComboBox<Account> newAccountComboBox;
    @FXML
    private ComboBox<TransactionType> newTransactionTypeComboBox;
    @FXML
    private TextField newTransactionAmountTextField;
    @FXML
    private ComboBox<pl.model.Currency> currencyComboBox;
    @FXML
    private DatePicker newTransactionDatePicker;
    @FXML
    private TextField newTransactionCommentField;

    @FXML
    private ComboBox<Pocket> newTransactionFromPocket;
    @FXML
    private Label amointInPocketsLabel;

    @FXML
    private TextField anotherAccNum1;
    @FXML
    private TextField anotherAccNum2;
    @FXML
    private TextField anotherAccNum3;
    // ========= EDIT PANE VÉGE =========

    private AccountTransaction editAccountTransaction = null;

    @FXML
    public void initialize() {

        tablePane.setOpacity(0);
        new FadeInUpTransition(tablePane).play();

        // TableView
        accountTableColumn.setCellValueFactory(new PropertyValueFactory<>("account"));
        moneyTableColumn.setCellValueFactory(new PropertyValueFactory<>("money"));
        // TODO: itt volt a beforeMoney
        dateTableColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        transactionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        transactionTableView.setRowFactory(tableView -> new TableRow<AccountTransaction>() {
            @Override
            protected void updateItem(AccountTransaction item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    if (item.getType().getSign().equals("-")) {
                        setStyle("-fx-background-color: lightcoral;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        moneyTableColumn.setCellFactory(col -> new TableCell<AccountTransaction, Float>() {
            @Override
            protected void updateItem(Float item, boolean empty) {
                super.updateItem(item, empty);

                if (item != null && !empty) {
                    setText(Constant.getNumberFormat().format(item));
                } else {
                    setText("");
                }
            }
        });

        beforeMoneyColumn.setCellFactory(new Callback<TableColumn<AccountTransaction, Float>, TableCell<AccountTransaction, Float>>() {
            @Override
            public TableCell<AccountTransaction, Float> call(TableColumn<AccountTransaction, Float> param) {
                return new TableCell<AccountTransaction, Float>() {
                    @Override
                    protected void updateItem(Float item, boolean empty) {
                        super.updateItem(item, empty);
                        if( item != null && !empty ) {
                            setText(Constant.getNumberFormat().format(item));
                        } else {
                            setText("");
                        }
                    }
                };
            }
        });
        beforeMoneyColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));

        for( Account account : Main.getLoggedUser().getAccounts() ) {
            account.tickAllTransactions();
        }

        dateTableColumn.setCellFactory(column -> new TableCell<AccountTransaction, Date>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);

                if( item == null || empty ) {
                    setText(null);
                    setStyle("");
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
                    setText(sdf.format(item));
                }

            }
        });

        crudColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures, ObservableValue>() {
            @Override
            public ObservableValue call(TableColumn.CellDataFeatures param) {
                return new SimpleBooleanProperty(param.getValue() != null);
            }
        });

        crudColumn.setCellFactory(param -> new ButtonCell(transactionTableView));

        // keres?hez sz?mlasz?mok kiv?laszt?sa
        VBox vbox = new VBox();
        for( Account acc : Main.getLoggedUser().getAccounts() ) {
            CheckBox checkBox = new CheckBox( acc.toString() );
            checkBox.setSelected(true);
            checkBox.selectedProperty().addListener(listener);
            vbox.getChildren().add( checkBox );
        }
        scrollPane.setContent(vbox);

        loadTransactionsToTable();

        searchFromDate.valueProperty().addListener(listener);
        searchToDate.valueProperty().addListener(listener);
        searchCommentField.textProperty().addListener(listener);
        searcMoneyFromField.textProperty().addListener(listener);
        searcMoneyToField.textProperty().addListener(listener);

        dateTableColumn.setSortType(TableColumn.SortType.DESCENDING);

        initEditTransaction();
    }

    private void initEditTransaction() {
        newAccountComboBox.getItems().setAll(Main.getLoggedUser().getAccounts());
        newTransactionTypeComboBox.getItems().setAll(Constant.getTransactionTypes());
        currencyComboBox.getItems().setAll(Constant.getCurrencies());

        newAccountComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> newTransactionFromPocket.getItems().setAll(newValue.getPockets()));
        newTransactionFromPocket.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> amointInPocketsLabel.setText( newValue.getMoney() + " " + newAccountComboBox.getSelectionModel().getSelectedItem().getCurrency()));
    }

    private void hideAllTypePane() {
        transferPane.setOpacity(0);
    }

    @FXML
    private void handleCancel() {
        editAccountTransaction = null;
        loadTransactionsToTable();
        editPane.setOpacity(0);
        new FadeInUpTransition(tablePane).play();
    }

    @FXML
    private void handleNewTransaction() {
        tablePane.setOpacity(0);
        new FadeInUpTransition(editPane).play();
    }

    @FXML
    private void handleSaveTransaction() {
        if( editAccountTransaction == null ) {
            saveNewTransaction();
        } else {
            try {
                checkAllFields();
                checkValidTransaction();

                fillTransactionFromFields(editAccountTransaction);
                fillEmptyFields(editAccountTransaction);

                updateTransactionToDatabase(editAccountTransaction);

                loadTransactionsToTable();
                clearAllFieldsOnEditPane();
                editPane.setOpacity(0);
                new FadeInUpTransition(tablePane).play();
            } catch (Throwable ex) {
                MessageBox.showErrorMessage("Hiba", "A tranzakciót nem lehet végrehajtani!", ex.getMessage(), false);
            }
        }
        editAccountTransaction = null;
    }

    private void fillPrevTransaction(AccountTransaction editAccountTransaction) {
        AccountTransaction prev = newAccountComboBox.getSelectionModel().getSelectedItem().getLatestTransaction();
        if( prev != null ) {
            editAccountTransaction.setBeforeAccountTransaction(prev);
        }
    }

    private void updateTransactionToDatabase(AccountTransaction editAccountTransaction) {
        Session session = SessionUtil.getSession();
        org.hibernate.Transaction tx = session.beginTransaction();
        session.update(editAccountTransaction);
        tx.commit();
        session.close();
    }

    private void saveNewTransaction() {
        try {
            checkAllFields();
            checkValidTransaction();

            AccountTransaction accountTransaction = new AccountTransaction();
            fillTransactionFromFields(accountTransaction);
            fillEmptyFields(accountTransaction);
            fillPrevTransaction(accountTransaction);

            saveTransactionToDatabase(accountTransaction);

            loadTransactionsToTable();
            clearAllFieldsOnEditPane();
            editPane.setOpacity(0);
            new FadeInUpTransition(tablePane).play();
        } catch (Throwable ex) {
            MessageBox.showErrorMessage("Hiba", "A tranzakciót nem lehet végrehajtani!", ex.getMessage(), false);
        }
    }

    private void saveTransactionToDatabase(AccountTransaction accountTransaction) {
        Account account = accountTransaction.getAccount();
        Session session = SessionUtil.getSession();
        org.hibernate.Transaction tx = session.beginTransaction();

        if( newTransactionTypeComboBox.getSelectionModel().getSelectedItem().getId() == 1 ) {
            saveOutTransfer(accountTransaction, account, session);
        }

        if( newTransactionTypeComboBox.getSelectionModel().getSelectedItem().getId() == 2) {
            saveCashFromAccount(accountTransaction, account, session);
        }

        if( newTransactionTypeComboBox.getSelectionModel().getSelectedItem().getId() == 3 ) {
            saveAccountToCash(accountTransaction, account, session);
        }

        session.save(accountTransaction);

        tx.commit();
        session.close();

        account.getAccountTransactions().add(accountTransaction);
    }

    private void saveAccountToCash(AccountTransaction accountTransaction, Account account, Session session) {
        ReadyCash readyCash = null;
        for(ReadyCash tmp : Main.getLoggedUser().getReadycash()) {
            if( tmp.getCurrency().equals(currencyComboBox.getSelectionModel().getSelectedItem()) ) {
                readyCash = tmp;
                break;
            }
        }

        if( readyCash != null ) {
            readyCash.setMoney( readyCash.getMoney() - accountTransaction.getMoney() );
            account.setMoney( account.getMoney() + accountTransaction.getMoney() );

            CashTransaction cashTransaction = new CashTransaction();
            cashTransaction.setCash(readyCash);
            cashTransaction.setMoney(accountTransaction.getMoney());
            cashTransaction.setComment(accountTransaction.getComment());
            cashTransaction.setCurrency(accountTransaction.getCurrency());
            cashTransaction.setType(accountTransaction.getType());
            cashTransaction.setDate(accountTransaction.getDate());
            cashTransaction.setBeforeTransaction(readyCash.getLatestTransaction());
            readyCash.getCashTransaction().add(cashTransaction);

            session.save(cashTransaction);

            session.update(readyCash);
            session.update(account);
        } else {
            MessageBox.showErrorMessage("Hiba", "A tranzakciót nem lehet végrehajtani!", "Nincs készpénzben ilyen valuta regisztrálva!", false);
            session.close();
        }
    }

    private void saveCashFromAccount(AccountTransaction accountTransaction, Account account, Session session) {
        ReadyCash readyCash = null;
        for(ReadyCash tmp : Main.getLoggedUser().getReadycash()) {
            if( currencyComboBox.getSelectionModel().getSelectedItem().equals(tmp.getCurrency()) ) {
                readyCash = tmp;
                break;
            }
        }

        if( readyCash == null ) {
            readyCash = new ReadyCash();
            readyCash.setCurrency(currencyComboBox.getSelectionModel().getSelectedItem());
            readyCash.setMoney(accountTransaction.getMoney());
            readyCash.setOwner(Main.getLoggedUser());
            session.save(readyCash);

            Main.getLoggedUser().getReadycash().add(readyCash);
        } else {
            readyCash.setMoney( readyCash.getMoney() + accountTransaction.getMoney() );
            session.update(readyCash);
        }

        CashTransaction cashTransaction = new CashTransaction();
        cashTransaction.setCash(readyCash);
        cashTransaction.setMoney(accountTransaction.getMoney());
        cashTransaction.setComment(accountTransaction.getComment());
        cashTransaction.setCurrency(accountTransaction.getCurrency());
        cashTransaction.setType(accountTransaction.getType());
        cashTransaction.setDate(accountTransaction.getDate());
        cashTransaction.setBeforeTransaction(readyCash.getLatestTransaction());
        session.save(cashTransaction);
        readyCash.getCashTransaction().add(cashTransaction);

        account.setMoney( account.getMoney() - accountTransaction.getMoney() );
        session.update(account);
    }

    private void saveOutTransfer(AccountTransaction accountTransaction, Account account, Session session) {
        account.setMoney( account.getMoney() - accountTransaction.getMoney() );
        session.update(account);
    }

    private void fillTransactionFromFields(AccountTransaction accountTransaction) {
        accountTransaction.setAccount(newAccountComboBox.getSelectionModel().getSelectedItem());
        accountTransaction.setType(newTransactionTypeComboBox.getSelectionModel().getSelectedItem());
        accountTransaction.setMoney( Float.valueOf(newTransactionAmountTextField.getText()) );
        accountTransaction.setComment(newTransactionCommentField.getText());
        accountTransaction.setCurrency(currencyComboBox.getSelectionModel().getSelectedItem());
        if(newTransactionDatePicker.getValue() != null){
            LocalDate localDate = newTransactionDatePicker.getValue();
            Instant instant = Instant.from(localDate.atStartOfDay(ZoneId.systemDefault()));
            Date tmpdate = Date.from(instant);
            accountTransaction.setDate(tmpdate);
        }
    }

    private void checkValidTransaction() throws Exception {
        StringBuilder buffer = new StringBuilder();

        if( newTransactionTypeComboBox.getSelectionModel().getSelectedItem().getSign().equals("-") ) {
            if( Float.valueOf( newTransactionAmountTextField.getText() ) > newAccountComboBox.getSelectionModel().getSelectedItem().getMoney()  ) {
                buffer.append("Nincs elég pénz a számládon!\n");
            }
        }

        if( buffer.toString().length() != 0 ) {
            throw new Exception(buffer.toString());
        }
    }

    private void fillEmptyFields(AccountTransaction accountTransaction) {

        if( newTransactionDatePicker.getValue() == null ) {
            accountTransaction.setDate(new Date());
        }

    }

    private void checkAllFields() throws Exception {
        StringBuilder buffer = new StringBuilder();

        if( newAccountComboBox.getSelectionModel().getSelectedItem() == null  ) {
            buffer.append("Számla választása kötelező!\n");
        }

        if( newTransactionTypeComboBox.getSelectionModel().getSelectedItem() == null ) {
            buffer.append("Tranzakció típusa választása kötelező!\n");
        }

        try {
            if( Float.valueOf(newTransactionAmountTextField.getText()) < 0 ) {
                buffer.append("Nullánál nagyobb számot lehet megadni csak összegnek!\n");
            }
        } catch (NumberFormatException ex) {
            buffer.append("Összegnek csak számot lehet megadni!\n");
        }

        // TODO: itt kell ellenőrizni a további feltételeket is

        if( buffer.toString().length() != 0 ) {
            throw new Exception(buffer.toString());
        }
    }

    private void loadTransactionToFrom(AccountTransaction accountTransaction) {
        editAccountTransaction = accountTransaction;
        newAccountComboBox.getSelectionModel().select(accountTransaction.getAccount());
        newTransactionTypeComboBox.getSelectionModel().select(accountTransaction.getType());
        newTransactionAmountTextField.setText(String.valueOf(accountTransaction.getMoney()));
        currencyComboBox.getSelectionModel().select(accountTransaction.getCurrency());
        newTransactionDatePicker.setValue(Constant.localDateFromDate(accountTransaction.getDate()));
        newTransactionCommentField.setText(accountTransaction.getComment());
    }

    private void clearAllFieldsOnEditPane() {
//        newAccountComboBox.getSelectionModel().select(0);
//        newTransactionTypeComboBox.getSelectionModel().select(0);
//        newTransactionAmountTextField.setText("");
//        currencyComboBox.getSelectionModel().select(0);
//        newTransactionDatePicker.setValue(null);
//        newTransactionCommentField.setText("");
//        newTransactionFromPocket.getSelectionModel().select(0);
//        amointInPocketsLabel.setText("");
//        anotherAccNum1.setText("");
//        anotherAccNum2.setText("");
//        anotherAccNum3.setText("");
    }

    private List<AccountTransaction> getAllAccountTransactions() {
        List<AccountTransaction> accountTransactions = new LinkedList<>();
        User logged = Main.getLoggedUser();
        for(Account account : logged.getAccounts()) {
            accountTransactions.addAll(account.getAccountTransactions());
        }
        return accountTransactions;
    }

    private void loadTransactionsToTable() {
        transactionTableView.getItems().clear();
        User loggedUser = Main.getLoggedUser();
        for( Account account : loggedUser.getAccounts() ) {
            account.tickAllTransactions();
            transactionTableView.getItems().addAll(account.getAccountTransactions());
        }
    }

    private class SearchListener implements ChangeListener {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {

            Set<AccountTransaction> accountTransactions = new HashSet<>(allAccountTransactions);

            if( searcMoneyFromField.getText().length() > 0 ) {
                for( AccountTransaction accountTransaction : allAccountTransactions) {
                    if( accountTransaction.getMoney() < Float.valueOf(searcMoneyFromField.getText()) ) {
                        accountTransactions.remove(accountTransaction);
                    }
                }
            }

            if( searcMoneyToField.getText().length() > 0 ) {
                allAccountTransactions.stream().filter(
                        accountTransaction ->
                                accountTransaction.getMoney() > Float.valueOf(searcMoneyToField.getText())).forEach(accountTransactions::remove);
            }

            if(searchFromDate.getValue() != null) {
                Date from = Constant.dateFromLocalDate(searchFromDate.getValue());
                new HashSet<>(allAccountTransactions).stream().filter(
                        accountTransaction ->
                                accountTransaction.getDate().before(from)).forEach(allAccountTransactions::remove);
            }

            if(searchToDate.getValue() != null) {
                Date to = Constant.dateFromLocalDate(searchToDate.getValue());
                new HashSet<>(allAccountTransactions).stream().filter(
                        accountTransaction ->
                                accountTransaction.getDate().after(to)).forEach(allAccountTransactions::remove);
            }

            if( searchCommentField.getText().length() != 0 ) {
                new HashSet<>(allAccountTransactions).stream().filter(
                        accountTransaction ->
                                accountTransaction.getComment().toLowerCase().contains(searchCommentField.getText().trim().toLowerCase())).forEach(allAccountTransactions::remove);
            }

            if( newValue instanceof Boolean ) {
                System.out.println("checkbox");
            }

            System.out.println(searchFromDate.getValue());
            System.out.println(searchToDate.getValue());

            transactionTableView.getItems().setAll(accountTransactions);

        }
    }

    private class ButtonCell extends TableCell<Object, Boolean> {
        final Hyperlink cellButtonDelete = new Hyperlink(Bundles.getString("delete"));
        final Hyperlink cellButtonEdit = new Hyperlink(Bundles.getString("edit"));
        final HBox hb = new HBox(cellButtonDelete, cellButtonEdit);

        ButtonCell(final TableView tblView) {
            hb.setSpacing(4);
            cellButtonDelete.setOnAction((ActionEvent t) -> {
                int row = getTableRow().getIndex();
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Biztos benne?");
                alert.setHeaderText("Biztosan törölni szeretné a kiválasztott tételt?");
                alert.setContentText("A törlés következtében az adat elveszik.");
                Optional<ButtonType> result = alert.showAndWait();
                if( result.get() == ButtonType.OK ) {
                    AccountTransaction accountTransaction = transactionTableView.getItems().get(row);
                    transactionTableView.getItems().remove(accountTransaction);

                    Session session = SessionUtil.getSession();
                    org.hibernate.Transaction tx = session.beginTransaction();
                    session.delete(accountTransaction);
                    tx.commit();
                    session.close();
                }
            });

            cellButtonEdit.setOnAction((ActionEvent t) -> {
                int row = getTableRow().getIndex();
                transactionTableView.getSelectionModel().select(row);
                tablePane.setOpacity(0);
                loadTransactionToFrom( transactionTableView.getItems().get(row) );
                new FadeInUpTransition(editPane).play();
            });
        }

        @Override
        protected void updateItem(Boolean item, boolean empty) {
            super.updateItem(item, empty);
            if(!empty) {
                setGraphic(hb);
            } else {
                setGraphic(null);
            }
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
                writer.append("Account number;Money;Money on acount;Currency;Date;Type;Another account number;Comment\n");
                for(int i = 0; i < transactionTableView.getItems().size(); i++){
                    writer.append(transactionTableView.getItems().get(i).getAccount().toString());
                    writer.append(';');
                    writer.append(String.valueOf(transactionTableView.getItems().get(i).getMoney()));
                    writer.append(';');
                    writer.append(String.valueOf(transactionTableView.getItems().get(i).getCurrency()));
                    writer.append(';');
                    writer.append(transactionTableView.getItems().get(i).getDate().toString());
                    writer.append(';');
                    writer.append(transactionTableView.getItems().get(i).getType().toString());
                    writer.append(';');
                    if( transactionTableView.getItems().get(i).getAnotherAccount() != null ) {
                        writer.append(transactionTableView.getItems().get(i).getAnotherAccount());
                        writer.append(';');

                    } else {
                        writer.append("null");
                        writer.append(';');
                    }
                    writer.append(transactionTableView.getItems().get(i).getComment());
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
