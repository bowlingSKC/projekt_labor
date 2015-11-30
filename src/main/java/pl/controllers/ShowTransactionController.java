package pl.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.hibernate.Query;
import org.hibernate.Session;
import pl.Main;
import pl.bundles.Bundles;
import pl.jpa.SessionUtil;
import pl.model.Account;
import pl.model.Currency;
import pl.model.AccountTransaction;
import pl.model.TransactionType;


import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ShowTransactionController {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy. MM. dd.");

    //@FXML
    //private TextField szamlaText;
    @FXML
    private TextField szamla1Text;
    @FXML
    private TextField szamla2Text;
    @FXML
    private TextField szamla3Text;
    @FXML
    private TextField moneyText;
    @FXML
    private DatePicker datumText;
    //@FXML
    //private TextField ellSzamlaText;
    @FXML
    private TextField ellSzamlaText1;
    @FXML
    private TextField ellSzamlaText2;
    @FXML
    private TextField ellSzamlaText3;
    @FXML
    private TextField commText;
    @FXML
    private Button closeButton;
    @FXML
    private Button acceptButton;
    @FXML
    private ComboBox<TransactionType> typeComboBox;
    @FXML
    private ComboBox<Currency> currencyComboBox;
    @FXML
    private Label accLabel;
    @FXML
    private Label moneyLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label anoaccLabel;
    @FXML
    private Label commLabel;
    @FXML
    private Label typeLabel;

    private AccountTransaction myAccountTransaction;

    @FXML
    public void initialize() {
        accLabel.setText(Bundles.getString("accountL"));
        moneyLabel.setText(Bundles.getString("moneyL"));
        dateLabel.setText(Bundles.getString("dateL"));
        anoaccLabel.setText(Bundles.getString("anoacc"));
        commLabel.setText(Bundles.getString("commentL"));
        typeLabel.setText(Bundles.getString("typeL"));
        closeButton.setText(Bundles.getString("cancel"));
        acceptButton.setText(Bundles.getString("accData"));
        //Setting listeners to textfields
        szamla1Text.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> ov, final String oldValue, final String newValue) {
                if (szamla1Text.getText().length() > 8) {
                    String s = szamla1Text.getText().substring(0, 8);
                    szamla1Text.setText(s);
                }
            }
        });
        szamla2Text.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> ov, final String oldValue, final String newValue) {
                if (szamla2Text.getText().length() > 8) {
                    String s = szamla2Text.getText().substring(0, 8);
                    szamla2Text.setText(s);
                }
            }
        });
        szamla3Text.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> ov, final String oldValue, final String newValue) {
                if (szamla3Text.getText().length() > 8) {
                    String s = szamla3Text.getText().substring(0, 8);
                    szamla3Text.setText(s);
                }
            }
        });
        //Currency betöltése
        Session session = SessionUtil.getSession();
        Query query = session.createQuery("from Currency");
        List<Currency> tmpCurr;
        tmpCurr = query.list();
        session.close();
        for(Currency curr : tmpCurr) {
            currencyComboBox.getItems().add(curr);
            /*if(curr.getCode().equals(myTransaction.getCurrency().getCode())){
                currencyComboBox.getSelectionModel().select(curr);
            }*/
        }

        //Tranzakció típusok betöltése
        session = SessionUtil.getSession();
        query = session.createQuery("from TransactionType");
        List<TransactionType> tTypes;
        tTypes = query.list();
        session.close();
        for(TransactionType ttype : tTypes) {
            typeComboBox.getItems().add(ttype);
        }
        typeComboBox.getSelectionModel().selectFirst();
        /*moneyText.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                myTransaction.setMoney(Float.valueOf(newValue));
            }
        });
        datumText.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                try {
                    DateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);       //dátum helyes formátumra hozása
                    Date date = format.parse(newValue.toString());
                    myTransaction.setDate(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
        ellSzamlaText.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                myTransaction.setAnotherAccount(newValue);
            }
        });
        commText.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                myTransaction.setComment(newValue);
            }
        });*/
    }


    public void setTransaction(AccountTransaction trans){
        myAccountTransaction = trans;
        //szamlaText.setText(myTransaction.getAccount().getAccountNumber().toString().substring(0,24));
        szamla1Text.setText(myAccountTransaction.getAccount().getAccountNumber().toString().substring(0,8));
        szamla2Text.setText(myAccountTransaction.getAccount().getAccountNumber().toString().substring(8,16));
        szamla3Text.setText(myAccountTransaction.getAccount().getAccountNumber().toString().substring(16,24));
        moneyText.setText(String.valueOf(myAccountTransaction.getMoney()));
        datumText.setValue(myAccountTransaction.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        //ellSzamlaText.setText(myTransaction.getAnotherAccount());
        if( myAccountTransaction.getAnotherAccount().toString().length() > 0 ) {
            ellSzamlaText1.setText(myAccountTransaction.getAnotherAccount().toString().substring(0,8));
            ellSzamlaText2.setText(myAccountTransaction.getAnotherAccount().toString().substring(8,16));
            ellSzamlaText3.setText(myAccountTransaction.getAnotherAccount().toString().substring(16,24));
        }
        commText.setText(myAccountTransaction.getComment());
        currencyComboBox.getSelectionModel().select(myAccountTransaction.getCurrency());

    }


    @FXML
    public void cancelTransaction(){
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    public void updateData(){
        //myTransaction.setAccount(new Account(checkSzamla(szamlaText.getText())));
        myAccountTransaction.setAccount(new Account(checkSzamla(szamla1Text.getText() + szamla2Text.getText() + szamla3Text.getText())));
        System.out.println(myAccountTransaction.getAccount().getAccountNumber());
        myAccountTransaction.setMoney(Float.valueOf(moneyText.getText()));
        Date mydate = Date.from(datumText.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
        System.out.println(mydate.toString());
        myAccountTransaction.setDate(mydate);
        //myTransaction.setAnotherAccount(checkSzamla(ellSzamlaText.getText()));
        myAccountTransaction.setAnotherAccount(checkSzamla(ellSzamlaText1.getText() + ellSzamlaText2.getText() + ellSzamlaText3.getText()));
        myAccountTransaction.setComment(commText.getText());
        myAccountTransaction.setType(typeComboBox.getSelectionModel().getSelectedItem());
        myAccountTransaction.setCurrency(currencyComboBox.getSelectionModel().getSelectedItem());
    }


    @FXML
    private void sendTransaction(){
        updateData();

        Account tempAcc = new Account();
        Session session;
        List<Account> tempAccounts = new ArrayList<>();
        for(Account acc : Main.getLoggedUser().getAccounts()){
            tempAccounts.add(acc);
        }
        for(Account ac : tempAccounts) {
            /*if(ac.getAccountNumber().toString().equals(szamlaText.getText()) ){
                tempAcc = ac;
            }*/
            if (ac.getAccountNumber().toString().equals(szamla1Text.getText() + szamla2Text.getText() + szamla3Text.getText())) {
                tempAcc = ac;
            }
        }
        myAccountTransaction.setAccount(tempAcc);
        //Get latest transaction
        AccountTransaction prev = tempAcc.getLatestTransaction();
        if( prev != null ) {
            myAccountTransaction.setBeforeAccountTransaction(prev);
        }

        String compare = myAccountTransaction.getAccount().getAccountNumber();
        session = SessionUtil.getSession();
        org.hibernate.Transaction tx = session.beginTransaction();

        try {
            // Számla kiválasztása
            for (Account acc : Main.getLoggedUser().getAccounts()) {
                if (compare.equals(acc.getAccountNumber())) {
                    System.out.println("OK");
                    acc.setMoney(acc.getMoney() + myAccountTransaction.getMoney());
                    //System.out.println("OK");
                    acc.setMoney(acc.getMoney() + myAccountTransaction.getMoney());
                    myAccountTransaction.setMoney(Math.abs(myAccountTransaction.getMoney()));
                    session.update(acc);

                    //myTransaction.setType(tempType);
                    session.save(myAccountTransaction);
                    System.out.println("OK");
                    acc.getAccountTransactions().add(myAccountTransaction);
                    session.save(myAccountTransaction);
                    //System.out.println("OK");
                }
            }

        } catch (Throwable ex) {
            tx.rollback();
            ex.printStackTrace();
        }
        tx.commit();
        session.flush();
        session.close();

        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();

    }

    private String checkSzamla(String szamla){
        if(szamla.length() == 18 ){
            szamla = szamla.substring(1,17);
            szamla = szamla + "00000000";
        }
        if(szamla.length() == 16 ){
            szamla = szamla + "00000000";
        }
        if(szamla.length() == 26 ){
            szamla = szamla.substring(1,25);
        }
        return szamla;
    }


}
