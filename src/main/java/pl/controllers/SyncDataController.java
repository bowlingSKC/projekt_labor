package pl.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import pl.Main;
import pl.bundles.Bundles;
import pl.jpa.SessionUtil;
import pl.model.*;
import pl.model.Currency;

import javax.swing.*;
import javax.swing.text.TabableView;
import java.awt.event.ActionEvent;
import java.io.*;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;


public class SyncDataController {

    @FXML
    private ListView<File> fileList;

    @FXML
    private ComboBox<String> fileTypes;

    @FXML
    private TableView<Transaction> transactionTableView;
    @FXML
    private TableColumn<Transaction, Account> szamlaTableColumn;
    @FXML
    private TableColumn<Transaction, Float> osszegTableColumn;
    @FXML
    private TableColumn<Transaction, Currency> currencyTableColumn;
    @FXML
    private TableColumn<Transaction, Date> dateTableColumn;
    @FXML
    private TableColumn<Transaction, Float> presentTableColumn;
    @FXML
    private TableColumn<Transaction, Account> anotherTableColumn;
    @FXML
    private TableColumn<Transaction, String> commentTableColumn;
    @FXML
    private TableColumn<Transaction, TransactionType> typeTableColumn;
    @FXML
    private Label filesLabel;
    @FXML
    private Label alltransLabel;
    @FXML
    private Label typeLabel;
    @FXML
    private Button openButton;
    @FXML
    private Button dataProcess;

    private ArrayList<Transaction> myTransactions;

