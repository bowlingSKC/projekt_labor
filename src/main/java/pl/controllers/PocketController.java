package pl.controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import org.hibernate.Query;
import org.hibernate.Session;
import pl.Main;
import pl.jpa.SessionUtil;
import pl.model.Account;
import pl.model.Pocket;
import pl.model.myCategory;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

public class PocketController {

    @FXML
    private ComboBox<Account> szamlaCombo;

    @FXML
    private ComboBox<myCategory> pocketCombo;

    @FXML
    private TextField moneyText;
    @FXML
    private TextArea moneyArea;
    @FXML
    private PieChart pocketPie;

    private float sumMoney;
    private float allMoney;
    private List<Account> accounts;
    private Dictionary<String, Float> accountMoney;
    private List<myCategory> categories;
    private List<Pocket> pockets;

    @FXML
    public void initialize() {
        //Számlák lekérdezése
        //accounts = new ArrayList<>();
        for (Account acc : Main.getLoggedUser().getAccounts()) {
            //accountMoney.put(acc.getAccountNumber(), acc.getMoney());
            szamlaCombo.getItems().add(acc);
            sumMoney += acc.getMoney();
            allMoney += acc.getMoney();
            //accounts.add(acc);
        }

        // Kategóriák lekérdezése
        Session session = SessionUtil.getSession();
        Query query = session.createQuery("from myCategory");
        categories = query.list();
        session.close();
        for(myCategory cat : categories){
            pocketCombo.getItems().add(cat);
        }

        // Számlák lekérdezése
        session = SessionUtil.getSession();
        query = session.createQuery("from Account");
        accounts = query.list();

        // Zsebek lekérdezése
        session = SessionUtil.getSession();
        query = session.createQuery("from Pocket");
        List<Pocket> segedPockets = new ArrayList<>();
        pockets = query.list();
        session.close();
        for(Pocket poc : pockets){
            if(poc.getOwner().getId() == Main.getLoggedUser().getId()){
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
                /*for (Account acc : accounts) {
                    if(acc.getAccountNumber() == poc.getAccount().getAccountNumber()){
                        acc.setMoney(acc.getMoney() - poc.getMoney());
                        System.out.println("-"+acc.getMoney());
                    }
                    System.out.println(acc.getAccountNumber() + " " +acc.getMoney());
                }*/
                segedPockets.add(poc);
            }
        }
        pockets = segedPockets;

        pocketPie.getData().add(new PieChart.Data("Fentmaradó", sumMoney));

        if(pockets != null)
        updateArea();

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

    }

    @FXML
    private void handleIn(){
        Session session = SessionUtil.getSession();
        org.hibernate.Transaction tx = session.beginTransaction();

        System.out.println("Betesz.");
        boolean wasPocket = false;
        boolean wasPie = false;
        try {
            //NEW
            //for (Account acc0 : accounts) {
            //    if(acc0.getAccountNumber() == szamlaCombo.getSelectionModel().getSelectedItem().getAccountNumber() &&
            //           acc0.getMoney() >= Float.valueOf(moneyText.getText())){
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
                                        //if (poc.getCategory().toString() == pocketPie.getData().get(i).getName()) {
                                        poc.setMoney(poc.getMoney() + Float.valueOf(moneyText.getText()));
                                        session.update(poc);
                                        //}
                                        //NEW
                                        /*for (Account acc : accounts) {
                                            if(acc.getAccountNumber() == poc.getAccount().getAccountNumber()){
                                                acc.setMoney(acc.getMoney()-Float.valueOf(moneyText.getText()));
                                            }
                                        }*/
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
                            /*for (Account acc : accounts) {
                                if(acc.getAccountNumber() == pocket.getAccount().getAccountNumber()){
                                    acc.setMoney(acc.getMoney()-Float.valueOf(moneyText.getText()));
                                }
                            }*/
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
                            /*for (Account acc : accounts) {
                                if(acc.getAccountNumber() == pocket.getAccount().getAccountNumber()){
                                    acc.setMoney(acc.getMoney()-Float.valueOf(moneyText.getText()));
                                }
                            }*/
                        }
                        tx.commit();
                        session.close();
                    }else{
                        System.out.println("A műveletet nem lehet végrehajtani!");
                    }

                //}
            //}

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
                                    session.delete(poc);
                                    remove = poc;
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
                                        pocketPie.getData().get(i).setPieValue(pocketPie.getData().get(i).getPieValue() - Double.valueOf(moneyText.getText()));
                                        sumMoney += Float.valueOf(moneyText.getText());
                                        session.delete(poc);
                                        remove = poc;
                                        System.out.println("2");
                                    }
                                    if(poc.getMoney() > Float.valueOf(moneyText.getText())){
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
            if(pocketPie.getData().get(i).getName() == "Fentmaradó"){
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
        moneyArea.clear();
        for(Pocket poc : pockets){
            moneyArea.appendText(poc.getCategory().toString() + "\t  " + poc.getMoney() + " Ft\t" + poc.getAccount().toString() + "\n");
        }
    }
}
