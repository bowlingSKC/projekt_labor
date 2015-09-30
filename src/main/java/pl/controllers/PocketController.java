package pl.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import org.hibernate.Query;
import org.hibernate.Session;
import pl.Main;
import pl.jpa.SessionUtil;
import pl.model.*;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by andru on 2015. 09. 29..
 */
public class PocketController {

    @FXML
    private ComboBox<Account> szamlaCombo;

    @FXML
    private ComboBox<myCategory> pocketCombo;

    @FXML
    private TextField moneyText;

    @FXML
    private PieChart pocketPie;

    private float sumMoney;
    private float allMoney;
    private List<myCategory> categories;
    private List<Pocket> pockets;

    @FXML
    public void initialize() {
        //Számlák lekérdezése
        for (Account acc : Main.getLoggedUser().getAccounts()) {
            szamlaCombo.getItems().add(acc);
            sumMoney += acc.getMoney();
            allMoney += acc.getMoney();
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
        session = SessionUtil.getSession();
        query = session.createQuery("from Pocket");
        pockets = query.list();
        session.close();
        for(Pocket poc : pockets){
            boolean pocWas = false;
            for(int i = 0; i < pocketPie.getData().size(); i++){
                if(poc.getCategory().toString() == pocketPie.getData().get(i).getName()){
                    pocketPie.getData().get(i).setPieValue((pocketPie.getData().get(i).getPieValue() + poc.getMoney()));
                    sumMoney -= poc.getMoney();
                    pocWas = true;
                }
            }
            if(pocWas == false){
                pocketPie.getData().add(new PieChart.Data(poc.getCategory().getName(), poc.getMoney()));
                sumMoney -= poc.getMoney();
            }
        }

        pocketPie.getData().add(new PieChart.Data("Fentmaradó", sumMoney));

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
            if(sumMoney >= Float.valueOf(moneyText.getText())) {
                /*for (int i = 0; i < pocketPie.getData().size(); i++) {
                    for (Pocket poc : pockets) {
                        if (pocketPie.getData().get(i).getName().equals(pocketCombo.getSelectionModel().getSelectedItem().toString())) {
                            wasPie = true;
                            if(poc.getAccount().toString() == szamlaCombo.getSelectionModel().getSelectedItem().toString()){
                                wasPocket = true;
                                pocketPie.getData().get(i).setPieValue(pocketPie.getData().get(i).getPieValue() + Double.valueOf(moneyText.getText()));
                                sumMoney -= Float.valueOf(moneyText.getText());

                                if (poc.getCategory().toString() == pocketPie.getData().get(i).getName()) {
                                    poc.setMoney(poc.getMoney() + Float.valueOf(moneyText.getText()));
                                    session.update(poc);
                                }
                            }
                        }
                    }
                }*/
                //fail
                for (int i = 0; i < pocketPie.getData().size(); i++) {
                    if (pocketPie.getData().get(i).getName().equals(pocketCombo.getSelectionModel().getSelectedItem().toString())) {
                        wasPie = true;
                        for (Pocket poc : pockets) {
                            if(poc.getAccount().toString() == szamlaCombo.getSelectionModel().getSelectedItem().toString()){
                                wasPocket = true;
                                pocketPie.getData().get(i).setPieValue(pocketPie.getData().get(i).getPieValue() + Double.valueOf(moneyText.getText()));
                                sumMoney -= Float.valueOf(moneyText.getText());

                                if (poc.getCategory().toString() == pocketPie.getData().get(i).getName()) {
                                    poc.setMoney(poc.getMoney() + Float.valueOf(moneyText.getText()));
                                    session.update(poc);
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
                    if(Double.valueOf(moneyText.getText()) < moneyPie){
                        sumMoney += Float.valueOf(moneyText.getText());
                        for(Pocket poc : pockets){
                            if(poc.getCategory().toString() == pocketPie.getData().get(i).getName()){
                                poc.setMoney(poc.getMoney() - Float.valueOf(moneyText.getText()));
                                session.update(poc);
                            }
                        }
                        pocketPie.getData().get(i).setPieValue(pocketPie.getData().get(i).getPieValue() - Double.valueOf(moneyText.getText()));
                    }
                    if(Double.valueOf(moneyText.getText()) == moneyPie){
                        sumMoney += Float.valueOf(moneyText.getText());
                        for(Pocket poc : pockets){
                            if(poc.getCategory().toString() == pocketPie.getData().get(i).getName()){
                                session.delete(poc);
                            }
                        }
                        pocketPie.getData().remove(i);
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
}
