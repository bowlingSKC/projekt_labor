package pl.controllers;

import com.sun.media.sound.EmergencySoundbank;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.hibernate.Session;
import org.hibernate.Transaction;
import pl.Constant;
import pl.Main;
import pl.MessageBox;
import pl.animations.FadeInUpTransition;
import pl.bundles.Bundles;
import pl.jpa.SessionUtil;
import pl.model.Account;
import pl.model.Currency;
import pl.model.Debit;
import pl.model.ReadyCash;

import javax.xml.soap.Text;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

public class DebitController {

    @FXML
    private AnchorPane tablePane;
    @FXML
    private AnchorPane editPane;
    @FXML
    private AnchorPane payPane;

    @FXML
    private TableView<Debit> debitTableView;
    @FXML
    private TableColumn<Debit, String> whoColumn;
    @FXML
    private TableColumn<Debit, Float> amountColumn;
    @FXML
    private TableColumn<Debit, Currency> currencyColumn;
    @FXML
    private TableColumn<Debit, Date> dateColumn;
    @FXML
    private TableColumn actionsColumn;

    @FXML
    private TextField toField;
    @FXML
    private TextField moneyField;
    @FXML
    private ComboBox<Currency> currencyComboBox;
    @FXML
    private DatePicker deadlinePicker;
    @FXML
    private TextField commentField;

    @FXML
    private Label payTitleLabel;
    @FXML
    private TextField payAmountField;
    @FXML
    private ComboBox<Currency> payCurrencyComboBox;
    @FXML
    private ComboBox<String> payCashTypeComboBox;
    @FXML
    private Label payAccoutLabel;
    @FXML
    private ComboBox<Account> payAccountComboBox;

    private Debit payDebit = null;

    @FXML
    public void initialize() {

        new FadeInUpTransition(tablePane).play();

        initTablePane();
        initEditPane();
        initPayPane();
    }

