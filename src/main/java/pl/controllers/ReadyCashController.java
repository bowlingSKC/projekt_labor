package pl.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import pl.Constant;
import pl.Main;
import pl.MessageBox;
import pl.jpa.SessionUtil;
import pl.model.Currency;
import pl.model.ReadyCash;

import java.util.Optional;

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
    private TableColumn<ReadyCash, Integer> inHufColumn;

    @FXML
    public void initialize() {
        initTransactionPane();
        initTablePane();
    }

    private void initTablePane() {
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
        transactionTypeComboBox.getItems().add("Kimenő");
        transactionTypeComboBox.getItems().add("Bemenő");
        currencyComboBox.getItems().setAll(Constant.getCurrencies());

        transactionTypeComboBox.getSelectionModel().select(0);
        currencyComboBox.getSelectionModel().select(0);
    }

    @FXML
    private void handleSave() {
        try {
            checkAllField();

            if( transactionTypeComboBox.getSelectionModel().getSelectedItem().equals("Kimenő") ) {
                handleOutTransactions();
            } else {
                handleInTransaction();
            }
            updateTableData();

            MessageBox.showInformationMessage("Sikeres", "Siker", "Hurrá", false);
        } catch (Throwable ex) {
            MessageBox.showErrorMessage("Hiba", "Nem lehet létrehozni a tranzakciót!", ex.getMessage(), false);
        }
    }

    private void updateTableData() {
        readyCashTableView.getItems().setAll(Main.getLoggedUser().getReadycash());
    }

    private void handleInTransaction() throws Exception {
        ReadyCash selected = new ReadyCash(Main.getLoggedUser(), 0.0f);
        boolean was = false;
        selected.setCurrency(currencyComboBox.getSelectionModel().getSelectedItem());
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
    }

    private void handleOutTransactions() throws Exception {
        if( Main.getLoggedUser().getReadycash() == null ) {
            throw new Exception("Nincs regisztrált készpénzed, így nem tudsz kimenő pénzt felvenni!");
        }

        ReadyCash readyCash = null;
        for( ReadyCash readyCash1 : Main.getLoggedUser().getReadycash() ) {
            if( readyCash1.getCurrency().equals(currencyComboBox.getSelectionModel().getSelectedItem()) ) {
                readyCash = readyCash1;
                break;
            }
        }

        if( readyCash == null ) {
            throw new Exception("A kiválasztott valutából nincs ");
        }

        if( readyCash.getMoney() < Float.valueOf(amountFiled.getText()) ) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Nincs elegendő pénzmennyiség");
            alert.setHeaderText("Nincs elég pénz a megadott valután.");
            alert.setContentText("Akarja az összes pénzt levenni a valutáról?");
            Optional<ButtonType> result = alert.showAndWait();
            if( result.get() == ButtonType.OK ) {
                transactionDelete(readyCash);
                Main.getLoggedUser().getReadycash().remove(readyCash);
            }
            return;
        }

        if( Float.compare(readyCash.getMoney() - Float.valueOf(amountFiled.getText()), 0) == 0 ) {
            transactionDelete(readyCash);
            Main.getLoggedUser().getReadycash().remove(readyCash);
        } else {
            readyCash.setMoney(readyCash.getMoney() - Float.valueOf(amountFiled.getText()));
            transactionSaveOrUpdate(readyCash);
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
                buffer.append("Nem lehet negatív számot megadni!\n");
            }
        } catch (NumberFormatException ex) {
            buffer.append("Csak számot lehet megadni a mezőben!\n");
        }

        if( buffer.toString().length() != 0 ) {
            throw new Exception(buffer.toString());
        }

    }
}
