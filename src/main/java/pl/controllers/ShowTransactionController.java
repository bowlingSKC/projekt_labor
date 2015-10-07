package pl.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import org.hibernate.Query;
import org.hibernate.Session;
import pl.Main;
import pl.jpa.SessionUtil;
import pl.model.Account;
import pl.model.Transaction;
import pl.model.TransactionType;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

/**
 * Created by andru on 2015. 09. 25..
 */
public class ShowTransactionController {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy. MM. dd.");

    @FXML
    private TextField szamlaText;
    @FXML
    private TextField moneyText;
    @FXML
    private DatePicker datumText;
    @FXML
    private TextField ellSzamlaText;
    //@FXML
    //private TextField ellNevText;
    @FXML
    private TextField commText;

    private Transaction myTransaction;

    @FXML
    public void initialize() {
        //Setting listeners to textfields
        szamlaText.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> ov, final String oldValue, final String newValue) {
                if (szamlaText.getText().length() > 24) {
                    String s = szamlaText.getText().substring(0, 24);
                    szamlaText.setText(s);
                }
            }
        });
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


    public  void setTransaction(Transaction trans){
        myTransaction = trans;
        //szamlaText.setText(myTransaction.getAccount());
        moneyText.setText(String.valueOf(myTransaction.getMoney()));
        datumText.setValue(myTransaction.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        ellSzamlaText.setText(myTransaction.getAnotherAccount());
        //ellNevText.setText(ellNev);
        commText.setText(myTransaction.getComment());

    }


    @FXML
    public void cancelTransaction(){
        
    }

    public void updateData(){
        //myTransaction.setAccount(checkSzamla(szamlaText.getText()));
        myTransaction.setMoney(Float.valueOf(moneyText.getText()));
        Date mydate = Date.from(datumText.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
        System.out.println(mydate.toString());
        myTransaction.setDate(mydate);
        myTransaction.setAnotherAccount(checkSzamla(ellSzamlaText.getText()));
        myTransaction.setComment(commText.getText());
    }


    @FXML
    private void sendTransaction(){
        /*
        updateData();
        //String compare = myTransaction.getAccount();
        Session session = SessionUtil.getSession();
        org.hibernate.Transaction tx = session.beginTransaction();

        try {
            // Számla kiválasztása
            for (Account acc : Main.getLoggedUser().getAccounts()) {
                if (compare.equals(acc.getAccountNumber())) {
                    System.out.println("OK");
                    acc.setMoney(acc.getMoney() + myTransaction.getMoney());
                    session.update(acc);

                    // Tranzakció létrehozása a belépett felhasználónak
                    Query query = session.createQuery("from TransactionType where id = :id");
                    query.setParameter("id", 2);
                    TransactionType transactionType = (TransactionType) query.uniqueResult();
                    myTransaction.setType(transactionType);

                    session.save(myTransaction);
                    tx.commit();
                    System.out.println("OK");
                }
            }

        } catch (Throwable ex) {
            tx.rollback();
            ex.printStackTrace();
        }
        session.flush();
        session.close();
        */
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
