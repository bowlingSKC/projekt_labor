package pl.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.hibernate.Session;
import org.hibernate.Transaction;
import pl.Constant;
import pl.CurrencyExchange;
import pl.Main;
import pl.MessageBox;
import pl.bundles.Bundles;
import pl.jpa.SessionUtil;
import pl.model.CashTransaction;
import pl.model.Currency;
import pl.model.ReadyCash;
import pl.model.TransactionType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ReadyCashController {

    @FXML
    private ComboBox<String> transactionTypeComboBox;
    @FXML
    private TextField amountFiled;
    @FXML
    private ComboBox<Currency> currencyComboBox;

    @FXML
    private TableView<ReadyCash> readyCashTableView;
    @FXML
    private TableColumn<ReadyCash, String> currencyColumn;
    @FXML
    private TableColumn<ReadyCash, Float> amountColumn;
    @FXML
    private TableColumn<ReadyCash, Float> inHufColumn;
    @FXML
    private Label sumLabel;

    // Tranzakciók
    @FXML
    private TableView<CashTransaction> transactionTableView;
    @FXML
    private TableColumn<CashTransaction, Date> dateTableColumn;
    @FXML
    private TableColumn<CashTransaction, TransactionType> typeTableColumn;
    @FXML
    private TableColumn<CashTransaction, Float> amountTableColumn;
    @FXML
    private TableColumn<CashTransaction, Currency> currencyTableColumn;
    @FXML
    private TableColumn<CashTransaction, String> commentTableColumn;
    @FXML
    private TextField commentField;

    private Map<String,Float> hufvalues = new HashMap<>();

    @FXML
    public void initialize() {
        initTransactionPane();
        initTablePane();
        initTransactionTablePane();
    }

    private void initTransactionTablePane() {

        readyCashTableView.setRowFactory(new Callback<TableView<ReadyCash>, TableRow<ReadyCash>>() {
            @Override
            public TableRow<ReadyCash> call(TableView<ReadyCash> param) {
                return new TableRow<ReadyCash>() {
                    @Override
                    protected void updateItem(ReadyCash item, boolean empty) {
                        super.updateItem(item, empty);
                        if( item != null && !empty ) {
                            if( Float.compare(item.getMoney(), 0.0f) == 0 ) {
                                setVisible(false);
                            } else {
                                setVisible(true);
                            }
                        }
                    }
                };
            }
        });

        dateTableColumn.setCellFactory(t -> new TableCell<CashTransaction, Date>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if( item != null && !empty ) {
                    setText(Constant.getDateFormat().format(item));
                } else {
                    setText("");
                }
            }
        });
        amountTableColumn.setCellFactory(t -> new TableCell<CashTransaction, Float>() {
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

        inHufColumn.setCellFactory(new Callback<TableColumn<ReadyCash, Float>, TableCell<ReadyCash, Float>>() {
            @Override
            public TableCell<ReadyCash, Float> call(TableColumn<ReadyCash, Float> param) {
                return new TableCell<ReadyCash, Float>() {
                    @Override
                    protected void updateItem(Float item, boolean empty) {
                        super.updateItem(item, empty);
                        try {
                            if( this.getTableRow() != null ) {
                                ReadyCash readyCash = readyCashTableView.getItems().get(getTableRow().getIndex());
                                if( !readyCash.getCurrency().equals(Constant.getHufCurrency()) ) {
                                    if( CurrencyExchange.isContainsKey(readyCash.getCurrency()) ) {
                                        setText(Constant.getNumberFormat().format(Math.floor(CurrencyExchange.getValue(readyCash.getCurrency()) * readyCash.getMoney())));
                                        hufvalues.put(readyCash.getCurrency().toString(), (float) Math.floor(CurrencyExchange.getValue(readyCash.getCurrency()) * readyCash.getMoney()));
                                         }
                                } else if( readyCash.getCurrency().equals(Constant.getHufCurrency()) ) {
                                    setText(Constant.getNumberFormat().format(readyCash.getMoney()));
                                    hufvalues.put(readyCash.getCurrency().toString(), readyCash.getMoney());
                                } else {
                                    setText("N/A");
                                }
                            }
                        } catch (Exception ex) {
                            // nem kell kezelni, csak nem éri el a listából, JavaFX hibája
                        }
                    }
                };
            }
        });

        amountTableColumn.setCellValueFactory(new PropertyValueFactory<>("money"));
        dateTableColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        currencyTableColumn.setCellValueFactory(new PropertyValueFactory<>("currency"));
        commentTableColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
        typeTableColumn.setCellValueFactory(new PropertyValueFactory<>("type"));


        updateTransactionTableItems();
    }

    private void updateTransactionTableItems() {
        transactionTableView.getItems().clear();
        for(ReadyCash readyCash : Main.getLoggedUser().getReadycash()) {
            transactionTableView.getItems().addAll(readyCash.getCashTransaction());
        }
    }

    private void initTablePane() {

        sumLabel.setText(Constant.getNumberFormat().format(Main.getLoggedUser().getAllMoneyInReadyCash()));

        readyCashTableView.setRowFactory(row -> new TableRow<ReadyCash>() {
            @Override
            protected void updateItem(ReadyCash item, boolean empty) {
                super.updateItem(item, empty);
                if( item != null && !empty ) {
                    if( item.getMoney() == 0.0 ) {
                        setVisible(false);
                    } else {
                        setVisible(true);
                    }
                }
            }
        });

        currencyColumn.setCellValueFactory(new PropertyValueFactory<>("currency"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("money"));

        amountColumn.setCellFactory(t -> new TableCell<ReadyCash, Float>() {
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

        updateTableData();
    }

    private void initTransactionPane() {
        transactionTypeComboBox.getItems().add(Bundles.getString("in"));
        transactionTypeComboBox.getItems().add(Bundles.getString("out"));
        currencyComboBox.getItems().setAll(Constant.getCurrencies());

        transactionTypeComboBox.getSelectionModel().select(0);
        currencyComboBox.getSelectionModel().select(0);
    }

    @FXML
    private void handleSave() {
        try {
            checkAllField();

            if( transactionTypeComboBox.getSelectionModel().getSelectedItem().equals(Bundles.getString("out")) ) {
                handleOutTransactions();
                System.out.println(Bundles.getString("out"));
            } else {
                handleInTransaction();
                System.out.println(Bundles.getString("in"));
            }
            updateTableData();
            updateTransactionTableItems();
            
        } catch (Throwable ex) {
            MessageBox.showErrorMessage(Bundles.getString("error.nodb.title"), Bundles.getString("error.processing"), ex.getMessage(), false);
        }
    }

    private void updateTableData() {
        readyCashTableView.getItems().setAll(Main.getLoggedUser().getReadycash());
    }

    private void handleInTransaction() throws Exception {
        ReadyCash selected = new ReadyCash(Main.getLoggedUser(), 0.0f);
        boolean was = false;
        selected.setCurrency(currencyComboBox.getSelectionModel().getSelectedItem());
        if( !CurrencyExchange.isContainsKey(currencyComboBox.getSelectionModel().getSelectedItem()) ) {
            CurrencyExchange.addCurrenciesToMap(currencyComboBox.getSelectionModel().getSelectedItem(), 0.0f);
            new Thread(CurrencyExchange::updateZeroCurrencies).start();
        }
        if( Main.getLoggedUser().getReadycash() != null ) {
            for(ReadyCash tmp : Main.getLoggedUser().getReadycash()) {
                if( tmp.getCurrency().equals(currencyComboBox.getSelectionModel().getSelectedItem()) ) {
                    selected = tmp;
                    was = true;
                    break;
                }
            }
        }
        selected.setMoney(selected.getMoney() + Float.valueOf(amountFiled.getText()));

        if( !was ) {
            Main.getLoggedUser().getReadycash().add(selected);
        }

        transactionSaveOrUpdate(selected);
        saveTransactionToDatabase(selected);
    }

    private void handleOutTransactions() throws Exception {
        if( Main.getLoggedUser().getReadycash() == null ) {
            throw new Exception(Bundles.getString("noregistered"));
        }

        ReadyCash readyCash = null;
        for( ReadyCash readyCash1 : Main.getLoggedUser().getReadycash() ) {
            if( readyCash1.getCurrency().equals(currencyComboBox.getSelectionModel().getSelectedItem()) ) {
                readyCash = readyCash1;
                break;
            }
        }

        if( readyCash == null ) {
            throw new Exception(Bundles.getString("noselectedcurr"));
        }

        if( readyCash.getMoney() < Float.valueOf(amountFiled.getText()) ) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(Bundles.getString("notenmon"));
            alert.setHeaderText(Bundles.getString("notenmon"));
            alert.setContentText(Bundles.getString("alltakeoff"));
            Optional<ButtonType> result = alert.showAndWait();
            if( result.get() == ButtonType.OK ) {
                transactionDelete(readyCash);
                Main.getLoggedUser().getReadycash().remove(readyCash);
            }
            return;
        }

        if( Float.compare(readyCash.getMoney() - Float.valueOf(amountFiled.getText()), 0) == 0 ) {
//            transactionDelete(readyCash);
//            Main.getLoggedUser().getReadycash().remove(readyCash);
        } else {
            readyCash.setMoney(readyCash.getMoney() - Float.valueOf(amountFiled.getText()));
            transactionSaveOrUpdate(readyCash);
        }
        saveTransactionToDatabase(readyCash);
    }

    private void saveTransactionToDatabase(ReadyCash readyCash) {
        CashTransaction transaction = new CashTransaction();
        transaction.setMoney(Float.valueOf(amountFiled.getText()));
        transaction.setDate(new Date());
        transaction.setCurrency(currencyComboBox.getSelectionModel().getSelectedItem());
        transaction.setComment(commentField.getText());
        transaction.setBeforeTransaction(Main.getLoggedUser().getLatestCashTransaction(currencyComboBox.getSelectionModel().getSelectedItem()));
        if( transactionTypeComboBox.getSelectionModel().getSelectedItem().equals(Bundles.getString("in")) ) {
            transaction.setType(Constant.getCashInType());
        } else {
            transaction.setType(Constant.getCashOutType());
        }
        transaction.setCash(readyCash);

        Session session = SessionUtil.getSession();
        Transaction tx = session.beginTransaction();
        session.save(transaction);
        tx.commit();
        session.close();

        for(ReadyCash rc : Main.getLoggedUser().getReadycash()) {
            if(rc.getCurrency().equals(currencyComboBox.getSelectionModel().getSelectedItem())) {
                rc.getCashTransaction().add(transaction);
                break;
            }
        }
    }

    private void transactionSaveOrUpdate(ReadyCash selected) {
        Session session = SessionUtil.getSession();
        Transaction tx = session.beginTransaction();
        session.saveOrUpdate(selected);
        tx.commit();
        session.close();
    }

    private void transactionDelete(ReadyCash selected) {
        Session session = SessionUtil.getSession();
        Transaction tx = session.beginTransaction();
        session.delete(selected);
        tx.commit();
        session.close();
    }

    private void checkAllField() throws Exception {
        StringBuilder buffer = new StringBuilder();

        try {
            if( Float.valueOf(amountFiled.getText()) < 0 ) {
                buffer.append(Bundles.getString("gtzero")+"\n");
            }
        } catch (NumberFormatException ex) {
            buffer.append(Bundles.getString("providenumber")+"\n");
        }

        if( buffer.toString().length() != 0 ) {
            throw new Exception(buffer.toString());
        }

    }

    public void handleToCSV(){
        FileWriter writer = null;
        //Write first table
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
                writer.append("Currency;Money;HUF value\n");
                for(int i = 0; i < readyCashTableView.getItems().size(); i++){
                    writer.append(readyCashTableView.getItems().get(i).getCurrency().toString());
                    writer.append(';');
                    writer.append(String.valueOf(readyCashTableView.getItems().get(i).getMoney()));
                    writer.append(';');
                    String tmp = readyCashTableView.getColumns().get(0).getCellData(i).toString();
                    writer.append(String.valueOf(hufvalues.get(tmp)));
                    writer.append('\n');
                    writer.flush();
                }
                writer.append('\n');
                writer.append('\n');
                writer.append("Total;");
                writer.append(sumLabel.getText());
                writer.close();
            }
            if(file != null && fileChooser.getSelectedExtensionFilter() == extFilter2){

                writer = new FileWriter(file);
                writer.append("Currency\tMoney\tHUF value\n");
                for(int i = 0; i < readyCashTableView.getItems().size(); i++){
                    writer.append(readyCashTableView.getItems().get(i).getCurrency().toString());
                    writer.append('\t');
                    writer.append(String.valueOf(readyCashTableView.getItems().get(i).getMoney()));
                    writer.append('\t');
                    String tmp = readyCashTableView.getColumns().get(0).getCellData(i).toString();
                    writer.append(String.valueOf(hufvalues.get(tmp)));
                    writer.append('\n');
                    writer.flush();
                }
                writer.append('\n');
                writer.append('\n');
                writer.append("Total\t");
                writer.append(sumLabel.getText());
                writer.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleToCSV2(){
        FileWriter writer = null;
        //Write second table
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
                writer.append("Date;Currency;Money;Type;Comment\n");
                for(int i = 0; i < transactionTableView.getItems().size(); i++){
                    writer.append(transactionTableView.getItems().get(i).getDate().toString());
                    writer.append(';');
                    writer.append(transactionTableView.getItems().get(i).getCurrency().toString());
                    writer.append(';');
                    writer.append(String.valueOf(transactionTableView.getItems().get(i).getMoney()));
                    writer.append(';');
                    writer.append(String.valueOf(transactionTableView.getItems().get(i).getType().toString()));
                    writer.append(';');
                    writer.append(String.valueOf(transactionTableView.getItems().get(i).getComment()));
                    writer.append('\n');
                    writer.flush();
                }
                writer.close();
            }
            if(file != null && fileChooser.getSelectedExtensionFilter() == extFilter2){

                writer = new FileWriter(file);
                writer.append("Date\tCurrency\tMoney\tType\tComment\n");
                for(int i = 0; i < transactionTableView.getItems().size(); i++){
                    writer.append(transactionTableView.getItems().get(i).getDate().toString());
                    writer.append('\t');
                    writer.append(transactionTableView.getItems().get(i).getCurrency().toString());
                    writer.append('\t');
                    writer.append(String.valueOf(transactionTableView.getItems().get(i).getMoney()));
                    writer.append('\t');
                    writer.append(String.valueOf(transactionTableView.getItems().get(i).getType().toString()));
                    writer.append('\t');
                    writer.append(String.valueOf(transactionTableView.getItems().get(i).getComment()));
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
