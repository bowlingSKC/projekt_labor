package pl.controllers;


import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.hibernate.*;
import pl.Constant;
import pl.Main;
import pl.animations.FadeInUpTransition;
import pl.jpa.SessionUtil;
import pl.model.*;
import pl.model.Transaction;

import java.io.IOException;
import java.text.SimpleDateFormat;
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

    @FXML
    private AnchorPane tablePane;
    @FXML
    private AnchorPane editPane;

    // ========= EDIT PANE =========
    @FXML
    private ComboBox<String> newTransactionType;
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
    // ========= EDIT PANE VÉGE =========

    @FXML
    public void initialize() {

        tablePane.setOpacity(0);
        new FadeInUpTransition(tablePane).play();

        // TableView
        accountTableColumn.setCellValueFactory(new PropertyValueFactory<>("account"));
        moneyTableColumn.setCellValueFactory(new PropertyValueFactory<>("money"));
        dateTableColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        transactionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        moneyTableColumn.setCellFactory(column -> new TableCell<Transaction, Float>() {
            @Override
            protected void updateItem(Float item, boolean empty) {
                super.updateItem(item, empty);

                if( item == null || empty ) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(Float.toString(item));

                    if( item < 0 ) {
                        setStyle("-fx-background-color: indianred;");
                    } else {
                        setStyle("");
                    }
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
        newTransactionType.getItems().add("Készpénz");
        newTransactionType.getItems().add("Bankkártya");
        newAccountComboBox.getItems().setAll(Main.getLoggedUser().getAccounts());
        newTransactionTypeComboBox.getItems().setAll(Constant.getTransactionTypes());
        currencyComboBox.getItems().setAll(Constant.getCurrencies());

        newTransactionType.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if( newTransactionType.getSelectionModel().getSelectedItem().equals("Bankkártya") ) {
                newAccountComboBox.setVisible(true);
            } else {
                newAccountComboBox.setVisible(false);
            }
        });
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
        /*
        Transaction transaction = new Transaction();
        if( newTransactionType.getSelectionModel().getSelectedItem().equals("Készpénz") ) {
            transaction.setBeforeMoney(Main.getLoggedUser().getReadycash().getMoney());
        } else {
            transaction.setBeforeMoney(newAccountComboBox.getSelectionModel().getSelectedItem().getMoney());
        }
        transaction.setAccount(newAccountComboBox.getSelectionModel().getSelectedItem());
        transaction.setComment(newTransactionCommentField.getText());
        transaction.setDate(Constant.dateFromLocalDate(newTransactionDatePicker.getValue()));
        transaction.setType(newTransactionTypeComboBox.getSelectionModel().getSelectedItem());
        float amount = Float.valueOf( newTransactionAmountTextField.getText() );
        transaction.setMoney(amount);

        Session session = SessionUtil.getSession();
        org.hibernate.Transaction tx = session.beginTransaction();
        session.save(transaction);
        tx.commit();
        session.close();

        newAccountComboBox.getSelectionModel().getSelectedItem().getTransactions().add(transaction);
        */ // TODO
        loadTransactionsToTable();
        tablePane.setOpacity(0);
        new FadeInUpTransition(tablePane).play();
    }

    private void loadTransactionToFrom(Transaction transaction) {

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
        final Hyperlink cellButtonDelete = new Hyperlink("Törlés");
        final Hyperlink cellButtonEdit = new Hyperlink("Módosítás");
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

}
