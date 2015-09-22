package pl.controllers;


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import pl.Main;
import pl.model.Account;
import pl.model.Transaction;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;

public class ListTransactionController {

    private final SearchListener listener = new SearchListener();

    @FXML
    private TableView<Transaction> transactionTableView;
    @FXML
    private TableColumn<Transaction, Account> accountTableColumn;
    @FXML
    private TableColumn<Transaction, Float> moneyTableColumn;
    @FXML
    private TableColumn<Transaction, Date> dateTableColumn;
    @FXML
    private TableColumn<Transaction, String> fromTableColumn;
    @FXML
    private DatePicker searchFromDate;
    @FXML
    private DatePicker searchToDate;
    @FXML
    private TextField searchCommentField;
    @FXML
    private ScrollPane scrollPane;

    @FXML
    public void initialize() {

        // TableView
        accountTableColumn.setCellValueFactory(new PropertyValueFactory<>("from"));
        moneyTableColumn.setCellValueFactory(new PropertyValueFactory<>("money"));
        fromTableColumn.setCellValueFactory(new PropertyValueFactory<>("to"));
        dateTableColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        moneyTableColumn.setCellFactory(column -> {
            return new TableCell<Transaction, Float>() {
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
            };
        });

        dateTableColumn.setCellFactory(column -> {
            return new TableCell<Transaction, Date>() {
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
            };
        });

        // keres�hez sz�mlasz�mok kiv�laszt�sa
        VBox vbox = new VBox();
        for( Account acc : Main.getLoggedUser().getAccounts() ) {
            CheckBox checkBox = new CheckBox( acc.toString() );
            checkBox.setSelected(true);
            checkBox.selectedProperty().addListener(listener);
            vbox.getChildren().add( checkBox );
        }
        scrollPane.setContent(vbox);

        // tranzakci�k bet�lt�se
        for(Account acc : Main.getLoggedUser().getAccounts()) {
            Set<Transaction> fromTransactions = acc.getFromTransactions();
            System.out.println(fromTransactions.size());
            if( fromTransactions != null ) {
                transactionTableView.getItems().addAll(fromTransactions);
            }
        }

        searchFromDate.valueProperty().addListener(listener);
        searchToDate.valueProperty().addListener(listener);
        searchCommentField.textProperty().addListener(listener);
    }

    private class SearchListener implements ChangeListener {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {

            transactionTableView.getItems().clear();
            for(Account acc : Main.getLoggedUser().getAccounts()) {
                if( acc.getFromTransactions() == null ) {
                    continue;
                }
                transactionTableView.getItems().addAll( acc.getFromTransactions() );
            }

            if( searchFromDate.getValue() != null ) {
                Date date = Date.from(searchFromDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                for(int i = 0; i < transactionTableView.getItems().size(); i++) {
                    if( transactionTableView.getItems().get(i).getDate().before(date) ) {
                        transactionTableView.getItems().remove(i);
                    }
                }
            }

            if( searchToDate.getValue() != null ) {
                Date date = Date.from(searchToDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                for(int i = 0; i < transactionTableView.getItems().size(); i++) {
                    if( transactionTableView.getItems().get(i).getDate().after(date) ) {
                        transactionTableView.getItems().remove(i);
                    }
                }
            }

            if( searchCommentField.getText() != "" ) {
                for(int i = 0; i < transactionTableView.getItems().size(); i++) {
                    if( !transactionTableView.getItems().get(i).getComment().contains( searchCommentField.getText() ) ) {
                        transactionTableView.getItems().remove(i);
                    }
                }
            }

            for( Node node : ((VBox)scrollPane.getContent()).getChildren() ) {
                CheckBox checkBox = (CheckBox) node;
                if( !checkBox.isSelected() ) {
                    String accNo = checkBox.getText().substring( checkBox.getText().indexOf("[") + 1, checkBox.getText().lastIndexOf("]") );
                    for(int i = 0; i < transactionTableView.getItems().size(); i++) {
                        if( transactionTableView.getItems().get(i).getFrom().getAccountNumber().equals(accNo) ) {
                            transactionTableView.getItems().remove(i);
                        }
                    }
                }
            }
        }
    }
}
