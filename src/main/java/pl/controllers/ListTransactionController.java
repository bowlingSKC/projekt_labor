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
import org.hibernate.Query;
import org.hibernate.Session;
import pl.Constant;
import pl.Main;
import pl.MessageBox;
import pl.animations.FadeInUpTransition;
import pl.bundles.Bundles;
import pl.jpa.SessionUtil;
import pl.model.*;
import pl.model.Currency;

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
    private final List<Transaction> allTransactions = getAllTransactions();

    @FXML
    private TableView<Transaction> transactionTableView;
    @FXML
    private TableColumn crudColumn;
    @FXML
    private TableColumn<Transaction, Account> accountTableColumn;
    @FXML
    private TableColumn<Transaction, Float> moneyTableColumn;
    @FXML
    private TableColumn<Transaction, Float> beforeMoneyColumn;
    @FXML
    private TableColumn<Transaction, Date> dateTableColumn;
    @FXML
    private TableColumn<Transaction, TransactionType> transactionTypeColumn;
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

    @FXML
    public void initialize() {

        tablePane.setOpacity(0);
        new FadeInUpTransition(tablePane).play();

        // TableView
        accountTableColumn.setCellValueFactory(new PropertyValueFactory<>("account"));
        moneyTableColumn.setCellValueFactory(new PropertyValueFactory<>("money"));
        beforeMoneyColumn.setCellValueFactory(new PropertyValueFactory<>("beforeMoney"));
        dateTableColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        transactionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        transactionTableView.setRowFactory(tableView -> new TableRow<Transaction>() {
            @Override
            protected void updateItem(Transaction item, boolean empty) {
                super.updateItem(item, empty);
                if( item != null && !empty ) {
                    if( item.getType().getSign().equals("-") ) {
                        setStyle("-fx-background-color: lightcoral;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        moneyTableColumn.setCellFactory(col -> new TableCell<Transaction, Float>() {
            @Override
            protected void updateItem(Float item, boolean empty) {
                super.updateItem(item, empty);

                if( item != null && !empty ) {
                    setText( Constant.getNumberFormat().format(item) );
                } else {
                    setText("");
                }
            }
        });

        beforeMoneyColumn.setCellFactory(col -> new TableCell<Transaction, Float>() {
            @Override
            protected void updateItem(Float item, boolean empty) {
                super.updateItem(item, empty);
                if( item != null && !empty ) {
                    setText(Constant.getNumberFormat().format(item));
                } else {
                    setText("");
                }
            }
        });

        dateTableColumn.setCellFactory(column -> new TableCell<Transaction, Date>() {
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

        initEditTransaction();
    }

    private void initEditTransaction() {
        newAccountComboBox.getItems().setAll(Main.getLoggedUser().getAccounts());
        newTransactionTypeComboBox.getItems().setAll(Constant.getTransactionTypes());
        currencyComboBox.getItems().setAll(Constant.getCurrencies());

        newAccountComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> newTransactionFromPocket.getItems().setAll(newValue.getPockets()));
        newTransactionFromPocket.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> amointInPocketsLabel.setText( newValue.getMoney() + " " + newAccountComboBox.getSelectionModel().getSelectedItem().getCurrency()));

        newTransactionTypeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            hideAllTypePane();
            if( newValue.getId() == 1 ) {
                new FadeInUpTransition(transferPane).play();
            }
        });

    }

    private void hideAllTypePane() {
        transferPane.setOpacity(0);
    }

    @FXML
    private void handleCancel() {
        loadTransactionsToTable();
        editPane.setOpacity(0);
        new FadeInUpTransition(tablePane).play();
    }

    @FXML
    private void handleNewTransaction() {
        loadTransactionToFrom(new Transaction());
        tablePane.setOpacity(0);
        new FadeInUpTransition(editPane).play();
    }

    @FXML
    private void handleSaveTransaction() {

        try {
            checkAllFields();
            checkValidTransaction();

            Transaction transaction = new Transaction();
            fillTransactionFromFields(transaction);
            fillEmptyFields(transaction);

            saveTransactionToDatabase(transaction);

            loadTransactionsToTable();
            clearAllFieldsOnEditPane();
            editPane.setOpacity(0);
            new FadeInUpTransition(tablePane).play();
        } catch (Throwable ex) {
            MessageBox.showErrorMessage("Hiba", "A tranzakciót nem lehet végrehajtani!", ex.getMessage(), false);
        }
    }

    private void saveTransactionToDatabase(Transaction transaction) {
        Account account = transaction.getAccount();
        Session session = SessionUtil.getSession();
        org.hibernate.Transaction tx = session.beginTransaction();

        if( newTransactionTypeComboBox.getSelectionModel().getSelectedItem().getId() == 1 ) {
            account.setMoney( account.getMoney() - transaction.getMoney() );
            session.update(account);
        }

        if( newTransactionTypeComboBox.getSelectionModel().getSelectedItem().getId() == 2) {
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
                readyCash.setMoney(transaction.getMoney());
                readyCash.setOwner(Main.getLoggedUser());
                session.save(readyCash);

                Main.getLoggedUser().getReadycash().add(readyCash);
            } else {
                readyCash.setMoney( readyCash.getMoney() + transaction.getMoney() );
                session.update(readyCash);
            }
            account.setMoney( account.getMoney() - transaction.getMoney() );
            session.update(account);

        }

        if( newTransactionTypeComboBox.getSelectionModel().getSelectedItem().getId() == 3 ) {
            ReadyCash readyCash = null;
            for(ReadyCash tmp : Main.getLoggedUser().getReadycash()) {
                if( tmp.getCurrency().equals(currencyComboBox.getSelectionModel().getSelectedItem()) ) {
                    readyCash = tmp;
                    break;
                }
            }

            if( readyCash != null ) {
                readyCash.setMoney( readyCash.getMoney() - transaction.getMoney() );
                account.setMoney( account.getMoney() + transaction.getMoney() );

                session.update(account);
                if( readyCash.getMoney() == 0.0f ) {
                    session.delete(readyCash);
                } else {
                    session.update(readyCash);
                }
                session.update(account);
            } else {
                MessageBox.showErrorMessage("Hiba", "A tranzakciót nem lehet végrehajtani!", "Nincs készpénzben ilyen valuta regisztrálva!", false);
                session.close();
                return;
            }
        }

        session.save(transaction);

        tx.commit();
        session.close();

        account.getTransactions().add(transaction);
    }

    private void fillTransactionFromFields(Transaction transaction) {
        transaction.setAccount(newAccountComboBox.getSelectionModel().getSelectedItem());
        transaction.setType(newTransactionTypeComboBox.getSelectionModel().getSelectedItem());
        transaction.setMoney( Float.valueOf(newTransactionAmountTextField.getText()) );
        transaction.setComment(newTransactionCommentField.getText());
        //New
        transaction.setCurrency(currencyComboBox.getSelectionModel().getSelectedItem());
        if(newTransactionDatePicker.getValue() != null){
            LocalDate localDate = newTransactionDatePicker.getValue();
            Instant instant = Instant.from(localDate.atStartOfDay(ZoneId.systemDefault()));
            Date tmpdate = Date.from(instant);
            transaction.setDate(tmpdate);
        }
    }

    private void checkValidTransaction() throws Exception {
        StringBuilder buffer = new StringBuilder();

        if( Float.valueOf( newTransactionAmountTextField.getText() ) > newAccountComboBox.getSelectionModel().getSelectedItem().getMoney()  ) {
            buffer.append("Nincs elég pénz a számládon!\n");
        }

        if( buffer.toString().length() != 0 ) {
            throw new Exception(buffer.toString());
        }
    }

    private void fillEmptyFields(Transaction transaction) {
        transaction.setBeforeMoney( newAccountComboBox.getSelectionModel().getSelectedItem().getMoney() );

        if( newTransactionDatePicker.getValue() == null ) {
            transaction.setDate(new Date());
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

    private void loadTransactionToFrom(Transaction transaction) {
        // TODO
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

    private List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new LinkedList<>();
        User logged = Main.getLoggedUser();
        for(Account account : logged.getAccounts()) {
            transactions.addAll(account.getTransactions());
        }
        return transactions;
    }

    private void loadTransactionsToTable() {
        transactionTableView.getItems().clear();
        User loggedUser = Main.getLoggedUser();
        for( Account account : loggedUser.getAccounts() ) {
            transactionTableView.getItems().addAll(account.getTransactions());
        }
    }

    private class SearchListener implements ChangeListener {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {

            Set<Transaction> transactions = new HashSet<>(allTransactions);

            if( searcMoneyFromField.getText().length() > 0 ) {
                for( Transaction transaction : allTransactions ) {
                    if( transaction.getMoney() < Float.valueOf(searcMoneyFromField.getText()) ) {
                        transactions.remove(transaction);
                    }
                }
            }

            if( searcMoneyToField.getText().length() > 0 ) {
                for( Transaction transaction : allTransactions ) {
                    if( transaction.getMoney() > Float.valueOf(searcMoneyFromField.getText()) ) {
                        transactions.remove(transaction);
                    }
                }
            }


            transactionTableView.getItems().setAll(transactions);

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
                    Transaction transaction = transactionTableView.getItems().get(row);
                    transactionTableView.getItems().remove(transaction);

                    Session session = SessionUtil.getSession();
                    org.hibernate.Transaction tx = session.beginTransaction();
                    session.delete(transaction);
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

                for(int i = 0; i < transactionTableView.getItems().size(); i++){
                    writer.append(transactionTableView.getItems().get(i).getAccount().toString());
                    writer.append(';');
                    writer.append(String.valueOf(transactionTableView.getItems().get(i).getMoney()));
                    writer.append(';');
                    writer.append(String.valueOf(transactionTableView.getItems().get(i).getBeforeMoney()));
                    writer.append(';');
                    writer.append(String.valueOf(transactionTableView.getItems().get(i).getCurrency()));
                    writer.append(';');
                    writer.append(transactionTableView.getItems().get(i).getDate().toString());
                    writer.append(';');
                    writer.append(transactionTableView.getItems().get(i).getType().toString());
                    writer.append(';');
                    writer.append(transactionTableView.getItems().get(i).getAnotherAccount().toString());
                    writer.append(';');
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
