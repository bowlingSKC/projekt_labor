package pl.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.hibernate.Query;
import org.hibernate.Session;
import pl.Main;
import pl.bundles.Bundles;
import pl.jpa.SessionUtil;
import pl.model.Account;
import pl.model.AccountTransaction;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class ListMonthController {

    @FXML
    private AreaChart<String, Float> areaChart;
    @FXML
    private DatePicker searchFromDate;
    @FXML
    private DatePicker searchToDate;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Label accountsLabel;
    @FXML
    private Label filterLabel;
    @FXML
    private Label startLabel;
    @FXML
    private Label endLabel;


    private List<AccountTransaction> allTransactions;
    private ArrayList<XYChart.Series> allseries;
    private String selected;
    private Date lastDate;

    @FXML
    public void initialize() {
        accountsLabel.setText(Bundles.getString("accounts"));
        filterLabel.setText(Bundles.getString("filter"));
        startLabel.setText(Bundles.getString("startintervall"));
        endLabel.setText(Bundles.getString("endintervall"));
        searchFromDate.valueProperty().addListener((obs, oldDate, newDate) -> {
            /*ZonedDateTime zonedDateTime = newDate.atStartOfDay(ZoneId.systemDefault());
            Instant instant = Instant.from(zonedDateTime);
            Date dateToSet = Date.from(instant);*/
            Date date = Date.from(newDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            //System.out.println("Listen. " + date.toString());
            refreshAreaChart2();
            //refreshChart();

        });
        searchToDate.valueProperty().addListener((obs, oldDate, newDate) -> {
            /*ZonedDateTime zonedDateTime = newDate.atStartOfDay(ZoneId.systemDefault());
            Instant instant = Instant.from(zonedDateTime);
            Date dateToSet = Date.from(instant);*/
            Date date = Date.from(newDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            //System.out.println("Listen. " + date.toString());
            refreshAreaChart2();
            //refreshChart();

        });
        //Számlák hozzáadása
        VBox vbox = new VBox();
        for (Account acc : Main.getLoggedUser().getAccounts()) {
            CheckBox checkBox = new CheckBox(acc.toString());
            checkBox.setSelected(true);
            checkBox.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    for (XYChart.Series ser : allseries) {
                        if (checkBox.getText().equals(ser.getName())) {
                            if (checkBox.isSelected()) {
                                areaChart.getData().add(ser);
                            } else {
                                areaChart.getData().remove(ser);
                            }
                        }
                    }
                }
            });
            vbox.getChildren().add(checkBox);
        }
        scrollPane.setContent(vbox);

        allseries = new ArrayList<>();
        lastDate = new Date();
        lastDate.setHours(lastDate.getHours() - (60 * 24));
        searchToDate.setValue(lastDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        lastDate.setHours(lastDate.getHours() - (30 * 24));
        searchFromDate.setValue(lastDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        refreshAreaChart2();

    }

    /*public void refreshAreaChart(int function, Date date) {
        areaChart.getData().clear();
        allseries.clear();

        areaChart.setTitle(Bundles.getString("balance"));
        final Comparator<XYChart.Data<String, Number>> comparator =
                (XYChart.Data<String, Number> o1, XYChart.Data<String, Number> o2) ->
                        o1.getXValue().compareTo(o2.getXValue());
        //Make series
        for (Account acc : Main.getLoggedUser().getAccounts()) {
            //accounts.add(acc);
            allseries.add(new XYChart.Series());
            allseries.get(allseries.size() - 1).setName(acc.toString());
        }
        //allseries.add(new XYChart.Series());
        //allseries.get(allseries.size()-1).setName("Készpénz");
        //Find transactions and add to series
        Session session;
        Query query;
        if (function == 1) {
            session = SessionUtil.getSession();
            query = session.createQuery("from Transaction");
            allTransactions = new ArrayList<>();
            allTransactions = query.list();
            session.close();
        }

        for (Transaction tra : allTransactions) {
            for (Account acc : Main.getLoggedUser().getAccounts()) {
                if (acc.getId() == tra.getAccount().getId()) {
                    for (XYChart.Series ser : allseries) {
                        if (tra.getDate().after(lastDate)) {
                            lastDate = tra.getDate();
                        }
                        switch (function) {
                            case 1:
                                if (ser.getName().equals(acc.toString())) {
                                    ser.getData().add(new XYChart.Data(tra.getDate().toString(), tra.getBeforeMoney()));
                                }
                                break;
                            case 2:
                                if (ser.getName().equals(acc.toString()) && (tra.getDate().after(date) || tra.getDate().equals(date))) {
                                    if (searchToDate.getValue() == null) {
                                        ser.getData().add(new XYChart.Data(tra.getDate().toString(), tra.getBeforeMoney()));
                                    } else {
                                        Date anotherDate = Date.from(searchToDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                                        if (tra.getDate().before(anotherDate) || tra.getDate().equals(anotherDate)) {
                                            ser.getData().add(new XYChart.Data(tra.getDate().toString(), tra.getBeforeMoney()));
                                        }
                                    }

                                }
                                break;
                            case 3:
                                if (ser.getName().equals(acc.toString()) && (tra.getDate().before(date) || tra.getDate().equals(date))) {
                                    if (searchFromDate.getValue() == null) {
                                        ser.getData().add(new XYChart.Data(tra.getDate().toString(), tra.getBeforeMoney()));
                                    } else {
                                        Date anotherDate = Date.from(searchFromDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                                        if (tra.getDate().after(anotherDate) || tra.getDate().equals(anotherDate)) {
                                            ser.getData().add(new XYChart.Data(tra.getDate().toString(), tra.getBeforeMoney()));
                                        }
                                    }
                                }
                                break;
                        }

                    }
                }
            }
        }
        //Add series to chart
        for (XYChart.Series ser : allseries) {
            ser.getData().sort(comparator);
            areaChart.getData().add(ser);
        }
    }*/

    public void refreshAreaChart2() {
        areaChart.getData().clear();
        allseries.clear();
        Format formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date fromDate, toDate;

        areaChart.setTitle(Bundles.getString("balance"));
        final Comparator<XYChart.Data<String, Number>> comparator =
                (XYChart.Data<String, Number> o1, XYChart.Data<String, Number> o2) ->
                        o1.getXValue().compareTo(o2.getXValue());
        //allseries.add(new XYChart.Series());
        //allseries.get(allseries.size()-1).setName("Készpénz");

        //Valid values
        Map<Date, Long> valid = new HashMap<Date, Long>();
        for(Account acc : Main.getLoggedUser().getAccounts()){
            for(AccountTransaction tra : acc.getAccountTransactions()){
                Float tmp = countMoney(acc, tra);
                if(valid.containsKey(tra.getDate())){
                    if(tra.getId() > valid.get(tra.getDate())){
                        valid.replace(tra.getDate(), tra.getId());
                    }
                }else{
                    valid.put(tra.getDate(), tra.getId());
                }
            }
        }

        //Find transactions and add to series
        for (Account acc : Main.getLoggedUser().getAccounts()) {
            allseries.add(new XYChart.Series());
            allseries.get(allseries.size() - 1).setName(acc.toString());
            try {
                fromDate = Date.from(searchFromDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                toDate = Date.from(searchToDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                while (!fromDate.after(toDate)) {
                    allseries.get(allseries.size() - 1).getData().add(new XYChart.Data(formatter.format(fromDate), 0.0f));
                    fromDate.setHours(fromDate.getHours() + 24);
                }
                fromDate = Date.from(searchFromDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                /** TODO */
                for (AccountTransaction tr : acc.getAccountTransactions()) {
                    if ((tr.getDate().after(fromDate) || tr.getDate().equals(fromDate)) &&
                            (tr.getDate().before(toDate) || tr.getDate().equals(toDate)) &&
                            valid.containsValue(tr.getId())) {
                        //System.out.println(tr.getId() + " - " + tr.getDate().toString());
                        Float tmp = countMoney(acc, tr);
                        allseries.get(allseries.size() - 1).getData().add(new XYChart.Data(tr.getDate().toString(), tmp));

                        //allseries.get(allseries.size() - 1).getData().add(new XYChart.Data(tr.getDate().toString(), tr.getBeforeMoney()));
                    }
                }

            } catch (Exception e) {
                //e.printStackTrace();
            }
        }

        //Add series to chart
        for (XYChart.Series ser : allseries) {
            ser.getData().sort(comparator);
            areaChart.getData().add(ser);
        }

        for (XYChart.Series<String, Float> s : areaChart.getData()) {
            float ertek = 0;
            for (XYChart.Data<String, Float> d : s.getData()) {
                //System.out.println(ertek + " - " + d.getYValue() + " - " + d.getXValue());
                if (ertek > 0 && d.getYValue() == 0) {
                    d.setYValue(ertek);
                }
                if (d.getYValue() > 0) {
                    ertek = d.getYValue();
                }

            }
            //Set null values
            boolean was = false;
            for (int i = s.getData().size() - 2; i >= 0; i--) {
                if (s.getData().get(i).getYValue() == 0) {
                    //Set money before first transaction
                    for(Account acc : Main.getLoggedUser().getAccounts()){
                        for(AccountTransaction tra : acc.getAccountTransactions()){
                            if(formatter.format(tra.getDate()).equals(s.getData().get(i+1).getXValue()) && s.getData().get(i).getYValue() == 0
                                    && acc.toString().equals(s.getName()) && !was){
                                Float tmp;
                                tmp = tra.getMoney();
                                if(tra.getType().getSign().equals("+")){
                                    tmp = tmp *-1;
                                }
                                //System.out.println(formatter.format(tra.getDate()) + " ---- " + acc.toString() + " - " + tmp.toString());
                                s.getData().get(i).setYValue(s.getData().get(i+1).getYValue()+tmp);
                                was = true;
                            }
                        }
                    }
                    if(s.getData().get(i).getYValue() == 0){
                        s.getData().get(i).setYValue(s.getData().get(i + 1).getYValue());
                    }
                }

            }
            //Placing tooltip
            for (XYChart.Series<String, Float> se : areaChart.getData()) {
                for (XYChart.Data<String, Float> d : se.getData()) {
                    Tooltip.install(d.getNode(), new Tooltip(
                            s.getName()  + "\n" +
                            d.getXValue().toString() + "\n" +
                                    Bundles.getString("accountedmoney") + ": " + d.getYValue() + " Ft"));
                }
            }
        }

        /*List<XYChart.Data<String, Float>> rem = new ArrayList<>();
        for (XYChart.Series<String, Float> s : areaChart.getData()) {
            for (XYChart.Data<String, Float> d : s.getData()) {
                //int ertek = d.getXValue().compareTo(formatter.format(toDate));

                //if (ertek == 1) {
                    //rem.add(d);
                    //s.getData().remove(d);
                //}
            }
        }
        for (XYChart.Data<String, Float> d : rem) {
            System.out.println(d.getXValue());
            areaChart.getData().remove(d);
        }*/
    }

    /*public void refreshChart(){
        for(int i = 0; i < accountListView.getItems().size(); i++){
            if(!accountListView.getItems().get(i).equals(selected)){
                accountListView.getSelectionModel().select(i);
                i = accountListView.getItems().size();
                itemSelected();
            }
        }
        for(int i = 0; i < accountListView.getItems().size(); i++){
            if(accountListView.getItems().get(i).equals(selected)){
                accountListView.getSelectionModel().select(i);
                i = accountListView.getItems().size();
                itemSelected();
            }
        }
    }*/

    public Float countMoney(Account acc, AccountTransaction tra){
        Float tmp = acc.getMoney();
        for(AccountTransaction tr : acc.getAccountTransactions()){
            if(tra.getId() < tr.getId()){
                Float tmpMoney = tr.getMoney();
                if(tr.getType().getSign().equals("+")){
                    tmpMoney = tmpMoney *-1;
                }
                tmp += tmpMoney;
            }
        }
        return tmp;
    }

    /*public void itemSelected(){
        for(XYChart.Series ser : allseries){
            if(accountListView.getSelectionModel().getSelectedItem().toString().equals(ser.getName())){
                if(areaChart.getData().size() == 0){
                    areaChart.getData().add(ser);
                }else{
                    if(areaChart.getData().get(0).getName().equals(accountListView.getSelectionModel().getSelectedItem().toString())){
                    }else {
                        areaChart.getData().clear();
                        //areaChart.setTitle("Egyenleg");
                        areaChart.getData().add(ser);
                    }
                }

            }
        }
    }*/

}
