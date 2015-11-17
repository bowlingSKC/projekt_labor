package pl.controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import org.hibernate.Query;
import org.hibernate.Session;
import pl.Main;
import pl.bundles.Bundles;
import pl.jpa.SessionUtil;
import pl.model.*;
import pl.model.Currency;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;

public class PocketSelectController {

    @FXML
    private ComboBox<Account> szamlaCombo;
    @FXML
    private ComboBox<myCategory> pocketCombo;
    @FXML
    private TextField moneyText;
    @FXML
    private PieChart pocketPie;

    @FXML
    private TableView<Pocket> pocketTableView;
    @FXML
    private TableColumn<Pocket, Account> accountTableColumn;
    @FXML
    private TableColumn<Pocket, Float> moneyTableColumn;
    @FXML
    private TableColumn<Pocket, myCategory> pocketTableColumn;

    @FXML
    private TableView<Account> remainedTableView;
    @FXML
    private TableColumn<Account, Account> szamlaTableColumn;
    @FXML
    private TableColumn<Account, Float> egyenlegTableColumn;
    //LANGUAGE
    @FXML
    private Label addpocketLabel;
    @FXML
    private Label chooseaccLabel;
    @FXML
    private Label choosepocketLabel;
    @FXML
    private Label moneyLabel;
    @FXML
    private Label errorLabel;
    @FXML
    private Button inButton;
    @FXML
    private Button outButton;
    @FXML
    private Button csvButton;

    private float sumMoney;
    private float allMoney;
    private Map<Long, Float> accountsMap;
    //private Dictionary<String, Float> accountMoney;
    private List<myCategory> categories;
    private List<Pocket> pockets;

