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
import pl.model.Property;

import java.util.Date;
import java.util.Optional;

public class PropertiesController {

    @FXML
    private AnchorPane tablePane;
    @FXML
    private AnchorPane editPane;

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

    @FXML
    public void initialize() {
        loadPropertiesToTable();
        initPropertyTable();
        initTableLayout();
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

            Property property = new Property();
            property.setName(nameField.getText());
            property.setBought(Constant.dateFromLocalDate(datePicker.getValue()));
            property.setMoney(Float.valueOf(moneyField.getText()));
            property.setComment(commentField.getText());
            property.setOwner(Main.getLoggedUser());

            Session session = SessionUtil.getSession();
            Transaction tx = session.beginTransaction();
            session.saveOrUpdate(property);                 // ez nem oké, mert mindig újat hoz létre
            tx.commit();
            session.close();

            Main.getLoggedUser().getProperties().add(property);
            loadPropertiesToTable();
            handleBackToTablePane();
        } catch (Throwable ex) {
            MessageBox.showErrorMessage("Hiba", "A vagyonelemet nem lehet létrehozni!", ex.getMessage(), false);
        }
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
        editPane.setOpacity(0);
        new FadeInUpTransition(tablePane).play();
    }

    private void loadPropertyToEditPane(Property property) {
        nameField.setText( property.getName() );
        moneyField.setText( Float.toString(property.getMoney()) );
        datePicker.setValue(Constant.localDateFromDate(property.getBought()));
        commentField.setText( property.getComment() );
    }

    private class ButtonCell extends TableCell<Object, Boolean> {
        final Hyperlink cellButtonDelete = new Hyperlink(Bundles.getString("delete"));
        final Hyperlink cellButtonEdit = new Hyperlink(Bundles.getString("edit"));
        final Hyperlink cellButtonSell = new Hyperlink("Elad");
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

                }
            });

            cellButtonEdit.setOnAction((ActionEvent t) -> {
                int row = getTableRow().getIndex();
                loadPropertyToEditPane(propertyTableView.getItems().get(row));
                tablePane.setOpacity(0);
                new FadeInUpTransition(editPane).play();

            });

            cellButtonSell.setOnAction((ActionEvent t) -> {

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