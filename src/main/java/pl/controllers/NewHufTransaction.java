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

import java.time.LocalDate;
import java.time.ZoneId;
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
    @FXML
    private DatePicker dateField;

    private List<Bank> banks;

    @FXML
    private void handleSubmit() {

        try {
            checkAllFields();
        } catch (Throwable ex) {
            MessageBox.showErrorMessage("Hiba", "A megb�z�st nem lehet l�trehotni!", ex.getMessage(), false);
            return;
        }

        if( confirmSendMoney() ) {

            // 1. Le kell vonni a sz�ml�r�l az �sszeget
            // 2. Tranzakci�t kell v�grehajtani
            // 3. Ha a kedvezm�nyezett szerepel a DB-ben akkor j�v� kell �rni sz�m�ra

            Session session = SessionUtil.getSession();
            org.hibernate.Transaction tx = session.beginTransaction();
            try {

                Account account = accountComboBox.getSelectionModel().getSelectedItem();

                Date mydate = Date.from(dateField.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                Transaction transaction = new Transaction(account, toAccountField.getText(), -Float.valueOf(moneyField.getText()), mydate, commentField.getText());
                session.save(transaction);

                Query query = session.createQuery("from Account where accountNumber = :toAccNum");
                query.setParameter("toAccNum", toAccountField.getText());
                Account toAccount = (Account) query.uniqueResult();
                if( toAccount != null ) {
                    Date mydate2 = Date.from(dateField.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                    Transaction transaction1 = new Transaction(account, account.getAccountNumber(), Float.valueOf(moneyField.getText()), mydate2, commentField.getText());
                    session.save(transaction1);
                    toAccount.setMoney(toAccount.getMoney() + Float.valueOf(moneyField.getText()));
                    toAccount.getFromTransactions().add(transaction1);
                    session.update(toAccount);
                }

                account.setMoney( account.getMoney() - Float.valueOf(moneyField.getText()) );
                account.getFromTransactions().add(transaction);
                session.update(account);

                tx.commit();
                //System.out.println("OK");

            } catch (Throwable ex) {
                tx.rollback();
                ex.printStackTrace();
            }
            session.close();

        }

    }

    private boolean confirmSendMoney() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Meger?s�t�s");
        alert.setHeaderText("Biztosan v�gre akarod hajtani a tranzakci�t?");
        alert.setContentText("Kedvezm�nyezett: " + toAccountField.getText() + "\n�sszeg: " + moneyField.getText() + " forint\n" +
                "Tranzakci� ut�n marad� �sszeg: " + (accountComboBox.getSelectionModel().getSelectedItem().getMoney() - Float.valueOf(moneyField.getText())) + " forint");
        Optional<ButtonType> result = alert.showAndWait();
        if( result.get() == ButtonType.OK ) {
            return true;
        }
        return false;
    }

    private void checkAllFields() throws Exception {
        StringBuffer buffer = new StringBuffer();

        if( accountComboBox.getSelectionModel().getSelectedItem() == null ) {
            buffer.append("Nem v�lasztott�l ki sz�ml�t!\n");
        }

        if( toAccountField.getText().length() != 24 ) {
            buffer.append("A banksz�mlasz�mnak 24 sz�mnak kell lennie!\n");
        }

        try {
            float money = Float.valueOf(moneyField.getText());
            if( money < 0 ) {
                buffer.append("Nem k�ldhetsz 0-n�l kisebb �sszeget!\n");
            }
            if( accountComboBox.getSelectionModel().getSelectedItem() != null && money > accountComboBox.getSelectionModel().getSelectedItem().getMoney()  ) {
                buffer.append("Nincs el�g p�nz a sz�ml�don!\n");
            }
        } catch (NumberFormatException ex) {
            buffer.append("Csak sz�mot �rhatsz a banksz�mlasz�mhoz!\n");
        }

        if( buffer.toString().length() != 0 ) {
            throw new Exception( buffer.toString() );
        }
    }


    @FXML
    public void initialize() {

        // Bankok lek�rdez�se
        Session session = SessionUtil.getSession();
        Query query = session.createQuery("from Bank");
        banks = query.list();
        session.close();

        //Mai dátum beállítása
        dateField.setValue(LocalDate.now());

        // Sz�ml�k lek�rdez�se
        accountComboBox.getItems().setAll(Main.getLoggedUser().getAccounts() );

        // Bank felismer�se
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