    @FXML
    public void initialize() {
        alltransLabel.setText(Bundles.getString("transactions"));
        filesLabel.setText(Bundles.getString("files"));
        typeLabel.setText(Bundles.getString("filetype"));
        openButton.setText(Bundles.getString("openfile"));
        dataProcess.setText(Bundles.getString("processall"));

        fileList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && !fileList.getSelectionModel().isEmpty()) {
                    handleProcess();
                }
            }
        });
        myTransactions = new ArrayList<>();
        fileTypes.getItems().add("OTP - CSV");
        fileTypes.getItems().add("Coming soon...");
        fileTypes.setValue("OTP - CSV");

        //TableView
        szamlaTableColumn.setCellValueFactory(new PropertyValueFactory<>("account"));
        szamlaTableColumn.setText(Bundles.getString("accountC"));
        osszegTableColumn.setCellValueFactory(new PropertyValueFactory<>("money"));
        osszegTableColumn.setText(Bundles.getString("moneyC"));
        currencyTableColumn.setCellValueFactory(new PropertyValueFactory<>("currency"));
        currencyTableColumn.setText(Bundles.getString("currency"));
        dateTableColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateTableColumn.setText(Bundles.getString("date"));
        presentTableColumn.setCellValueFactory(new PropertyValueFactory<>("beforeMoney"));
        presentTableColumn.setText(Bundles.getString("balance"));
        anotherTableColumn.setCellValueFactory(new PropertyValueFactory<>("anotherAccount"));
        anotherTableColumn.setText(Bundles.getString("anotheracc"));
        commentTableColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
        commentTableColumn.setText(Bundles.getString("comment"));
        typeTableColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeTableColumn.setText(Bundles.getString("type"));

        transactionTableView.setRowFactory( tv -> {
            TableRow<Transaction> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    Transaction rowData = row.getItem();
                    handleSelect(rowData);
                }
            });
            return row ;
        });
        //Pénz formátum
        osszegTableColumn.setCellFactory(column -> new TableCell<Transaction, Float>() {
            @Override
            protected void updateItem(Float item, boolean empty) {
                super.updateItem(item, empty);

                if( item == null || empty ) {
                    setText(null);
                    setStyle("");
                } else {
                    NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
                    setText(numberFormat.format(item));
                    //setText(Float.toString(item));
                }
            }
        });
        presentTableColumn.setCellFactory(column -> new TableCell<Transaction, Float>() {
            @Override
            protected void updateItem(Float item, boolean empty) {
                super.updateItem(item, empty);

                if( item == null || empty ) {
                    setText(null);
                    setStyle("");
                } else {
                    NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
                    setText(numberFormat.format(item));
                    //setText(Float.toString(item));
                }
            }
        });
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
            switch (fileTypes.getSelectionModel().getSelectedItem()) {
                case "OTP - CSV":
                    processOTPCSV();
                    break;
            }
            transactionTableView.getItems().clear();
            for (Transaction tra : myTransactions) {
                transactionTableView.getItems().addAll(tra);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    @FXML
    private void handleSelect(Transaction raw){
        //int index = transactionList.getSelectionModel().getSelectedIndex();
        int index = 0;
        for(int i = 0; i < myTransactions.size(); i++){
            if(myTransactions.get(i) == raw){
                index = i;
                //System.out.println(index);
                try {
                    Stage dialogStage = new Stage();
                    FXMLLoader loader = new FXMLLoader(Main.class.getResource("../layout/ShowTransaction.fxml"));
                    BorderPane pane = loader.load();
                    Scene scene = new Scene(pane);
                    dialogStage.setScene(scene);
                    ShowTransactionController showTransactionController = loader.getController();
                    showTransactionController.setTransaction(myTransactions.get(index));
                    dialogStage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Sikertelen művelet!");
                }
            }
        }


    }

    @FXML
    private void handleAll(){
        if( confirmAll() ) {
            Session session = SessionUtil.getSession();
            //Query query = session.createQuery("from TransactionType");
            //List<TransactionType> tTypes;
            //tTypes = query.list();
            org.hibernate.Transaction tx = session.beginTransaction();

            for (int index = 0; index < myTransactions.size(); index++){
                Long compare = myTransactions.get(index).getAccount().getId();
                //System.out.println(compare);
                //compare = compare.substring(0, 24);
                try {
                    // Számla kiválasztása
                    for (Account acc : Main.getLoggedUser().getAccounts()) {
                        if (compare == acc.getId()) {
                            //System.out.println("OK.");
                            acc.setMoney(acc.getMoney() + myTransactions.get(index).getMoney());
                            myTransactions.get(index).setMoney(Math.abs(myTransactions.get(index).getMoney()));
                            session.update(acc);

                            // Tranzakció létrehozása a belépett felhasználónak
                            /*for(TransactionType ttype : tTypes) {
                                if(ttype.getId() == 1){
                                    Transaction myTransaction = myTransactions.get(index);
                                    myTransaction.setType(ttype);
                                    session.save(myTransaction);
                                    System.out.println("OK!");
                                }
                            }*/
                            session.save( myTransactions.get(index));

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

    /*private boolean confirmTransaction(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Megerősítés");
        alert.setHeaderText("Biztosan fel akarod venni a kiválasztott tételt?");
        alert.setContentText(transactionList.getSelectionModel().getSelectedItem().toString());
        Optional<ButtonType> result = alert.showAndWait();
        if( result.get() == ButtonType.OK ) {
            return true;
        }
        return false;
    }*/

    private boolean confirmAll(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(Bundles.getString("error.syncdata.title"));
        alert.setHeaderText(Bundles.getString("error.syncdata.question"));
        alert.setContentText(Bundles.getString("error.syncdata.content"));
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

    private void processOTPCSV(){
        myTransactions.clear();
        TransactionType tempType = new TransactionType();
        Session session = SessionUtil.getSession();
        Query query = session.createQuery("from TransactionType");
        List<TransactionType> tTypes;
        tTypes = query.list();
        session.close();
        try {
            String csvFile = fileList.getSelectionModel().getSelectedItem().getAbsolutePath();
            BufferedReader br = null;
            String line = "";
            String cvsSplitBy = ",";

            try {
                ObservableList<String> items = FXCollections.observableArrayList();
                br = new BufferedReader(new FileReader(csvFile));
                while ((line = br.readLine()) != null) {

                    // use comma as separator
                    String[] transaction = line.split(";");
                    items.add("Számlaszám: " + transaction[0] + "  Összeg:  " + transaction[2] +
                            "   Pénznem: " + transaction[3] + "   Dátum: " + transaction[4] +
                            "   Könyvelt egyenleg: " + transaction[6] + " " + transaction[3] +
                            "   Ellenoldali számlaszám: " + transaction[7] + "   Ellenoldali név: " + transaction[8] +
                            "   Közlemény: " + transaction[9] + " " + transaction[10] + " " + transaction[12]);
                    //transactionList.setItems(items);


                    //Parse to transaction
                    transaction[0] = checkSzamla(transaction[0]);   //számlaszám helyes formátumra hozása
                    transaction[7] = checkSzamla(transaction[7]);
                    DateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);       //dátum helyes formátumra hozása
                    //Find account
                    Account tempAcc = new Account();
                    session = SessionUtil.getSession();
                    query = session.createQuery("from Account");
                    List<Account> tempAccounts;
                    tempAccounts = query.list();
                    session.close();
                    for(Account ac : tempAccounts) {
                        if(ac.getAccountNumber().toString().equals(transaction[0]) ){
                            tempAcc = ac;
                        }
                    }
                    //Find transaction
                    for(TransactionType ttype : tTypes) {
                        if(ttype.getId() == 1 && Float.valueOf(transaction[2]) < 0){
                            tempType = ttype;
                        }
                        if(ttype.getId() == 8 && Float.valueOf(transaction[2]) > 0){
                            tempType = ttype;
                        }
                    }
                    //Find currency
                    Currency myCurr = new Currency();
                    session = SessionUtil.getSession();
                    query = session.createQuery("from Currency");
                    List<Currency> tmpCurr = query.list();
                    session.close();
                    for(Currency curr : tmpCurr) {
                        if(curr.getCode().equals(transaction[3]) ){
                            myCurr = curr;
                        }
                    }
                    Date date = format.parse(transaction[4]);
                    myTransactions.add(new Transaction(tempAcc, transaction[7], Float.valueOf(transaction[6]),
                            Float.valueOf(transaction[2]), date,
                            transaction[9] + " " + transaction[10] + " " + transaction[12], tempType, myCurr));
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
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Hiba!");
        }
    }

    //OLD
    /*@FXML
    private void handleSelect() {
        int index = transactionList.getSelectionModel().getSelectedIndex();
        String compare = myTransactions.get(index).getAccount();

        if( confirmTransaction() ) {
            Session session = SessionUtil.getSession();
            org.hibernate.Transaction tx = session.beginTransaction();

            try {
                // Számla kiválasztása
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
        */
   // }
}
