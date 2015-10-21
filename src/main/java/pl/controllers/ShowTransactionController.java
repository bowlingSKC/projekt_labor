package pl.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.hibernate.Query;
import org.hibernate.Session;
import pl.Main;
import pl.jpa.SessionUtil;
import pl.model.Account;
import pl.model.Transaction;
import pl.model.TransactionType;


import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by andru on 2015. 09. 25..
 */
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
    private ComboBox<TransactionType> typeComboBox;

    private Transaction myTransaction;

    @FXML
    public void initialize() {
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

        //Tranzakció típusok betöltése
        Session session = SessionUtil.getSession();
        Query query = session.createQuery("from TransactionType");
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


    public  void setTransaction(Transaction trans){
        myTransaction = trans;
        //szamlaText.setText(myTransaction.getAccount().getAccountNumber().toString().substring(0,24));
        szamla1Text.setText(myTransaction.getAccount().getAccountNumber().toString().substring(0,8));
        szamla2Text.setText(myTransaction.getAccount().getAccountNumber().toString().substring(8,16));
        szamla3Text.setText(myTransaction.getAccount().getAccountNumber().toString().substring(16,24));
        moneyText.setText(String.valueOf(myTransaction.getMoney()));
        datumText.setValue(myTransaction.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        //ellSzamlaText.setText(myTransaction.getAnotherAccount());
        if( myTransaction.getAnotherAccount().toString().length() > 0 ) {
            ellSzamlaText1.setText(myTransaction.getAnotherAccount().toString().substring(0,8));
            ellSzamlaText2.setText(myTransaction.getAnotherAccount().toString().substring(8,16));
            ellSzamlaText3.setText(myTransaction.getAnotherAccount().toString().substring(16,24));
        }
        commText.setText(myTransaction.getComment());

    }


    @FXML
    public void cancelTransaction(){
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    public void updateData(){
        //myTransaction.setAccount(new Account(checkSzamla(szamlaText.getText())));
        myTransaction.setAccount(new Account(checkSzamla(szamla1Text.getText() + szamla2Text.getText() + szamla3Text.getText())));
        myTransaction.setMoney(Float.valueOf(moneyText.getText()));
        Date mydate = Date.from(datumText.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
        System.out.println(mydate.toString());
        myTransaction.setDate(mydate);
        //myTransaction.setAnotherAccount(checkSzamla(ellSzamlaText.getText()));
        myTransaction.setAnotherAccount(checkSzamla(ellSzamlaText1.getText() + ellSzamlaText2.getText() + ellSzamlaText3.getText()));
        myTransaction.setComment(commText.getText());
        myTransaction.setType(typeComboBox.getSelectionModel().getSelectedItem());
    }


    @FXML
    private void sendTransaction(){
        updateData();

        Account tempAcc = new Account();
        Session session = SessionUtil.getSession();
        Query query = session.createQuery("from Account");
        List<Account> tempAccounts;
        tempAccounts = query.list();
        session.close();
        for(Account ac : tempAccounts) {
            /*if(ac.getAccountNumber().toString().equals(szamlaText.getText()) ){
                tempAcc = ac;
            }*/
            if (ac.getAccountNumber().toString().equals(szamla1Text.getText() + szamla2Text.getText() + szamla3Text.getText())) {
                tempAcc = ac;

            }
        }


//        String compare = myTransaction.getAccount().toString();
        String compare = myTransaction.getAccount().getAccountNumber();
//        compare = compare.substring(0,24);
        session = SessionUtil.getSession();
        org.hibernate.Transaction tx = session.beginTransaction();

        try {
            // Számla kiválasztása
            for (Account acc : Main.getLoggedUser().getAccounts()) {
                if (compare.equals(acc.getAccountNumber())) {
                    System.out.println("OK");
                    acc.setMoney(acc.getMoney() + myTransaction.getMoney());
                    session.update(acc);

                    myTransaction.setAccount(tempAcc);
                    //myTransaction.setType(tempType);
                    session.save(myTransaction);
                    System.out.println("OK");
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
