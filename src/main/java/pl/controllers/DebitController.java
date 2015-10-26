package pl.controllers;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.hibernate.Session;
import org.hibernate.Transaction;
import pl.Constant;
import pl.Main;
import pl.MessageBox;
import pl.animations.FadeInUpTransition;
import pl.bundles.Bundles;
import pl.jpa.SessionUtil;
import pl.model.Currency;
import pl.model.Debit;

import javax.xml.soap.Text;
import java.time.LocalDate;
import java.util.Date;

public class DebitController {

    @FXML
    private AnchorPane tablePane;
    @FXML
    private AnchorPane editPane;

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
    public void initialize() {

        new FadeInUpTransition(tablePane).play();

        initTablePane();
        initEditPane();
    }

    private void initEditPane() {
        currencyComboBox.getItems().setAll(Constant.getCurrencies());
    }

    private void initTablePane() {
        whoColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("money"));
        currencyColumn.setCellValueFactory(new PropertyValueFactory<>("currency"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("deadline"));

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
        updateTableItems();
        editPane.setOpacity(0);
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
            MessageBox.showErrorMessage("Hiba", "A tartozást nem lehet elmeneteni!", ex.getMessage(), false);
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
            builder.append("Kedvezményezett kitöltése kötelető!\n");
        }

        try {
            if( Float.valueOf(moneyField.getText()) < 0 ) {
                builder.append("0-nál nagyobb számot lehet megani az összeg mezőben!\n");
            }
        } catch (NumberFormatException ex) {
            builder.append("Csak számot lehet beírni az összeg mezőbe!\n");
        }

        if( currencyComboBox.getSelectionModel().getSelectedItem() == null ) {
            builder.append("Valuta kiválasztása kötelező!\n");
        }

        if( deadlinePicker.getValue() != null ) {
            if( deadlinePicker.getValue().isBefore(LocalDate.now()) ) {
                builder.append("A határidő nem lehet a mai napnál korábban!\n");
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
        final HBox hbox = new HBox(editLink, deleteLink);

        ButtonCell(final TableView tbl) {
            hbox.setSpacing(4);

            editLink.setOnAction((ActionEvent event) -> {

            });

            deleteLink.setOnAction((ActionEvent event) -> {

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

}