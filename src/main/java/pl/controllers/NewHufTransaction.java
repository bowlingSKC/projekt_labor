package pl.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.hibernate.Query;
import org.hibernate.Session;
import pl.Main;
import pl.MessageBox;
import pl.jpa.SessionUtil;
import pl.model.Account;
import pl.model.Bank;
import pl.model.Transaction;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public class NewHufTransaction {

    @FXML
    private ComboBox<Account> accountComboBox;
    @FXML
    private TextField toAccountField;
    @FXML
    private Label bankLabel;
    @FXML
    private TextField moneyField;
    @FXML
    private TextField commentField;

    private List<Bank> banks;

    @FXML
    private void handleSubmit() {

        try {
            checkAllFields();
        } catch (Throwable ex) {
            MessageBox.showErrorMessage("Hiba", "A megbízást nem lehet létrehotni!", ex.getMessage(), false);
            return;
        }

        if( confirmSendMoney() ) {

            // 1. Le kell vonni a számláról az összeget
            // 2. Tranzakciót kell végrehajtani
            // 3. Ha a kedvezményezett szerepel a DB-ben akkor jóvá kell írni számára

            Session session = SessionUtil.getSession();
            org.hibernate.Transaction tx = session.beginTransaction();
            try {

                Account account = accountComboBox.getSelectionModel().getSelectedItem();

                Transaction transaction = new Transaction(account, toAccountField.getText(), -Float.valueOf(moneyField.getText()), new Date(), commentField.getText());
                session.save(transaction);

                Query query = session.createQuery("from Account where accountNumber = :toAccNum");
                query.setParameter("toAccNum", toAccountField.getText());
                Account toAccount = (Account) query.uniqueResult();
                if( toAccount != null ) {
                    Transaction transaction1 = new Transaction(account, account.getAccountNumber(), Float.valueOf(moneyField.getText()), new Date(), commentField.getText());
                    session.save(transaction1);
                    toAccount.setMoney( toAccount.getMoney() + Float.valueOf(moneyField.getText()) );
                    toAccount.getFromTransactions().add(transaction1);
                    session.update(toAccount);
                }

                account.setMoney( account.getMoney() - Float.valueOf(moneyField.getText()) );
                account.getFromTransactions().add(transaction);
                session.update(account);

                tx.commit();
            } catch (Throwable ex) {
                tx.rollback();
                ex.printStackTrace();
            }

        }

    }

    private boolean confirmSendMoney() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Meger?sítés");
        alert.setHeaderText("Biztosan végre akarod hajtani a tranzakciót?");
        alert.setContentText("Kedvezményezett: " + toAccountField.getText() + "\nÖsszeg: " + moneyField.getText() + " forint\n" +
                "Tranzakció után maradó összeg: " + (accountComboBox.getSelectionModel().getSelectedItem().getMoney() - Float.valueOf(moneyField.getText())) + " forint");
        Optional<ButtonType> result = alert.showAndWait();
        if( result.get() == ButtonType.OK ) {
            return true;
        }
        return false;
    }

    private void checkAllFields() throws Exception {
        StringBuffer buffer = new StringBuffer();

        if( accountComboBox.getSelectionModel().getSelectedItem() == null ) {
            buffer.append("Nem választottál ki számlát!\n");
        }

        if( toAccountField.getText().length() != 24 ) {
            buffer.append("A bankszámlaszámnak 24 számnak kell lennie!\n");
        }

        try {
            float money = Float.valueOf(moneyField.getText());
            if( money < 0 ) {
                buffer.append("Nem küldhetsz 0-nál kisebb összeget!\n");
            }
            if( accountComboBox.getSelectionModel().getSelectedItem() != null && money > accountComboBox.getSelectionModel().getSelectedItem().getMoney()  ) {
                buffer.append("Nincs elég pénz a számládon!\n");
            }
        } catch (NumberFormatException ex) {
            buffer.append("Csak számot írhatsz a bankszámlaszámhoz!\n");
        }

        if( buffer.toString().length() != 0 ) {
            throw new Exception( buffer.toString() );
        }
    }


    @FXML
    public void initialize() {

        // Bankok lekérdezése
        Session session = SessionUtil.getSession();
        Query query = session.createQuery("from Bank");
        banks = query.list();
        session.close();

        // Számlák lekérdezése
        accountComboBox.getItems().setAll(Main.getLoggedUser().getAccounts() );

        // Bank felismerése
        bankLabel.setText("");
        toAccountField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if( !newValue ) {
                    boolean found = false;
                    String giro = toAccountField.getText().substring(0, 3);
                    for( Bank bank : banks ) {
                        if( giro.equals(bank.getGiro()) ) {
                            bankLabel.setText( bank.getName() );
                            found = true;
                            return;
                        }
                    }
                    if( !found ) {
                        bankLabel.setText( "Ismeretlen bank" );
                    }
                }
            }
        });

    }

}