    @FXML
    public void initialize() {
        //LANGUAGE
        addpocketLabel.setText(Bundles.getString("addpocket"));
        chooseaccLabel.setText(Bundles.getString("chooseacc"));
        choosepocketLabel.setText(Bundles.getString("choosepocket"));
        moneyLabel.setText(Bundles.getString("money"));
        inButton.setText(Bundles.getString("putin"));
        outButton.setText(Bundles.getString("takeout"));
        csvButton.setText(Bundles.getString("csv"));
        pocketPie.setTitle(Bundles.getString("pockets"));

        //Számlák lekérdezése
        accountsMap = new HashMap<>();
        sumMoney = 0;
        allMoney = 0;
        for (Account acc : Main.getLoggedUser().getAccounts()) {
            szamlaCombo.getItems().add(acc);
        }

        // Kategóriák lekérdezése
        Session session = SessionUtil.getSession();
        Query query = session.createQuery("from myCategory");
        categories = query.list();
        session.close();
        for(myCategory cat : categories){
            pocketCombo.getItems().add(cat);
        }

        // Zsebek lekérdezése
        /*List<Pocket> segedPockets = new ArrayList<>();
        for (Account acc : Main.getLoggedUser().getAccounts()) {
            for(Pocket poc : acc.getPockets()){
                boolean pocWas = false;
                for(int i = 0; i < pocketPie.getData().size(); i++){
                    if(poc.getCategory().toString() == pocketPie.getData().get(i).getName()){
                        pocketPie.getData().get(i).setPieValue((pocketPie.getData().get(i).getPieValue() + poc.getMoney()));
                        sumMoney -= poc.getMoney();
                        pocWas = true;
                    }
                }
                if(!pocWas){
                    pocketPie.getData().add(new PieChart.Data(poc.getCategory().getName(), poc.getMoney()));
                    sumMoney -= poc.getMoney();
                }
                //NEW
                for (Map.Entry<Long,Float> acc2 : accountsMap.entrySet()) {
                    if(acc2.getKey() == poc.getAccount().getId()){
                        acc2.setValue(acc2.getValue()- poc.getMoney());
                    }
                }
                segedPockets.add(poc);
            }
        }
        pockets = segedPockets;
        pocketPie.getData().add(new PieChart.Data(Bundles.getString("remained"), sumMoney));
        if(pockets != null){
            updateArea();
        }*/

        //Placing tooltips
        Tooltip tp = new Tooltip();
        tp.setText(Bundles.getString("givepocketmoney"));
        moneyText.setTooltip(tp);

        // TableView
        accountTableColumn.setCellValueFactory(new PropertyValueFactory<>("account"));
        accountTableColumn.setText(Bundles.getString("accountC"));
        moneyTableColumn.setCellValueFactory(new PropertyValueFactory<>("money"));
        moneyTableColumn.setText(Bundles.getString("moneyC"));
        pocketTableColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        pocketTableColumn.setText(Bundles.getString("pocket"));

        egyenlegTableColumn.setCellValueFactory(new PropertyValueFactory<>("money"));
        egyenlegTableColumn.setText(Bundles.getString("remained"));
        szamlaTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        szamlaTableColumn.setText(Bundles.getString("account"));
        //Pénz formátum
        moneyTableColumn.setCellFactory(column -> new TableCell<Pocket, Float>() {
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
        egyenlegTableColumn.setCellFactory(column -> new TableCell<Account, Float>() {
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

        errorLabel.setVisible(false);
        szamlaCombo.setOnAction((event) -> {
            pocketPie.getData().clear();
            calculate(szamlaCombo.getSelectionModel().getSelectedItem());
        });

    }

    @FXML
    private void handleIn(){
        System.out.println("Betesz.");
        errorLabel.setVisible(false);

        Session session = SessionUtil.getSession();
        org.hibernate.Transaction tx = session.beginTransaction();

        boolean wasPocket = false;
        boolean wasPie = false;
        try {
            for (int i = 0; i < pocketPie.getData().size(); i++) {
                if (pocketPie.getData().get(i).getName().equals(pocketCombo.getSelectionModel().getSelectedItem().toString())) {
                    wasPie = true;
                    for (Pocket poc : pockets) {
                        if(poc.getAccount().toString().equals(szamlaCombo.getSelectionModel().getSelectedItem().toString())
                                && poc.getCategory().toString().equals(pocketPie.getData().get(i).getName())
                                && poc.getCategory().toString().equals(pocketCombo.getSelectionModel().getSelectedItem().toString())){
                            wasPocket = true;
                            for (Map.Entry<Long,Float> acc : accountsMap.entrySet()) {
                                if(acc.getKey() == poc.getAccount().getId() && acc.getValue() >= Float.valueOf(moneyText.getText())){
                                    acc.setValue(acc.getValue() - Float.valueOf(moneyText.getText()));
                                    pocketPie.getData().get(i).setPieValue(pocketPie.getData().get(i).getPieValue() + Double.valueOf(moneyText.getText()));
                                    sumMoney -= Float.valueOf(moneyText.getText());
                                    System.out.println("1");
                                    poc.setMoney(poc.getMoney() + Float.valueOf(moneyText.getText()));
                                    session.update(poc);
                                }
                                if (acc.getKey() == poc.getAccount().getId() && acc.getValue() < Float.valueOf(moneyText.getText())){
                                    errorLabel.setText(Bundles.getString("notenoughmoney"));
                                    errorLabel.setVisible(true);
                                }
                            }
                        }
                    }
                }
            }
            //Nincs ilyen a diagrammon, nincs ilyen az adatbázisban
            if (!wasPie) {
                Pocket pocket = new Pocket(Float.valueOf(moneyText.getText()), Main.getLoggedUser(), pocketCombo.getSelectionModel().getSelectedItem(), szamlaCombo.getSelectionModel().getSelectedItem());
                //NEW
                for (Map.Entry<Long,Float> acc : accountsMap.entrySet()) {
                    if(acc.getKey() == pocket.getAccount().getId() && acc.getValue() >= Float.valueOf(moneyText.getText())){
                        acc.setValue(acc.getValue() - Float.valueOf(moneyText.getText()));
                        pockets.add(0, pocket);
                        pocketPie.getData().add(new PieChart.Data(pocketCombo.getSelectionModel().getSelectedItem().toString(), Float.valueOf(moneyText.getText())));
                        sumMoney -= Float.valueOf(moneyText.getText());
                        session.save(pocket);
                        System.out.println("2");
                    }
                    if(acc.getKey() == pocket.getAccount().getId() && acc.getValue() < Float.valueOf(moneyText.getText())){
                        errorLabel.setText(Bundles.getString("notenoughmoney"));
                        errorLabel.setVisible(true);
                    }
                }
            }
            //Van ilyen a diagrammon, de nincs az adatbázisban
            if(wasPie && !wasPocket){
                Pocket pocket = new Pocket(Float.valueOf(moneyText.getText()), Main.getLoggedUser(), pocketCombo.getSelectionModel().getSelectedItem(), szamlaCombo.getSelectionModel().getSelectedItem());
                //NEW
                for (Map.Entry<Long,Float> acc : accountsMap.entrySet()) {
                    if(acc.getKey() == pocket.getAccount().getId()){
                        acc.setValue(acc.getValue() - Float.valueOf(moneyText.getText()));
                        pockets.add(0,pocket);
                        for(int i = 0; i < pocketPie.getData().size(); i++){
                            if(pocketCombo.getSelectionModel().getSelectedItem().toString().equals(pocketPie.getData().get(i).getName())){
                                pocketPie.getData().get(i).setPieValue((pocketPie.getData().get(i).getPieValue() + Double.valueOf(moneyText.getText())));
                            }
                        }
                        sumMoney -= Float.valueOf(moneyText.getText());
                        session.save(pocket);
                        System.out.println("3");
                    }
                    if (acc.getKey() == pocket.getAccount().getId() && acc.getValue() < Float.valueOf(moneyText.getText())){
                        errorLabel.setText(Bundles.getString("notenoughmoney"));
                        errorLabel.setVisible(true);
                    }
                }
            }
            tx.commit();
            session.close();
        } catch (Throwable ex) {
            tx.rollback();
            ex.printStackTrace();
        }
        refreshPie();
        updateArea();
    }

    @FXML
    private void handleOut(){
        System.out.println("Kivesz.");
        errorLabel.setVisible(false);

        Session session = SessionUtil.getSession();
        org.hibernate.Transaction tx = session.beginTransaction();

        boolean was = false;
        double moneyPie;
        try {
            for(int i = 0; i < pocketPie.getData().size(); i++){
                if(pocketPie.getData().get(i).getName().equals(pocketCombo.getSelectionModel().getSelectedItem().toString())) {
                    was = true;
                    moneyPie = pocketPie.getData().get(i).getPieValue();
                    Pocket remove = null;
                    //Egész szelet kivétele
                    if(Double.valueOf(moneyText.getText()) == moneyPie){
                        for (Pocket poc : pockets) {
                            if (poc.getAccount().toString().equals(szamlaCombo.getSelectionModel().getSelectedItem().toString())
                                    && poc.getCategory().toString().equals(pocketPie.getData().get(i).getName())
                                    && poc.getCategory().toString().equals(pocketCombo.getSelectionModel().getSelectedItem().toString())
                                    && poc.getMoney() == moneyPie) {
                                for (Map.Entry<Long,Float> acc : accountsMap.entrySet()) {
                                    if(acc.getKey() == poc.getAccount().getId()){
                                        acc.setValue(acc.getValue() + Float.valueOf(moneyText.getText()));
                                    }
                                }
                                session.delete(poc);
                                remove = poc;
                                pocketPie.getData().remove(i);
                                sumMoney += Float.valueOf(moneyText.getText());
                                i--;
                                System.out.println("3");
                            }
                        }
                    }
                    //Szelet darabjának kivétele
                    if(Double.valueOf(moneyText.getText()) < moneyPie){
                        for (Pocket poc : pockets) {
                            if (poc.getAccount().toString().equals(szamlaCombo.getSelectionModel().getSelectedItem().toString())
                                    && poc.getCategory().toString().equals(pocketPie.getData().get(i).getName())
                                    && poc.getCategory().toString().equals(pocketCombo.getSelectionModel().getSelectedItem().toString())) {
                                if(poc.getMoney() == Float.valueOf(moneyText.getText())){
                                    for (Map.Entry<Long,Float> acc : accountsMap.entrySet()) {
                                        if(acc.getKey() == poc.getAccount().getId()){
                                            acc.setValue(acc.getValue() + Float.valueOf(moneyText.getText()));
                                        }
                                    }
                                    pocketPie.getData().get(i).setPieValue(pocketPie.getData().get(i).getPieValue() - Double.valueOf(moneyText.getText()));
                                    sumMoney += Float.valueOf(moneyText.getText());
                                    session.delete(poc);
                                    remove = poc;
                                    System.out.println("2");
                                }
                                if(poc.getMoney() > Float.valueOf(moneyText.getText())){
                                    for (Map.Entry<Long,Float> acc : accountsMap.entrySet()) {
                                        if(acc.getKey() == poc.getAccount().getId()){
                                            acc.setValue(acc.getValue() + Float.valueOf(moneyText.getText()));
                                        }
                                    }
                                    poc.setMoney(poc.getMoney() - Float.valueOf(moneyText.getText()));
                                    session.update(poc);
                                    pocketPie.getData().get(i).setPieValue(pocketPie.getData().get(i).getPieValue() - Double.valueOf(moneyText.getText()));
                                    sumMoney += Float.valueOf(moneyText.getText());
                                    System.out.println("1");
                                }
                            }
                        }
                    }
                    if(remove != null){
                        pockets.remove(remove);
                    }

                    if(Double.valueOf(moneyText.getText()) > moneyPie){
                        errorLabel.setText(Bundles.getString("notenoughmoneyonpocket"));
                        errorLabel.setVisible(true);
                    }
                }
            }

            tx.commit();
            session.close();

        } catch (Throwable ex) {
            tx.rollback();
            ex.printStackTrace();
        }
        refreshPie();
        updateArea();
    }

    public void refreshPie(){
        for(int i = 0; i < pocketPie.getData().size(); i++){
            if(pocketPie.getData().get(i).getName() == "Fentmaradt"){
                pocketPie.getData().get(i).setPieValue(sumMoney);
            }
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public float getActMoney(){
        for(int i = 0; i < pocketPie.getData().size(); i++) {
            for (Pocket poc : pockets) {
                if (poc.getAccount().toString().equals(szamlaCombo.getSelectionModel().getSelectedItem().toString())
                        && poc.getCategory().toString().equals(pocketPie.getData().get(i).getName())) {
                    return poc.getMoney();
                }
            }
        }
        return 0;
    }

    public void updateArea(){
        pocketTableView.getItems().clear();
        remainedTableView.getItems().clear();
        for(Pocket poc : pockets){
            pocketTableView.getItems().add(poc);
        }
        for (Map.Entry<Long,Float> acc : accountsMap.entrySet()) {
            Account tmp = new Account();
            tmp.setId(acc.getKey());
            tmp.setMoney(acc.getValue());
            for (Account account : Main.getLoggedUser().getAccounts()) {
                if(acc.getKey() == account.getId()){
                    tmp.setAccountNumber(account.getAccountNumber());
                    tmp.setName(account.getName());
                }
            }
            remainedTableView.getItems().addAll(tmp);
        }
        //Placing tooltips
        pocketPie.getData().stream().forEach(data -> {
            Tooltip tooltip = new Tooltip();
            tooltip.setText(data.getPieValue() + " Ft - " + round((data.getPieValue() / allMoney)*100,2) + " %");
            Tooltip.install(data.getNode(), tooltip);
            data.pieValueProperty().addListener((observable, oldValue, newValue) ->
                    tooltip.setText(data.getPieValue() + " Ft - " + round((data.getPieValue() / allMoney)*100,2) + " %"));

        });
    }

    public void exportCSV(){
        FileWriter writer = null;
        try {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter =
                    new FileChooser.ExtensionFilter("CSV file (*.csv)", "*.csv");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showSaveDialog(Main.getPrimaryStage());
            if(file != null){

                writer = new FileWriter(file);
                writer.append("Account;Money;Pocket\n");
                for(Pocket poc : pockets){
                    writer.append(poc.getAccount().toString());
                    writer.append(';');
                    writer.append(String.valueOf(poc.getMoney()));
                    writer.append(';');
                    writer.append(poc.getCategory().toString());
                    writer.append('\n');
                    writer.flush();
                }
                writer.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void calculate(Account selected){
        accountsMap = new HashMap<>();
        for (Account acc : Main.getLoggedUser().getAccounts()) {
            if(selected.getId() == acc.getId()){
                sumMoney = acc.getMoney();
                allMoney = acc.getMoney();
                accountsMap.put(acc.getId(), acc.getMoney());
                accountsMap.put(acc.getId(), acc.getMoney());
            }
        }

        List<Pocket> segedPockets = new ArrayList<>();
        for (Account acc : Main.getLoggedUser().getAccounts()) {
            for (Pocket poc : acc.getPockets()) {
                if(poc.getAccount().getId() == selected.getId()){
                    segedPockets.add(poc);
                    pocketPie.getData().add(new PieChart.Data(poc.getCategory().getName(), poc.getMoney()));
                    sumMoney -= poc.getMoney();
                    for (Map.Entry<Long,Float> acc2 : accountsMap.entrySet()) {
                        if(acc2.getKey() == poc.getAccount().getId()){
                            acc2.setValue(acc2.getValue()- poc.getMoney());
                        }
                    }
                }
            }
        }
        pockets = segedPockets;
        pocketPie.getData().add(new PieChart.Data(Bundles.getString("remained"), sumMoney));
        if(pockets != null){
            updateArea();
        }

    }
}
