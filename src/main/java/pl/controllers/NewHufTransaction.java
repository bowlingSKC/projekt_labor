package pl.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.hibernate.Query;
import org.hibernate.Session;
import pl.Main;
import pl.MessageBox;
import pl.bundles.Bundles;
import pl.jpa.SessionUtil;
import pl.model.*;

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
            MessageBox.showErrorMessage(Bundles.getString("error.nodb.title"), Bundles.getString("error.processing"), ex.getMessage(), false);
            return;
        }

        if( confirmSendMoney() ) {
                        Session session = SessionUtil.getSession();
            org.hibernate.Transaction tx = session.beginTransaction();

            try {
                // Sz�mla kiv�laszt�sa
                Account account = accountComboBox.getSelectionModel().getSelectedItem();
                account.setMoney(account.getMoney() - Float.valueOf(moneyField.getText()));
                session.update(account);

                // Tranzakci� l�trehoz�sa a bel�pett felhaszn�l�nak
                Date myTransactionDate = Date.from(dateField.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());

                Query query = session.createQuery("from TransactionType where id = :id");
                query.setParameter("id", 1);
                TransactionType transactionType = (TransactionType) query.uniqueResult();
                //Transaction myTransaction = new Transaction(account.getAccountNumber(), toAccountField.getText().trim(), Float.valueOf(moneyField.getText()),
                //        myTransactionDate, commentField.getText().trim(), transactionType);

               // session.save(myTransaction);
                tx.commit();

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
        alert.setTitle( Bundles.getString("confirmation"));
        alert.setHeaderText(Bundles.getString("suretrans"));
        alert.setContentText(Bundles.getString("anotheracc") + toAccountField.getText() + "\n" + Bundles.getString("moneyC") + moneyField.getText() + " forint\n" +
                Bundles.getString("aftertrans") + (accountComboBox.getSelectionModel().getSelectedItem().getMoney() - Float.valueOf(moneyField.getText())) + " forint");
        Optional<ButtonType> result = alert.showAndWait();
        if( result.get() == ButtonType.OK ) {
            return true;
        }
        return false;
    }

    private void checkAllFields() throws Exception {
        StringBuffer buffer = new StringBuffer();

        if( accountComboBox.getSelectionModel().getSelectedItem() == null ) {
            buffer.append(Bundles.getString("accountselect")+"\n");
        }

        if( toAccountField.getText().length() != 24 ) {
            buffer.append(Bundles.getString("account.number.false")+"\n");
        }

        try {
            float money = Float.valueOf(moneyField.getText());
            if( money < 0 ) {
                buffer.append(Bundles.getString("moneygt")+"\n");
            }
            if( accountComboBox.getSelectionModel().getSelectedItem() != null && money > accountComboBox.getSelectionModel().getSelectedItem().getMoney()  ) {
                buffer.append(Bundles.getString("notenoughmoney")+"\n");
            }
        } catch (NumberFormatException ex) {
            buffer.append(Bundles.getString("providenumber")+"\n");
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

        //Mai d�tum be�ll�t�sa
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
                            bankLabel.setText( Bundles.getString("unknownbank") );
                        }
                    }
                }
            }
        });

    }

}
