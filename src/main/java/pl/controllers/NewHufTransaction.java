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
import pl.model.TransactionType;

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
            MessageBox.showErrorMessage("Hiba", "A megb?z?st nem lehet l?trehotni!", ex.getMessage(), false);
            return;
        }

        if( confirmSendMoney() ) {
                        Session session = SessionUtil.getSession();
            org.hibernate.Transaction tx = session.beginTransaction();

            try {
                // Számla kiválasztása
                Account account = accountComboBox.getSelectionModel().getSelectedItem();
                account.setMoney(account.getMoney() - Float.valueOf(moneyField.getText()));
                session.update(account);

                // Tranzakció létrehozása a belépett felhasználónak
                Date myTransactionDate = Date.from(dateField.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());

                Query query = session.createQuery("from TransactionType where id = :id");
                query.setParameter("id", 2);
                TransactionType transactionType = (TransactionType) query.uniqueResult();
                Transaction myTransaction = new Transaction(account.getAccountNumber(), toAccountField.getText().trim(), Float.valueOf(moneyField.getText()),
                        myTransactionDate, commentField.getText().trim(), transactionType);

                session.save(myTransaction);
                tx.commit();
                //System.out.println("OK");

            } catch (Throwable ex) {
                tx.rollback();
                ex.printStackTrace();
            }
            session.flush();
            session.close();
        }

    }

    private boolean confirmSendMoney() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Meger?s?t?s");
        alert.setHeaderText("Biztosan v?gre akarod hajtani a tranzakci?t?");
        alert.setContentText("Kedvezm?nyezett: " + toAccountField.getText() + "\n?sszeg: " + moneyField.getText() + " forint\n" +
                "Tranzakci? ut?n marad? ?sszeg: " + (accountComboBox.getSelectionModel().getSelectedItem().getMoney() - Float.valueOf(moneyField.getText())) + " forint");
        Optional<ButtonType> result = alert.showAndWait();
        if( result.get() == ButtonType.OK ) {
            return true;
        }
        return false;
    }

    private void checkAllFields() throws Exception {
        StringBuffer buffer = new StringBuffer();

        if( accountComboBox.getSelectionModel().getSelectedItem() == null ) {
            buffer.append("Nem v?lasztott?l ki sz?ml?t!\n");
        }

        if( toAccountField.getText().length() != 24 ) {
            buffer.append("A banksz?mlasz?mnak 24 sz?mnak kell lennie!\n");
        }

        try {
            float money = Float.valueOf(moneyField.getText());
            if( money < 0 ) {
                buffer.append("Nem k?ldhetsz 0-n?l kisebb ?sszeget!\n");
            }
            if( accountComboBox.getSelectionModel().getSelectedItem() != null && money > accountComboBox.getSelectionModel().getSelectedItem().getMoney()  ) {
                buffer.append("Nincs el?g p?nz a sz?ml?don!\n");
            }
        } catch (NumberFormatException ex) {
            buffer.append("Csak sz?mot ?rhatsz a banksz?mlasz?mhoz!\n");
        }

        if( buffer.toString().length() != 0 ) {
            throw new Exception( buffer.toString() );
        }
    }


    @FXML
    public void initialize() {

        // Bankok lek?rdez?se
        Session session = SessionUtil.getSession();
        Query query = session.createQuery("from Bank");
        banks = query.list();
        session.close();

        //Mai dátum beállítása
        dateField.setValue(LocalDate.now());

        // Sz?ml?k lek?rdez?se
        accountComboBox.getItems().setAll(Main.getLoggedUser().getAccounts() );

        // Bank felismer?se
        bankLabel.setText("");
        toAccountField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if( toAccountField.getText().length() >= 3 ) {
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
            }
        });

    }

}
