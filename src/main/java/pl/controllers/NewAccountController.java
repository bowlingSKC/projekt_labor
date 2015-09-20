package pl.controllers;

import javafx.fxml.FXML;
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

            Account account = new Account(accountNumberField.getText(), accountNameField.getText(), Float.valueOf(accountMoneyField.getText()), sdf.parse( accountCreatedField.getText() ),
                    Main.getLoggedUser(), bankComboBox.getSelectionModel().getSelectedItem());
            Main.getLoggedUser().getAccounts().add(account);

            Session session = SessionUtil.getSession();
            org.hibernate.Transaction tx = session.beginTransaction();
            session.save(account);
            tx.commit();
            session.close();

            MessageBox.showInformationMessage("�j sz�mla", "Az �j sz�mla sikeresen l�tre lett hozva!", "", false);
        } catch (Throwable ex) {
            MessageBox.showErrorMessage("Hiba", "A sz�ml�t nem lehet l�trehozni!", ex.getMessage(), false);
        }
    }

    private void checkAllFields() throws Exception {
        StringBuffer buffer = new StringBuffer();

        if( accountNumberField.getText().length() != 24 ) {
            buffer.append("A foly�sz�mla sz�m�nak 24 bet?b?l kell �llnia!\n");
        }

        try {
            float money = Float.valueOf( accountMoneyField.getText() );
            if( money < 0 ) {
                buffer.append("Nem kezdhetsz 0-n�l kisebb �sszeggel!\n");
            }
        } catch (NumberFormatException ex) {
            buffer.append("�sszegnek csak sz�mot lehet be�rni!\n");
        }

        // TODO: t�bb vizsg�lat

        if( buffer.toString().length() != 0 ) {
            throw new Exception(buffer.toString());
        }
    }

    @FXML
    public void initialize() {
        // Bankok felt�lt�se
        Session session = SessionUtil.getSession();
        Query query = session.createQuery("from Bank ORDER BY name ASC");
        @SuppressWarnings("unchecked")
        List<Bank> banks = query.list();
        session.close();
        bankComboBox.getItems().setAll(banks);
        bankComboBox.getSelectionModel().select(0);

        // D�tum beilleszt�se
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        accountCreatedField.setText( sdf.format(new Date()) );

        // P�nznemek felt�lt�se
        accountDevComboBox.getItems().add("HUF");
    }


}