    private void initPayPane() {
        payCashTypeComboBox.getItems().setAll(Bundles.getString("account"), Bundles.getString("cash"));
        payCashTypeComboBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if( newValue.equals(Bundles.getString("account")) ) {
                payAccoutLabel.setVisible(false);
                payAccountComboBox.setVisible(false);
            } else {
                payAccoutLabel.setVisible(true);
                payAccountComboBox.setVisible(true);
            }
        });

        payCurrencyComboBox.getItems().setAll(Constant.getCurrencies());

        payAccountComboBox.getItems().setAll(Main.getLoggedUser().getAccounts());
        payAccountComboBox.getSelectionModel().select(0);
    }

    private void initEditPane() {
        currencyComboBox.getItems().setAll(Constant.getCurrencies());
        currencyComboBox.getSelectionModel().select(Constant.getHufCurrency());
    }

    private void initTablePane() {
        whoColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("money"));
        currencyColumn.setCellValueFactory(new PropertyValueFactory<>("currency"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("deadline"));

        // TODO
        debitTableView.setRowFactory(row -> new TableRow<Debit>() {
            @Override
            protected void updateItem(Debit item, boolean empty) {
                super.updateItem(item, empty);
                if( item != null & !empty ) {
                    long nowTime = new Date().getTime();
                    long deadlineTime = item.getDeadline().getTime();
                    int diffDays = (int)((deadlineTime - nowTime) / (1000 * 60 * 60 * 24));
                    if( diffDays == 0 ) {
                        setStyle("-fx-background-color: lightcoral;");
                    } else if( diffDays < 7 ) {
                        setStyle("-fx-background-color: coral;");
                    } else if( diffDays < 3 ) {
                        setStyle("-fx-background-color: yellow;");
                    }
                }
            }
        });

        amountColumn.setCellFactory(col -> new TableCell<Debit, Float>() {
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

        dateColumn.setCellFactory(col -> new TableCell<Debit, Date>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if( item != null && !empty ) {
                    setText( Constant.getDateFormat().format(item) );
                } else {
                    setText("");
                }
            }
        });

        actionsColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Debit, Boolean>, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<Debit, Boolean> param) {
                return new SimpleBooleanProperty(param.getValue() != null);
            }
        });

        actionsColumn.setCellFactory(param -> new ButtonCell(debitTableView));

        updateTableItems();
    }

    @FXML
    private void handleNewDebit() {
        tablePane.setOpacity(0);
        new FadeInUpTransition(editPane).play();
    }

    @FXML
    private void handleBackToTablePane() {
        payDebit = null;
        updateTableItems();
        editPane.setOpacity(0);
        payPane.setOpacity(0);
        new FadeInUpTransition(tablePane).play();
    }

    @FXML
    private void handleSaveDebit() {
        try {
            checkFields();

            Debit debit = createDebitFromFields();

            Session session = SessionUtil.getSession();
            Transaction tx = session.beginTransaction();
            session.save(debit);
            tx.commit();
            session.close();

            Main.getLoggedUser().getDebits().add(debit);
            handleBackToTablePane();
        } catch (Throwable ex) {
            MessageBox.showErrorMessage(Bundles.getString("error.nodb.title"), Bundles.getString("nodebitsave"), ex.getMessage(), false);
        }
    }

    private Debit createDebitFromFields() {
        Debit debit = new Debit();
        debit.setName( toField.getText() );
        debit.setCurrency(currencyComboBox.getSelectionModel().getSelectedItem());
        debit.setOwner(Main.getLoggedUser());
        debit.setDeadline(Constant.dateFromLocalDate(deadlinePicker.getValue()));
        debit.setDescription(commentField.getText());
        debit.setMoney(Float.valueOf(moneyField.getText()));
        return debit;
    }

    private void checkFields() throws Exception {
        StringBuilder builder = new StringBuilder();

        if( toField.getText().length() == 0 ) {
            builder.append(Bundles.getString("error.payee") + "\n");
        }

        try {
            if( Float.valueOf(moneyField.getText()) < 0 ) {
                builder.append(Bundles.getString("gtzero") + "\n");
            }
        } catch (NumberFormatException ex) {
            builder.append(Bundles.getString("moneynumber") + "\n");
        }

        if( currencyComboBox.getSelectionModel().getSelectedItem() == null ) {
            builder.append(Bundles.getString("havetocurrency") + "\n");
        }

        if( deadlinePicker.getValue() != null ) {
            if( deadlinePicker.getValue().isBefore(LocalDate.now()) ) {
                builder.append(Bundles.getString("duebefore") + "\n");
            }
        }

        if( builder.toString().length() != 0 ) {
            throw new Exception(builder.toString());
        }
    }

    private void updateTableItems() {
        debitTableView.getItems().setAll(Main.getLoggedUser().getDebits());
    }

    private class ButtonCell extends TableCell<Object, Boolean> {
        final Hyperlink editLink = new Hyperlink(Bundles.getString("menu.debit.edit"));
        final Hyperlink deleteLink = new Hyperlink(Bundles.getString("menu.debit.delete"));
        final Hyperlink payLink = new Hyperlink(Bundles.getString("balancA"));
        final HBox hbox = new HBox(payLink, editLink, deleteLink);

        ButtonCell(final TableView tbl) {
            hbox.setSpacing(4);

            editLink.setOnAction((ActionEvent event) -> {

            });

            deleteLink.setOnAction((ActionEvent event) -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle( Bundles.getString("confirmation") );
                alert.setHeaderText(Bundles.getString("deletedebit"));
                alert.setContentText(Bundles.getString("undone"));

                Optional<ButtonType> result = alert.showAndWait();
                if( result.get() == ButtonType.OK ) {
                    int row = getTableRow().getIndex();
                    Debit selected = debitTableView.getItems().get(row);

                    Main.getLoggedUser().getDebits().remove(selected);
                    Session session = SessionUtil.getSession();
                    Transaction tx = session.beginTransaction();
                    session.delete(selected);
                    tx.commit();
                    session.close();

                    updateTableItems();
                }
            });

            payLink.setOnAction((ActionEvent e) -> {
                payDebit = debitTableView.getItems().get(getTableRow().getIndex());
                loadToPayPane(debitTableView.getItems().get(getTableRow().getIndex()));
                tablePane.setOpacity(0);
                new FadeInUpTransition(payPane).play();
            });
        }

        @Override
        protected void updateItem(Boolean item, boolean empty) {
            super.updateItem(item, empty);
            if(!empty) {
                setGraphic(hbox);
            } else {
                setGraphic(null);
            }
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
                writer.append("Name;Money;Currency;Deadline\n");
                for(int i = 0; i < debitTableView.getItems().size(); i++){
                    writer.append(debitTableView.getItems().get(i).getName());
                    writer.append(';');
                    writer.append(String.valueOf(debitTableView.getItems().get(i).getMoney()));
                    writer.append(';');
                    writer.append(debitTableView.getItems().get(i).getCurrency().toString());
                    writer.append(';');
                    writer.append(debitTableView.getItems().get(i).getDeadline().toString());
                    /*writer.append(';');
                    writer.append(accountTableView.getItems().get(i).getBank().toString());
                    writer.append(';');
                    writer.append(accountTableView.getItems().get(i).getCreatedDate().toString());
                    */writer.append('\n');
                    writer.flush();
                }
                writer.close();
            }
            if(file != null && fileChooser.getSelectedExtensionFilter() == extFilter2){
                writer = new FileWriter(file);
                writer.append("Name\tMoney\tCurrency\tDeadline\n");
                for(int i = 0; i < debitTableView.getItems().size(); i++){
                    writer.append(debitTableView.getItems().get(i).getName());
                    writer.append('\t');
                    writer.append(String.valueOf(debitTableView.getItems().get(i).getMoney()));
                    writer.append('\t');
                    writer.append(debitTableView.getItems().get(i).getCurrency().toString());
                    writer.append('\t');
                    writer.append(debitTableView.getItems().get(i).getDeadline().toString());
                    /*writer.append(';');
                    writer.append(accountTableView.getItems().get(i).getBank().toString());
                    writer.append(';');
                    writer.append(accountTableView.getItems().get(i).getCreatedDate().toString());
                    */writer.append('\n');
                    writer.flush();
                }
                writer.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadToPayPane(Debit debit) {
        payTitleLabel.setText(debit.getName());
        payAmountField.setText(debit.getMoney() + "");
        payCurrencyComboBox.getSelectionModel().select(debit.getCurrency());

        payDebit = debit;
    }

    @FXML
    private void handlePay() {
        try {
            checkPayFields();
            checkMoneyInCash();

            float newValue = Float.valueOf(payAmountField.getText());

            Session session = SessionUtil.getSession();
            Transaction tx = session.beginTransaction();
            if( (payDebit.getMoney() - newValue) == 0 ) {
                session.delete(payDebit);
                Main.getLoggedUser().getDebits().remove(payDebit);
            } else {
                payDebit.setMoney(Float.valueOf(payAmountField.getText()));
            }
            session.close();

            handleBackToTablePane();
        } catch (Exception ex) {
            MessageBox.showErrorMessage(Bundles.getString("error.nodb.title"),Bundles.getString("error.processing"), ex.getMessage(), false);
        }
    }

    private void checkMoneyInCash() throws Exception {
        float amount = Float.valueOf(payAmountField.getText());
        if( payCashTypeComboBox.getSelectionModel().getSelectedItem().equals(Bundles.getString("account")) ) {
            if( (payAccountComboBox.getSelectionModel().getSelectedItem().getMoney() - amount) < 0 ) {
                throw new Exception(Bundles.getString("notenoughmoney"));
            }
        } else {
            ReadyCash selected = null;
            for(ReadyCash readyCash : Main.getLoggedUser().getReadycash()) {
                if( payCurrencyComboBox.getSelectionModel().getSelectedItem().equals(readyCash.getCurrency()) ) {
                    selected = readyCash;
                    break;
                }
            }
            if( selected == null ) {
                throw new Exception(Bundles.getString("noselectedcurr"));
            }

            if( selected.getMoney() < amount ) {
                throw new Exception(Bundles.getString("notenmon"));
            }
        }
    }

    private void checkPayFields() throws Exception {
        StringBuffer buffer = new StringBuffer();

        try {
            if( Float.valueOf(payAmountField.getText()) < 0 ) {
                buffer.append(Bundles.getString("providenumber") +"\n");
            }
            if( payDebit.getMoney() < Float.valueOf(payAmountField.getText()) ) {
                buffer.append(Bundles.getString("debitmore")+"\n");
                buffer.append(Bundles.getString("debitoriginal")+" "+ payDebit.getMoney() +"\n");
            }
        } catch (NumberFormatException ex ) {
            buffer.append(Bundles.getString("providenumber") +"\n");
        }

        if( buffer.toString().length() != 0 ) {
            throw new Exception(buffer.toString());
        }
    }

}
