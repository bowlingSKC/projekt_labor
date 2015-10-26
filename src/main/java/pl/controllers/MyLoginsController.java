package pl.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import pl.Constant;
import pl.Main;
import pl.bundles.Bundles;
import pl.model.Login;

import java.util.Date;

public class MyLoginsController {

    @FXML
    private TableView<Login> loginTableView;
    @FXML
    private TableColumn<Login, Date> dateTableColumn;
    @FXML
    private TableColumn<Login, Boolean> successTableColumn;
    @FXML
    private TableColumn<Login, String> ipTableColumn;

    @FXML
    public void initialize() {

        loginTableView.setRowFactory(row -> new TableRow<Login>() {
            @Override
            protected void updateItem(Login item, boolean empty) {
                super.updateItem(item, empty);
                if( item != null & !empty ) {
                    if( !item.isSuccess() ) {
                        setStyle("-fx-background-color: lightcoral;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        dateTableColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateTableColumn.setCellFactory(col -> new TableCell<Login, Date>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if( item != null && !empty ) {
                    setText(Constant.getDateTimeFormat().format(item));
                } else {
                    setText("");
                }
            }
        });

        successTableColumn.setCellValueFactory(new PropertyValueFactory<>("success"));
        successTableColumn.setCellFactory(col -> new TableCell<Login, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if( item != null && !empty ) {
                    if( item ) {
                        setText(Bundles.getString("yes"));
                    } else {
                        setText(Bundles.getString("no"));
                    }
                } else {
                    setText("");
                }
            }
        });

        ipTableColumn.setCellValueFactory(new PropertyValueFactory<>("ip"));

        loginTableView.getItems().setAll(Main.getLoggedUser().getLogins());
    }

}
