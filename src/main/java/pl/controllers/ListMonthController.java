package pl.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import org.hibernate.Query;
import org.hibernate.Session;
import pl.Main;
import pl.jpa.SessionUtil;
import pl.model.Account;
import pl.model.Transaction;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ListMonthController {

    @FXML
    private AreaChart<String,Float> areaChart;
    @FXML
    private DatePicker searchFromDate;
    @FXML
    private DatePicker searchToDate;

    private List<Transaction> allTransactions;
    private ArrayList<XYChart.Series> allseries;

    @FXML
    public void initialize() {
        searchFromDate.valueProperty().addListener((obs, oldDate, newDate) -> {
            /*ZonedDateTime zonedDateTime = newDate.atStartOfDay(ZoneId.systemDefault());
            Instant instant = Instant.from(zonedDateTime);
            Date dateToSet = Date.from(instant);*/
            Date date = Date.from(newDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            System.out.println("Listen. " + date.toString());
            refreshAreaChart(2, date);

        });
        searchToDate.valueProperty().addListener((obs, oldDate, newDate) -> {
            /*ZonedDateTime zonedDateTime = newDate.atStartOfDay(ZoneId.systemDefault());
            Instant instant = Instant.from(zonedDateTime);
            Date dateToSet = Date.from(instant);*/
            Date date = Date.from(newDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            System.out.println("Listen. " + date.toString());
            refreshAreaChart(3, date);

        });

        /*Session session = SessionUtil.getSession();
        Query query = session.createQuery("from Account");
        List<Account> tmp = query.list();
        for(Account acc : tmp){
            if(acc.getOwner().getId() == Main.getLoggedUser().getId()){
                listAccounts.getItems().add(acc);
            }
        }*/

        allseries = new ArrayList<>();
        refreshAreaChart(1, null);
    }

    public void refreshAreaChart(int function, Date date){
        areaChart.getData().clear();
        allseries.clear();

        areaChart.setTitle("Egyenleg");
        final Comparator<XYChart.Data<String, Number>> comparator =
                (XYChart.Data<String, Number> o1, XYChart.Data<String, Number> o2) ->
                        o1.getXValue().compareTo(o2.getXValue());
        //Make series
        for (Account acc : Main.getLoggedUser().getAccounts()) {
            //accounts.add(acc);
            allseries.add(new XYChart.Series());
            allseries.get(allseries.size()-1).setName(acc.toString());
        }
        //Find transactions and add to series
        Session session = SessionUtil.getSession();
        Query query = session.createQuery("from Transaction");
        allTransactions = new ArrayList<>();
        allTransactions = query.list();
        session.close();
        for(Transaction tra : allTransactions){
            for (Account acc : Main.getLoggedUser().getAccounts()) {
                if(acc.getId() == tra.getAccount().getId()){
                    for(XYChart.Series ser : allseries){
                        switch (function){
                            case 1:
                                if(ser.getName().equals(acc.toString())){
                                    ser.getData().add(new XYChart.Data(tra.getDate().toString(), tra.getBeforeMoney()));
                                }
                                break;
                            case 2:
                                if(ser.getName().equals(acc.toString()) && (tra.getDate().after(date) || tra.getDate().equals(date))){
                                    if(searchToDate.getValue() == null){
                                        ser.getData().add(new XYChart.Data(tra.getDate().toString(), tra.getBeforeMoney()));
                                    }else{
                                        Date anotherDate = Date.from(searchToDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                                        if(tra.getDate().before(anotherDate) || tra.getDate().equals(anotherDate)){
                                            ser.getData().add(new XYChart.Data(tra.getDate().toString(), tra.getBeforeMoney()));
                                        }
                                    }

                                }
                                break;
                            case 3:
                                if(ser.getName().equals(acc.toString()) && (tra.getDate().before(date) || tra.getDate().equals(date))){
                                    if(searchFromDate.getValue() == null){
                                        ser.getData().add(new XYChart.Data(tra.getDate().toString(), tra.getBeforeMoney()));
                                    }else{
                                        Date anotherDate = Date.from(searchFromDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                                        if(tra.getDate().after(anotherDate) || tra.getDate().equals(anotherDate)){
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
        for(XYChart.Series ser : allseries){
            ser.getData().sort(comparator);
            areaChart.getData().addAll(ser);

        }

    }

}
