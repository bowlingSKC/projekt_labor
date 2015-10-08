package pl.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import pl.Constant;
import pl.Main;
import pl.MessageBox;
import pl.jpa.SessionUtil;
import pl.model.Currency;
import pl.model.ReadyCash;

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
    private ReadyCash selectedReadyCash;

    @FXML
    public void initialize() {
        initTransactionPane();
        initTablePane();
    }

    private void initTablePane() {
        currencyColumn.setCellValueFactory(new PropertyValueFactory<>("currency"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("money"));

        readyCashTableView.getItems().setAll(Main.getLoggedUser().getReadycash());
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

            ReadyCash selected = getSelectedReadyCash();
            Session session = SessionUtil.getSession();
            Transaction tx = session.beginTransaction();
            session.saveOrUpdate(selected);
            tx.commit();
            session.close();

            MessageBox.showInformationMessage("Sikeres", "Siker", "Hurrá", false);
        } catch (Throwable ex) {
            ex.printStackTrace();
            MessageBox.showErrorMessage("Hiba", "Nem lehet létrehozni a tranzakciót!", ex.getMessage(), false);
        }
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

        for(ReadyCash readyCash : Main.getLoggedUser().getReadycash()) {
            if( readyCash.getCurrency().equals(currencyComboBox.getSelectionModel().getSelectedItem()) ) {
                if( Float.valueOf(amountFiled.getText()) > readyCash.getMoney() ) {
                    buffer.append("Nincs elég pénz ebből a valutából!\n");
                }
                break;
            }
        }

        if( buffer.toString().length() != 0 ) {
            throw new Exception(buffer.toString());
        }

    }

    public ReadyCash getSelectedReadyCash() {
        ReadyCash selected = new ReadyCash(Main.getLoggedUser(), 0.0f);
        selected.setCurrency(Constant.getHufCurrency());
        for(ReadyCash readyCash : Main.getLoggedUser().getReadycash()) {
            if( readyCash.getCurrency().equals(currencyComboBox.getSelectionModel().getSelectedItem()) ) {
                selected = readyCash;
            }
        }
        return selected;
    }
}
