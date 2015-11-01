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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Locale;

public class PocketController {

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
    private Button inButton;
    @FXML
    private Button outButton;

    private float sumMoney;
    private float allMoney;
    private List<Account> accounts;
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
        pocketPie.setTitle(Bundles.getString("pockets"));

        //Számlák lekérdezése
        accounts = new ArrayList<>();
        for (Account acc : Main.getLoggedUser().getAccounts()) {
            szamlaCombo.getItems().add(acc);
            sumMoney += acc.getMoney();
            allMoney += acc.getMoney();
            accounts.add(acc);
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
        List<Pocket> segedPockets = new ArrayList<>();
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
                for (Account acc2 : accounts) {
                    if(acc2.getId() == poc.getAccount().getId()){
                        acc2.setMoney(acc2.getMoney() - poc.getMoney());
                    }
                }
                segedPockets.add(poc);
            }
        }
        pockets = segedPockets;
        pocketPie.getData().add(new PieChart.Data(Bundles.getString("remained"), sumMoney));
        if(pockets != null){
            updateArea();
        }

        //Placing tooltips
        pocketPie.getData().stream().forEach(data -> {
            Tooltip tooltip = new Tooltip();
            tooltip.setText(round((data.getPieValue() / allMoney)*100,2) + " %");
            Tooltip.install(data.getNode(), tooltip);
            data.pieValueProperty().addListener((observable, oldValue, newValue) ->
                    tooltip.setText(round(((Double)newValue/allMoney)*100,2) + " %"));

        });
        Tooltip tp = new Tooltip();
        tp.setText("Adja meg a zsebhez rendelni kívánt összeget.");
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

    }

    @FXML
    private void handleIn(){
        Session session = SessionUtil.getSession();
        org.hibernate.Transaction tx = session.beginTransaction();

        System.out.println("Betesz.");
        boolean wasPocket = false;
        boolean wasPie = false;
        try {
            if(sumMoney >= Float.valueOf(moneyText.getText())) {
                for (int i = 0; i < pocketPie.getData().size(); i++) {
                    if (pocketPie.getData().get(i).getName().equals(pocketCombo.getSelectionModel().getSelectedItem().toString())) {
                        wasPie = true;
                        for (Pocket poc : pockets) {
                            if(poc.getAccount().toString().equals(szamlaCombo.getSelectionModel().getSelectedItem().toString())
                                    && poc.getCategory().toString().equals(pocketPie.getData().get(i).getName())
                                    && poc.getCategory().toString().equals(pocketCombo.getSelectionModel().getSelectedItem().toString())){
                                wasPocket = true;
                                pocketPie.getData().get(i).setPieValue(pocketPie.getData().get(i).getPieValue() + Double.valueOf(moneyText.getText()));
                                sumMoney -= Float.valueOf(moneyText.getText());
                                System.out.println("1");
                                poc.setMoney(poc.getMoney() + Float.valueOf(moneyText.getText()));
                                for (Account acc : accounts) {
                                    if(acc.getId() == poc.getAccount().getId()){
                                      acc.setMoney(acc.getMoney()-Float.valueOf(moneyText.getText()));
                                      }
                                }
                            }
                        }
                    }
                }
                //Nincs ilyen a diagrammon, nincs ilyen az adatbázisban
                if (!wasPie) {
                    Pocket pocket = new Pocket(Float.valueOf(moneyText.getText()), Main.getLoggedUser(), pocketCombo.getSelectionModel().getSelectedItem(), szamlaCombo.getSelectionModel().getSelectedItem());
                    pockets.add(0, pocket);
                    pocketPie.getData().add(new PieChart.Data(pocketCombo.getSelectionModel().getSelectedItem().toString(), Float.valueOf(moneyText.getText())));
                    sumMoney -= Float.valueOf(moneyText.getText());
                    session.save(pocket);
                    System.out.println("2");
                            //NEW
                    for (Account acc : accounts) {
                        if(acc.getId() == pocket.getAccount().getId()){
                            acc.setMoney(acc.getMoney()-Float.valueOf(moneyText.getText()));
                        }
                    }
                }
                //Van ilyen a diagrammon, de nincsa az adatbázisban
                if(wasPie && !wasPocket){
                    Pocket pocket = new Pocket(Float.valueOf(moneyText.getText()), Main.getLoggedUser(), pocketCombo.getSelectionModel().getSelectedItem(), szamlaCombo.getSelectionModel().getSelectedItem());
                    pockets.add(0,pocket);
                    for(int i = 0; i < pocketPie.getData().size(); i++){
                        if(pocketCombo.getSelectionModel().getSelectedItem().toString() == pocketPie.getData().get(i).getName()){
                            pocketPie.getData().get(i).setPieValue((pocketPie.getData().get(i).getPieValue() + Double.valueOf(moneyText.getText())));
                        }
                    }
                    sumMoney -= Float.valueOf(moneyText.getText());
                    session.save(pocket);
                    System.out.println("3");
                    //NEW
                    for (Account acc : accounts) {
                        if(acc.getId() == pocket.getAccount().getId()){
                            acc.setMoney(acc.getMoney()-Float.valueOf(moneyText.getText()));
                        }
                    }
                }
                tx.commit();
                session.close();
            }else{
                System.out.println("A műveletet nem lehet végrehajtani!");
            }

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
                        for(int j = 0; j < pocketPie.getData().size(); j++) {
                            for (Pocket poc : pockets) {
                                if (poc.getAccount().toString().equals(szamlaCombo.getSelectionModel().getSelectedItem().toString())
                                        && poc.getCategory().toString().equals(pocketPie.getData().get(j).getName())
                                        && poc.getCategory().toString().equals(pocketCombo.getSelectionModel().getSelectedItem().toString())
                                        && poc.getMoney() == moneyPie) {
                                    for (Account acc : accounts) {
                                        if(acc.getId() == poc.getAccount().getId()){
                                            acc.setMoney(acc.getMoney()+Float.valueOf(moneyText.getText()));
                                        }
                                    }
                                    session.delete(poc);
                                    remove = poc;
                                    j--;
                                    pocketPie.getData().remove(i);
                                    sumMoney += Float.valueOf(moneyText.getText());
                                    System.out.println("3");
                                }
                            }
                        }

                    }
                    //Szelet darabjának kivétele
                    if(Double.valueOf(moneyText.getText()) < moneyPie){
                        for(int j = 0; j < pocketPie.getData().size(); j++) {
                            for (Pocket poc : pockets) {
                                if (poc.getAccount().toString().equals(szamlaCombo.getSelectionModel().getSelectedItem().toString())
                                        && poc.getCategory().toString().equals(pocketPie.getData().get(j).getName())
                                        && poc.getCategory().toString().equals(pocketCombo.getSelectionModel().getSelectedItem().toString())) {
                                    if(poc.getMoney() == Float.valueOf(moneyText.getText())){
                                        for (Account acc : accounts) {
                                            if(acc.getId() == poc.getAccount().getId()){
                                                acc.setMoney(acc.getMoney()+Float.valueOf(moneyText.getText()));
                                            }
                                        }
                                        pocketPie.getData().get(i).setPieValue(pocketPie.getData().get(i).getPieValue() - Double.valueOf(moneyText.getText()));
                                        sumMoney += Float.valueOf(moneyText.getText());
                                        session.delete(poc);
                                        remove = poc;
                                        System.out.println("2");
                                    }
                                    if(poc.getMoney() > Float.valueOf(moneyText.getText())){
                                        for (Account acc : accounts) {
                                            if(acc.getId() == poc.getAccount().getId()){
                                                acc.setMoney(acc.getMoney()+Float.valueOf(moneyText.getText()));
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
                    }
                    if(remove != null){
                        pockets.remove(remove);
                    }

                    if(Double.valueOf(moneyText.getText()) > moneyPie){
                        System.out.println("A műveletet nem lehet végrehajtani!");
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
            pocketTableView.getItems().addAll(poc);
        }
        for(Account acc : accounts){
            remainedTableView.getItems().addAll(acc);
        }
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
}
