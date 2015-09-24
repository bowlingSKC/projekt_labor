package pl.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import org.hibernate.Query;
import org.hibernate.Session;
import pl.Main;
import pl.jpa.SessionUtil;
import pl.model.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;


public class SyncDataController {

    @FXML
    private ListView<File> fileList;

    @FXML
    private ListView<String> transactionList;

    private ArrayList<Transaction> myTransactions;

    @FXML
    public void initialize() {
        fileList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                    handleProcess();
                }
            }
        });
        transactionList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                    handleSelect();
                }
            }
        });
        myTransactions = new ArrayList<>();
    }

    @FXML
    private void handleOpen(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");       //XMl needed
        fileChooser.getExtensionFilters().add(extFilter);
        File myfile = fileChooser.showOpenDialog(Main.getPrimaryStage());
        if(myfile != null){
            fileList.getItems().add(myfile);
        }
    }
    @FXML
    private void handleProcess(){
        try {

            String csvFile = fileList.getSelectionModel().getSelectedItem().getAbsolutePath();
            BufferedReader br = null;
            String line = "";
            String cvsSplitBy = ",";

            try {
                ObservableList<String> items =FXCollections.observableArrayList ();
                br = new BufferedReader(new FileReader(csvFile));
                while ((line = br.readLine()) != null) {

                    // use comma as separator
                    String[] transaction = line.split(";");
                    items.add("Számlaszám: " + transaction[0] + "  Összeg:  " + transaction[2] +
                            "   Pénznem: " + transaction[3] + "   Dátum: " + transaction[4] +
                            "   Könyvelt egyenleg: " + transaction[6] + " " + transaction[3] +
                            "   Ellenoldali számlaszám: " + transaction[7] + "   Ellenoldali név: " + transaction[8] +
                            "   Közlemény: " + transaction[9] + " " + transaction[10] + " " + transaction[12]);
                    transactionList.setItems(items);

                    //Parse to transaction
                    transaction[0] = checkSzamla(transaction[0]);   //számlaszám helyes formátumra hozása
                    transaction[7] = checkSzamla(transaction[7]);
                    DateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);       //dátum helyes formátumra hozása
                    Date date = format.parse(transaction[4]);
                    myTransactions.add(new Transaction(transaction[0], transaction[7], Float.valueOf(transaction[2]),
                            date, transaction[9] + " " + transaction[10] + " " + transaction[12], new TransactionType()));
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }catch(Exception e){
            System.out.println("Hiba!");
        }
    }
    @FXML
    private void handleSelect(){
        int index = transactionList.getSelectionModel().getSelectedIndex();
        String compare = myTransactions.get(index).getAccount();

        if( confirmTransaction() ) {
            Session session = SessionUtil.getSession();
            org.hibernate.Transaction tx = session.beginTransaction();

            try {
                // Számla kiválasztása

                //Account account = Main.getLoggedUser().getAccounts();
                for( Account acc : Main.getLoggedUser().getAccounts() ) {
                    if(compare.equals(acc.getAccountNumber())){
                        System.out.println("OK");
                        acc.setMoney(acc.getMoney() + myTransactions.get(index).getMoney());
                        session.update(acc);

                        // Tranzakció létrehozása a belépett felhasználónak
                        Query query = session.createQuery("from TransactionType where id = :id");
                        query.setParameter("id", 2);
                        TransactionType transactionType = (TransactionType) query.uniqueResult();
                        Transaction myTransaction = myTransactions.get(index);
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
        }

    }

    @FXML
    private void handleAll(){
        if( confirmAll() ) {
            Session session = SessionUtil.getSession();
            org.hibernate.Transaction tx = session.beginTransaction();
            System.out.println(myTransactions.size());

            for (int index = 0; index < myTransactions.size(); index++){
                String compare = myTransactions.get(index).getAccount();
                try {
                    // Számla kiválasztása

                    //Account account = Main.getLoggedUser().getAccounts();
                    for (Account acc : Main.getLoggedUser().getAccounts()) {
                        if (compare.equals(acc.getAccountNumber())) {
                            System.out.println("OK.");
                            acc.setMoney(acc.getMoney() + myTransactions.get(index).getMoney());
                            session.update(acc);

                            // Tranzakció létrehozása a belépett felhasználónak
                            Query query = session.createQuery("from TransactionType where id = :id");
                            query.setParameter("id", 2);
                            TransactionType transactionType = (TransactionType) query.uniqueResult();
                            Transaction myTransaction = myTransactions.get(index);
                            myTransaction.setType(transactionType);

                            session.save(myTransaction);
                            System.out.println("OK!");
                        }
                    }

                } catch (Throwable ex) {
                    tx.rollback();
                    ex.printStackTrace();
                }

            }
            tx.commit();
            session.flush();
            session.close();
        }
    }

    private boolean confirmTransaction(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Megerősítés");
        alert.setHeaderText("Biztosan fel akarod venni a kiválasztott tételt?");
        alert.setContentText(transactionList.getSelectionModel().getSelectedItem().toString());
        Optional<ButtonType> result = alert.showAndWait();
        if( result.get() == ButtonType.OK ) {
            return true;
        }
        return false;
    }

    private boolean confirmAll(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Megerősítés");
        alert.setHeaderText("Biztosan fel akarod venni az összes tételt?");
        alert.setContentText("Ez a művelet kockázatos lehet.");
        Optional<ButtonType> result = alert.showAndWait();
        if( result.get() == ButtonType.OK ) {
            return true;
        }
        return false;
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
