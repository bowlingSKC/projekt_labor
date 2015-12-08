package pl.controllers;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import pl.Constant;
import pl.Main;
import pl.MessageBox;
import pl.animations.FadeInUpTransition;
import pl.bundles.Bundles;
import pl.dao.PropertyDao;
import pl.jpa.SessionUtil;
import pl.model.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class PropertiesController {

    private Property editedProperty = null;

    @FXML
    private AnchorPane tablePane;
    @FXML
    private AnchorPane editPane;
    @FXML
    private AnchorPane sellPane;
    @FXML
    private AnchorPane newPane;

    @FXML
    private TableView<Property> propertyTableView;
    @FXML
    private TableColumn<Property, String> nameColumn;
    @FXML
    private TableColumn<Property, Float> moneyColumn;
    @FXML
    private TableColumn<Property, Date> dateTableColumn;
    @FXML
    private TableColumn actionsColumn;

    @FXML
    private TextField nameField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField commentField;
    @FXML
    private TextField newValueValueTextField;
    @FXML
    private DatePicker newValueDatePicker;
    @FXML
    private TextField newValueCommentField;
    @FXML
    private TableView<PropertyValue> propertyValueTable;
    @FXML
    private TableColumn<PropertyValue, Date> propertyValueDateTableColumn;
    @FXML
    private TableColumn<PropertyValue, Float> propertyValueFloatTableColumn;
    @FXML
    private TableColumn<PropertyValue, String> propertyValueStringTableColumn;


    // ====== SELL PANE ======
    @FXML
    private Label sellPropertyNameLabel;
    @FXML
    private TextField sellPropertyValueField;
    @FXML
    private ComboBox<Currency> sellProCurrencyComboBoxComboBox;
    @FXML
    private DatePicker sellPropertyDate;
    @FXML
    private ComboBox<String> sellPropertyTypeBox;
    @FXML
    private ComboBox<Account> sellPropertyAccounts;
    // ====== SELL PANE VÉGE ======

    // ====== NEW PANE ======
    @FXML
    private TextField newNameField;
    @FXML
    private DatePicker newDatePicker;
    @FXML
    private TextField newMoneyField;
    @FXML
    private TextField newCommentField;
    // ====== NEW PANE VÉGE ======

    @FXML
    public void initialize() {
        loadPropertiesToTable();
        initPropertyTable();
        initTableLayout();
        initNewValuePane();

        initSellPane();
    }

    private void initNewValuePane() {
        propertyValueDateTableColumn.setCellFactory(new Callback<TableColumn<PropertyValue, Date>, TableCell<PropertyValue, Date>>() {
            @Override
            public TableCell<PropertyValue, Date> call(TableColumn<PropertyValue, Date> param) {
                return new TableCell<PropertyValue, Date>() {
                    @Override
                    protected void updateItem(Date item, boolean empty) {
                        super.updateItem(item, empty);

                        if( item != null && !empty ) {
                            setText(Constant.getDateFormat().format(item));
                        } else {
                            setText("");
                        }
                    }
                };
            }
        });
        propertyValueDateTableColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        propertyValueFloatTableColumn.setCellFactory(new Callback<TableColumn<PropertyValue, Float>, TableCell<PropertyValue, Float>>() {
            @Override
            public TableCell<PropertyValue, Float> call(TableColumn<PropertyValue, Float> param) {
                return new TableCell<PropertyValue, Float>() {
                    @Override
                    protected void updateItem(Float item, boolean empty) {
                        super.updateItem(item, empty);

                        if( item != null && !empty ) {
                            setText( Constant.getNumberFormat().format(item) );
                        } else {
                            setText("");
                        }
                    }
                };
            }
        });
        propertyValueFloatTableColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

        propertyValueStringTableColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
    }

    private void initSellPane() {
        sellProCurrencyComboBoxComboBox.getItems().setAll(Constant.getCurrencies());
        sellProCurrencyComboBoxComboBox.getSelectionModel().select(Constant.getHufCurrency());
        sellPropertyTypeBox.getItems().add(Bundles.getString("readycash"));
        sellPropertyTypeBox.getItems().add(Bundles.getString("account"));
        sellPropertyAccounts.setVisible(false);
        sellPropertyTypeBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if( newValue.equals(Bundles.getString("account")) ) {
                sellPropertyAccounts.setVisible(true);
            } else {
                sellPropertyAccounts.setVisible(false);
            }
        });
        sellPropertyAccounts.getItems().setAll(Main.getLoggedUser().getAccounts());
    }

    private void initTableLayout() {
        tablePane.setOpacity(0);
        new FadeInUpTransition(tablePane).play();
    }

    private void loadPropertiesToTable() {
        propertyTableView.getItems().setAll(Main.getLoggedUser().getProperties());
    }

    private void initPropertyTable() {

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        moneyColumn.setCellFactory(new Callback<TableColumn<Property, Float>, TableCell<Property, Float>>() {
            @Override
            public TableCell<Property, Float> call(TableColumn<Property, Float> param) {
                return new TableCell<Property, Float>() {
                    @Override
                    protected void updateItem(Float item, boolean empty) {
                        super.updateItem(item, empty);
                        try {
                            if( this.getTableRow() != null ) {
                                Property property = propertyTableView.getItems().get( this.getTableRow().getIndex() );
                                setText( Constant.getNumberFormat().format(property.getLatestValue()) );
                            } else {
                                setText("");
                            }
                        } catch (Exception ex) {
                            // nem kell semmit sem csinalni
                        }
                    }
                };
            }
        });

        dateTableColumn.setCellValueFactory(new PropertyValueFactory<>("bought"));

        dateTableColumn.setCellFactory(cell -> new TableCell<Property, Date>() {
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

        actionsColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Property, Boolean>, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<Property, Boolean> param) {
                return new SimpleBooleanProperty(param.getValue() != null);
            }
        });

        actionsColumn.setCellFactory(param -> new ButtonCell(propertyTableView));
    }

    @FXML
    private void handleNewProperty() {
        tablePane.setOpacity(0);
        new FadeInUpTransition(newPane).play();
    }

    @FXML
    private void handleSaveProperty() {
        if( editedProperty == null ) {
            saveNewPropertyToDatabase();
        } else {
            updatePropertyInDatabase();
        }

        loadPropertiesToTable();
        handleBackToTablePane();
    }

    @FXML
    private void newValueSubmitButton() {
        try {
            checkNewValueFields();

            PropertyValue value = new PropertyValue();
            value.setProperty(editedProperty);
            value.setComment(newValueCommentField.getText());
            value.setValue(Float.valueOf(newValueValueTextField.getText()));
            if( newValueDatePicker.getValue() != null ) {
                value.setDate(Constant.dateFromLocalDate(newValueDatePicker.getValue()));
            } else {
                value.setDate(new Date());
            }

            editedProperty.getValues().add(value);

            refreshValuesTable();

            Session session = SessionUtil.getSession();
            Transaction tx = session.beginTransaction();
            session.save(value);
            tx.commit();
            session.close();

        } catch (Exception e) {
            MessageBox.showErrorMessage(Bundles.getString("error.nodb.title"), Bundles.getString("error.processing"), e.getMessage(), false);
        }
    }

    private void refreshValuesTable() {
        propertyValueTable.getItems().setAll(editedProperty.getValues());
    }

    private void checkNewValueFields() throws Exception {
        StringBuffer buffer = new StringBuffer();

        try {
            if( Float.valueOf(newValueValueTextField.getText()) < 0 ) {
                buffer.append(Bundles.getString("gtzero") +"\n");
            }
        } catch (NumberFormatException ex) {
            buffer.append(Bundles.getString("providenumber") +"\n");
        }

        if( newValueDatePicker.getValue() != null ) {
            if( Constant.dateFromLocalDate(newValueDatePicker.getValue()).after(new Date()) ) {
                buffer.append(Bundles.getString("dateafter") +"\n");
            }
        }

        if( buffer.toString().length() != 0 ) {
            throw new Exception(buffer.toString());
        }
    }

    private void updatePropertyInDatabase() {
        Session session = SessionUtil.getSession();
        Transaction tx = session.beginTransaction();
        session.update(editedProperty);
        tx.commit();
        session.close();
    }

    private void saveNewPropertyToDatabase() {
        try {
            checkAllField();

            Property property = new Property();
            property.setName(newNameField.getText());
            if(newDatePicker.getValue() != null) {
                property.setBought(Constant.dateFromLocalDate(newDatePicker.getValue()));
            } else {
                property.setBought(new Date());
            }
            property.setComment(newCommentField.getText());
            property.setOwner(Main.getLoggedUser());

            PropertyValue value = new PropertyValue(property, Float.valueOf(newMoneyField.getText()), Constant.dateFromLocalDate(newDatePicker.getValue()), null);
            property.getValues().add(value);

            Session session = SessionUtil.getSession();
            Transaction tx = session.beginTransaction();
            session.saveOrUpdate(property);
            session.save(value);
            tx.commit();
            session.close();

            Main.getLoggedUser().getProperties().add(property);
        } catch (Throwable ex) {
            MessageBox.showErrorMessage(Bundles.getString("error.nodb.title"), Bundles.getString("error.processing"), ex.getMessage(), false);
        }
    }

    @FXML
    private void handleSellProperty() {

        Session session = SessionUtil.getSession();
        Transaction tx = session.beginTransaction();

        Property property = propertyTableView.getSelectionModel().getSelectedItem();

        if( sellPropertyTypeBox.getSelectionModel().getSelectedItem().equals(Bundles.getString("account")) ) {
            AccountTransaction accountTransaction = new AccountTransaction();
            accountTransaction.setMoney(Float.valueOf(sellPropertyValueField.getText()));
            accountTransaction.setAccount(sellPropertyAccounts.getSelectionModel().getSelectedItem());
            accountTransaction.setDate(Constant.dateFromLocalDate(sellPropertyDate.getValue()));
            accountTransaction.setType(Constant.getTransactionTypes().get(Constant.getTransactionTypes().size() - 1));
            accountTransaction.setComment("Vagyontargy eladasabol szarmazo jovedelem: " + property.getName() + "");
            accountTransaction.setCurrency(Constant.getHufCurrency());
            accountTransaction.setBeforeAccountTransaction( sellPropertyAccounts.getSelectionModel().getSelectedItem().getLatestTransaction() );

            Account selected = sellPropertyAccounts.getSelectionModel().getSelectedItem();
            selected.setMoney(selected.getMoney() + Float.valueOf(sellPropertyValueField.getText()));

            session.update(selected);
            session.save(accountTransaction);

            selected.getAccountTransactions().add(accountTransaction);
            Main.getLoggedUser().getProperties().remove(property);
        } else {
            ReadyCash readyCash = null;
            for( ReadyCash tmp : Main.getLoggedUser().getReadycash() ) {
                if( tmp.getCurrency().getCode().equals( sellProCurrencyComboBoxComboBox.getSelectionModel().getSelectedItem().getCode() ) ) {
                    readyCash = tmp;
                    break;
                }
            }

            if( readyCash != null ) {
                readyCash.setMoney( readyCash.getMoney() + Float.valueOf(sellPropertyValueField.getText()) );
                session.update(readyCash);
            } else {
                readyCash = new ReadyCash();
                readyCash.setMoney( readyCash.getMoney() + Float.valueOf(sellPropertyValueField.getText()) );
                readyCash.setOwner(Main.getLoggedUser());
                readyCash.setCurrency( sellProCurrencyComboBoxComboBox.getSelectionModel().getSelectedItem() );

                Main.getLoggedUser().getReadycash().add(readyCash);
                session.save(readyCash);
            }
            Main.getLoggedUser().getProperties().remove(property);
        }

        while( property.getValues().size() != 0 ) {
            PropertyValue value = property.getLatestPropertyValue();
            property.getValues().remove(value);
            session.delete(value);
        }
        session.delete(property);

        tx.commit();
        session.close();


        handleBackToTablePane();
    }

    private void checkAllField() throws Exception {
        StringBuffer buffer = new StringBuffer();

        if( newNameField.getText().length() == 0 ) {
            buffer.append(Bundles.getString("providename") +"\n");
        }

        try {
            if( Float.valueOf(newMoneyField.getText()) < 1 ) {
                buffer.append(Bundles.getString("gtzero") +"\n");
            }
        } catch (NumberFormatException ex) {
            buffer.append(Bundles.getString("providenumber") +"\n");
        }

        if( buffer.toString().length() != 0 ) {
            throw new Exception(buffer.toString());
        }
    }

    @FXML
    private void handleBackToTablePane() {
        editedProperty = null;
        setUnvisibleAllPanes();
        updateTableItems();
        new FadeInUpTransition(tablePane).play();
    }

    private void updateTableItems() {
        propertyTableView.getItems().setAll(Main.getLoggedUser().getProperties());
    }

    private void setUnvisibleAllPanes() {
        editPane.setOpacity(0);
        sellPane.setOpacity(0);
        newPane.setOpacity(0);
    }

    private void loadPropertyToEditPane(Property property) {
        editedProperty = property;
        nameField.setText( property.getName() );
        datePicker.setValue(Constant.localDateFromDate(property.getBought()));
        commentField.setText( property.getComment() );

        refreshValuesTable();
    }

    private class ButtonCell extends TableCell<Object, Boolean> {
        final Hyperlink cellButtonDelete = new Hyperlink(Bundles.getString("delete"));
        final Hyperlink cellButtonEdit = new Hyperlink(Bundles.getString("edit"));
        final Hyperlink cellButtonSell = new Hyperlink(Bundles.getString("sell"));
        final HBox hb = new HBox(cellButtonDelete, cellButtonEdit, cellButtonSell);

        ButtonCell(final TableView tblView) {
            hb.setSpacing(4);
            cellButtonDelete.setOnAction((ActionEvent t) -> {
                int row = getTableRow().getIndex();
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(Bundles.getString("sure"));
                alert.setHeaderText(Bundles.getString("deleteprop"));
                alert.setContentText(Bundles.getString("deleteloss"));
                Optional<ButtonType> result = alert.showAndWait();
                if( result.get() == ButtonType.OK ) {
                    PropertyDao.deleteProperty( (Property) tblView.getItems().get(row));
                    Main.getLoggedUser().getProperties().remove( (Property) tblView.getItems().get(row) );
                    updateTableItems();
                }
            });

            cellButtonEdit.setOnAction((ActionEvent t) -> {
                int row = getTableRow().getIndex();
                loadPropertyToEditPane(propertyTableView.getItems().get(row));
                tablePane.setOpacity(0);
                new FadeInUpTransition(editPane).play();
            });

            cellButtonSell.setOnAction((ActionEvent t) -> {
                Property selected = propertyTableView.getItems().get( getTableRow().getIndex() );
                sellPropertyNameLabel.setText( selected.getName() );
                sellPropertyValueField.setText( selected.getLatestValue() + "" );
                propertyTableView.getSelectionModel().select(selected);
                tablePane.setOpacity(0);
                new FadeInUpTransition(sellPane).play();
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
            FileChooser.ExtensionFilter extFilter1 =
                    new FileChooser.ExtensionFilter("CSV file (*.csv)", "*.csv");
            fileChooser.getExtensionFilters().add(extFilter1);
            FileChooser.ExtensionFilter extFilter2 = new FileChooser.ExtensionFilter("Excel file (*.xls)", "*.xls");
            fileChooser.getExtensionFilters().add(extFilter2);
            File file = fileChooser.showSaveDialog(Main.getPrimaryStage());
            if(file != null && fileChooser.getSelectedExtensionFilter() == extFilter1){

                writer = new FileWriter(file);
                writer.append("Name;Money;Bought date\n");
                for(int i = 0; i < propertyTableView.getItems().size(); i++){
                    writer.append(propertyTableView.getItems().get(i).getName());
                    writer.append(';');
                    writer.append(String.valueOf(propertyTableView.getItems().get(i).getLatestValue()));
                    writer.append(';');
                    writer.append(propertyTableView.getItems().get(i).getBought().toString());
                    /*writer.append(';');
                    writer.append(propertyTableView.getItems().get(i).getComment());
                    writer.append(';');
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
                writer.append("Name\tMoney\tBought date\n");
                for(int i = 0; i < propertyTableView.getItems().size(); i++){
                    writer.append(propertyTableView.getItems().get(i).getName());
                    writer.append('\t');
                    writer.append(String.valueOf(propertyTableView.getItems().get(i).getLatestValue()));
                    writer.append('\t');
                    writer.append(propertyTableView.getItems().get(i).getBought().toString());
                    /*writer.append(';');
                    writer.append(propertyTableView.getItems().get(i).getComment());
                    writer.append(';');
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

}
