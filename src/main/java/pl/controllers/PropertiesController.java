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
import org.hibernate.Session;
import org.hibernate.Transaction;
import pl.Constant;
import pl.Main;
import pl.MessageBox;
import pl.animations.FadeInUpTransition;
import pl.bundles.Bundles;
import pl.dao.PropertyDao;
import pl.jpa.SessionUtil;
import pl.model.Account;
import pl.model.Currency;
import pl.model.Property;
import pl.model.ReadyCash;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
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
    private TextField moneyField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField commentField;

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

    @FXML
    public void initialize() {
        loadPropertiesToTable();
        initPropertyTable();
        initTableLayout();

        initSellPane();
    }

    private void initSellPane() {
        sellProCurrencyComboBoxComboBox.getItems().setAll(Constant.getCurrencies());
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
        moneyColumn.setCellValueFactory(new PropertyValueFactory<>("money"));
        dateTableColumn.setCellValueFactory(new PropertyValueFactory<>("bought"));

        moneyColumn.setCellFactory( cell -> new TableCell<Property, Float>() {
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
        new FadeInUpTransition(editPane).play();
        loadPropertyToEditPane(new Property());
    }

    @FXML
    private void handleSaveProperty() {
        try {
            checkAllField();

            if( editedProperty == null ) {
                editedProperty = new Property();
            }

            editedProperty.setName(nameField.getText());
            editedProperty.setBought(Constant.dateFromLocalDate(datePicker.getValue()));
            editedProperty.setMoney(Float.valueOf(moneyField.getText()));
            editedProperty.setComment(commentField.getText());
            editedProperty.setOwner(Main.getLoggedUser());

            Session session = SessionUtil.getSession();
            Transaction tx = session.beginTransaction();
            session.saveOrUpdate(editedProperty);
            tx.commit();
            session.close();

            if( Main.getLoggedUser().getProperties().contains(editedProperty) ) {   // TODO: bug
                Main.getLoggedUser().getProperties().add(editedProperty);
            }

            loadPropertiesToTable();
            handleBackToTablePane();
        } catch (Throwable ex) {
            MessageBox.showErrorMessage("Hiba", "A vagyonelemet nem lehet létrehozni!", ex.getMessage(), false);
        }
    }

    @FXML
    private void handleSellProperty() {

        Session session = SessionUtil.getSession();
        Transaction tx = session.beginTransaction();

        Property property = propertyTableView.getSelectionModel().getSelectedItem();

        if( sellPropertyTypeBox.getSelectionModel().getSelectedItem().equals(Bundles.getString("account")) ) {
            pl.model.Transaction transaction = new pl.model.Transaction();
            transaction.setMoney(Float.valueOf(sellPropertyValueField.getText()));
            transaction.setAccount(sellPropertyAccounts.getSelectionModel().getSelectedItem());
            transaction.setDate(Constant.dateFromLocalDate(sellPropertyDate.getValue()));
            transaction.setBeforeMoney(5.0f);
            transaction.setType(Constant.getTransactionTypes().get(Constant.getTransactionTypes().size() - 1));
            transaction.setComment("Vagyontargy eladasabol szarmazo jovedelem: " + property.getName() + "");

            Account selected = sellPropertyAccounts.getSelectionModel().getSelectedItem();
            selected.setMoney(selected.getMoney() + Float.valueOf(sellPropertyValueField.getText()));

            session.update(selected);
            session.save(transaction);

            selected.getTransactions().add(transaction);
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
        }

        session.delete(property);

        tx.commit();
        session.close();


        handleBackToTablePane();
    }

    private void checkAllField() throws Exception {
        StringBuffer buffer = new StringBuffer();

        if( nameField.getText().length() == 0 ) {
            buffer.append("Kötelező nevet adni a vagyonelemnek!\n");
        }

        try {
            if( Float.valueOf(moneyField.getText()) <= 0 ) {
                buffer.append("A vagyonelem értéke nem lehet 0 vagy annál kisebb értékű!\n");
            }
        } catch (NumberFormatException ex) {
            buffer.append("Az érték mezőbe csak számot lehet beírni!\n");
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
    }

    private void loadPropertyToEditPane(Property property) {
        editedProperty = property;
        nameField.setText( property.getName() );
        moneyField.setText( Float.toString(property.getMoney()) );
        datePicker.setValue(Constant.localDateFromDate(property.getBought()));
        commentField.setText( property.getComment() );
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
                alert.setTitle("Biztos benne?");
                alert.setHeaderText("Biztosan törölni szeretné a kiválasztott vagyontárgyat?");
                alert.setContentText("A törlés következtében az adat elveszik.");
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
                propertyTableView.getSelectionModel().select( selected );
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
            FileChooser.ExtensionFilter extFilter =
                    new FileChooser.ExtensionFilter("CSV file (*.csv)", "*.csv");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showSaveDialog(Main.getPrimaryStage());
            if(file != null){

                writer = new FileWriter(file);
                writer.append("Name;Money;Bought date\n");
                for(int i = 0; i < propertyTableView.getItems().size(); i++){
                    writer.append(propertyTableView.getItems().get(i).getName());
                    writer.append(';');
                    writer.append(String.valueOf(propertyTableView.getItems().get(i).getMoney()));
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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
