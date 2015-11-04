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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.hibernate.Query;
import org.hibernate.Session;
import pl.Main;
import pl.bundles.Bundles;
import pl.jpa.SessionUtil;
import pl.model.*;
import pl.model.Currency;

import java.io.*;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class SyncDataController {

    @FXML
    private ListView<File> fileList;

    @FXML
    private ComboBox<String> fileTypes;

    @FXML
    private TableView<AccountTransaction> transactionTableView;
    @FXML
    private TableColumn<AccountTransaction, Account> szamlaTableColumn;
    @FXML
    private TableColumn<AccountTransaction, Float> osszegTableColumn;
    @FXML
    private TableColumn<AccountTransaction, Currency> currencyTableColumn;
    @FXML
    private TableColumn<AccountTransaction, Date> dateTableColumn;
    //@FXML
    //private TableColumn<AccountTransaction, Float> presentTableColumn;
    @FXML
    private TableColumn<AccountTransaction, Account> anotherTableColumn;
    @FXML
    private TableColumn<AccountTransaction, String> commentTableColumn;
    @FXML
    private TableColumn<AccountTransaction, TransactionType> typeTableColumn;
    @FXML
    private Label filesLabel;
    @FXML
    private Label alltransLabel;
    @FXML
    private Label typeLabel;
    @FXML
    private Label errorLabel;
    @FXML
    private Button openButton;
    @FXML
    private Button dataProcess;


    private ArrayList<AccountTransaction> myAccountTransactions;
    //private ArrayList<AccountTransaction> myTransactions;

    @FXML
    public void initialize() {
        alltransLabel.setText(Bundles.getString("transactions"));
        filesLabel.setText(Bundles.getString("files"));
        typeLabel.setText(Bundles.getString("filetype"));
        openButton.setText(Bundles.getString("openfile"));
        dataProcess.setText(Bundles.getString("processall"));
        errorLabel.setVisible(false);

        fileList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && !fileList.getSelectionModel().isEmpty()) {
                    errorLabel.setVisible(false);
                    handleProcess();
                    if(errorLabel.isVisible()){
                        checkExist(1);
                    }else{
                        checkExist(2);
                    }
                }
            }
        });
        myAccountTransactions = new ArrayList<>();
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
        //presentTableColumn.setCellValueFactory(new PropertyValueFactory<>("beforeMoney"));
        //presentTableColumn.setText(Bundles.getString("balance"));
        anotherTableColumn.setCellValueFactory(new PropertyValueFactory<>("anotherAccount"));
        anotherTableColumn.setText(Bundles.getString("anotheracc"));
        commentTableColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
        commentTableColumn.setText(Bundles.getString("comment"));
        typeTableColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeTableColumn.setText(Bundles.getString("type"));

        transactionTableView.setRowFactory( tv -> {
            TableRow<AccountTransaction> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    AccountTransaction rowData = row.getItem();
                    handleSelect(rowData);
                }
            });
            return row ;
        });
        //Pénz formátum
        osszegTableColumn.setCellFactory(column -> new TableCell<AccountTransaction, Float>() {
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
        /*presentTableColumn.setCellFactory(column -> new TableCell<AccountTransaction, Float>() {
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
        });*/
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
            for(AccountTransaction tra : myAccountTransactions){
                transactionTableView.getItems().addAll(tra);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    @FXML
    private void handleSelect(AccountTransaction raw){
        //int index = transactionList.getSelectionModel().getSelectedIndex();
        int index = 0;
        for(int i = 0; i < myAccountTransactions.size(); i++){
            if(myAccountTransactions.get(i) == raw){
                index = i;
                //System.out.println(index);
                try {
                    Stage dialogStage = new Stage();
                    FXMLLoader loader = new FXMLLoader(Main.class.getResource("../layout/ShowTransaction.fxml"));
                    BorderPane pane = loader.load();
                    Scene scene = new Scene(pane);
                    dialogStage.setScene(scene);
                    ShowTransactionController showTransactionController = loader.getController();
                    showTransactionController.setTransaction(myAccountTransactions.get(index));
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
            boolean first = true;
            AccountTransaction tmp = null;

            org.hibernate.Transaction tx = session.beginTransaction();
            for (int index = 0; index < myAccountTransactions.size(); index++){
                Long compare = myAccountTransactions.get(index).getAccount().getId();

                //Playing with before_id
                AccountTransaction prev = myAccountTransactions.get(index).getAccount().getLatestTransaction();
                if(!first){
                    myAccountTransactions.get(index).setBeforeAccountTransaction(tmp);
                    tmp = myAccountTransactions.get(index);
                }
                if( prev != null && first) {
                    myAccountTransactions.get(index).setBeforeAccountTransaction(prev);
                    first = false;
                    tmp = myAccountTransactions.get(index);
                    //System.out.println(myAccountTransactions.get(index).getDate().toString() + " - " + myAccountTransactions.get(index).getBeforeAccountTransaction().getDate().toString());
                }
                if(prev == null && first){
                    //System.out.println(myAccountTransactions.get(index).getDate().toString() + " - null");
                    first = false;
                    tmp = myAccountTransactions.get(index);
                }

                //System.out.println(compare);
                //compare = compare.substring(0, 24);
                try {
                    // Számla kiválasztása
                    for (Account acc : Main.getLoggedUser().getAccounts()) {
                        //System.out.println(acc.getId().toString() + " " + compare.toString());
                        if (compare == acc.getId()) {
                            System.out.println("OK.");
                            acc.setMoney(acc.getMoney() + myAccountTransactions.get(index).getMoney());
                            //System.out.println("OK.");
                            acc.setMoney(acc.getMoney() + myAccountTransactions.get(index).getMoney());
                            myAccountTransactions.get(index).setMoney(Math.abs(myAccountTransactions.get(index).getMoney()));
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
                            session.save( myAccountTransactions.get(index));

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

    private void processOTPCSV(){
        myAccountTransactions.clear();
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
                    Account tempAcc = null;
                    for(Account ac : Main.getLoggedUser().getAccounts()) {
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

                    //Create new
                    if(tempAcc != null){
                        AccountTransaction toAdd = new AccountTransaction(
                                tempAcc, transaction[7], Float.valueOf(transaction[2]), date,
                                transaction[9] + " " + transaction[10] + " " + transaction[12], tempType, myCurr);
                        myAccountTransactions.add(toAdd);
                    }else{
                        errorLabel.setVisible(true);
                        errorLabel.setText(Bundles.getString("importmissingvalues"));
                    }
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

    private void checkExist(int function){
        boolean exist;
        boolean error = true;
        for(AccountTransaction acctra : myAccountTransactions){
            exist = false;
            for(Account acc : Main.getLoggedUser().getAccounts()){
                for(AccountTransaction tra : acc.getAccountTransactions()){
                    if(tra.getMoney() == Math.abs(acctra.getMoney())){
                        exist = true;
                    }
                }
            }
            if(!exist){
                error = false;
            }
        }
        if(error){
            errorLabel.setVisible(true);
            switch (function){
                case 1:
                    errorLabel.setText(errorLabel.getText() + " + " + Bundles.getString("importexist"));
                    break;
                case 2:
                    errorLabel.setText(Bundles.getString("importexist"));
                    break;
            }

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
