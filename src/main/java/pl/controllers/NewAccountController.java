package pl.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.hibernate.Query;
import org.hibernate.Session;
import pl.Main;
import pl.MessageBox;
import pl.jpa.SessionUtil;
import pl.model.Account;
import pl.model.Bank;
import pl.model.Transaction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class NewAccountController {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @FXML
    private ComboBox<Bank> bankComboBox;
    @FXML
    private TextField accountNumberField;
    @FXML
    private TextField accountNameField;
    @FXML
    private TextField accountMoneyField;
    @FXML
    private ComboBox<String> accountDevComboBox;
    @FXML
    private TextField accountCreatedField;

    @FXML
    private void handleSave() {

        try {
            checkAllFields();
            if( !checkGiroCode() ) {
                return;
            }

            Account account = new Account(accountNumberField.getText(), accountNameField.getText(), Float.valueOf(accountMoneyField.getText()), sdf.parse( accountCreatedField.getText() ),
                    Main.getLoggedUser(), bankComboBox.getSelectionModel().getSelectedItem());
            Main.getLoggedUser().getAccounts().add(account);

            Session session = SessionUtil.getSession();
            org.hibernate.Transaction tx = session.beginTransaction();
            session.save(account);
            tx.commit();
            session.close();

            MessageBox.showInformationMessage("Új számla", "Az új számla sikeresen létre lett hozva!", "", false);
        } catch (Throwable ex) {
            MessageBox.showErrorMessage("Hiba", "A számlát nem lehet létrehozni!", ex.getMessage(), false);
        }
    }

    private boolean checkGiroCode() {
        if(accountNumberField.getText().length() > 3) {
            String userGiro = accountNumberField.getText().substring(0, 3);
            Session session = SessionUtil.getSession();
            Query query = session.createQuery("from Bank where giro = :giro");
            query.setParameter("giro", userGiro);
            Bank bank = (Bank) query.uniqueResult();
            session.close();
            if( (bank != null) && (!accountNumberField.getText().substring(0, 2).equals(bankComboBox.getSelectionModel().getSelectedItem().getGiro())) ) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Figyelmeztetés");
                alert.setHeaderText("A kiválaszott bank és a megadott bankszámlaszám nem egyezik.\nBiztosan így akarja elmenteni?");
                alert.setContentText("A rendszer ehhez a számlaszámhoz a " + bank.getName() + " bankot ajánlja.");

                Optional<ButtonType> result = alert.showAndWait();
                if( result.get() == ButtonType.OK ) {
                    return true;
                }
            }
        }

        return false;
    }

    private void checkAllFields() throws Exception {
        StringBuffer buffer = new StringBuffer();

        if( accountNumberField.getText().length() != 24 ) {
            buffer.append("A folyószámla számának 24 bet?b?l kell állnia!\n");
        }

        try {
            float money = Float.valueOf( accountMoneyField.getText() );
            if( money < 0 ) {
                buffer.append("Nem kezdhetsz 0-nál kisebb összeggel!\n");
            }
        } catch (NumberFormatException ex) {
            buffer.append("Összegnek csak számot lehet beírni!\n");
        }

        // TODO: több vizsgálat

        if( buffer.toString().length() != 0 ) {
            throw new Exception(buffer.toString());
        }
    }

    @FXML
    public void initialize() {
        // Bankok feltöltése
        Session session = SessionUtil.getSession();
        Query query = session.createQuery("from Bank ORDER BY name ASC");
        @SuppressWarnings("unchecked")
        List<Bank> banks = query.list();
        session.close();
        bankComboBox.getItems().setAll(banks);
        bankComboBox.getSelectionModel().select(0);

        // Dátum beillesztése
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        accountCreatedField.setText( sdf.format(new Date()) );

        // Pénznemek feltöltése
        accountDevComboBox.getItems().add("HUF");
        accountDevComboBox.getSelectionModel().select("HUF");
    }

}