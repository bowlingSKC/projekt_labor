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
import javafx.stage.FileChooser;
import org.hibernate.Query;
import org.hibernate.Session;
import pl.CurrencyExchange;
import pl.Main;
import pl.bundles.Bundles;
import pl.jpa.SessionUtil;
import pl.model.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    @FXML
    private Label errorLabel;
    @FXML
    private Button csvButton;

    private List<CheckBox> checkBoxes = new ArrayList<>();
    private ArrayList<XYChart.Series> allseries;
    private Map<String,Map<String,Float>> ultimate = new HashMap<>();

    @FXML
    public void initialize() {
        accountsLabel.setText(Bundles.getString("items"));
        filterLabel.setText(Bundles.getString("filter"));
        startLabel.setText(Bundles.getString("startintervall"));
        endLabel.setText(Bundles.getString("endintervall"));
        csvButton.setText(Bundles.getString("csv"));
        errorLabel.setVisible(false);
        searchToDate.setValue(searchToDate.getValue().now());
        searchFromDate.setValue(searchToDate.getValue().minusMonths(1));
        searchFromDate.valueProperty().addListener((obs, oldDate, newDate) -> {
            //Date date = Date.from(newDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            refreshAreaChart();
            //searchToDate.setValue(searchFromDate.getValue().plusMonths(1));
        });
        searchToDate.valueProperty().addListener((obs, oldDate, newDate) -> {
            //Date date = Date.from(newDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            refreshAreaChart();
            //searchFromDate.setValue(searchToDate.getValue().minusMonths(1));
        });
        //Számlák hozzáadása
        VBox vbox = new VBox();
        for (Account acc : Main.getLoggedUser().getAccounts()) {
            CheckBox checkBox = new CheckBox(acc.toString());
            checkBox.setSelected(true);
            checkBoxes.add(checkBox);
            checkBox.setOnAction(event -> {
                for (XYChart.Series ser : allseries) {
                    if (checkBox.getText().equals(ser.getName())) {
                        if (checkBox.isSelected() && !areaChart.getData().contains(ser)) {
                            areaChart.getData().add(ser);
                        }
                        if(!checkBox.isSelected() && areaChart.getData().contains(ser)) {
                            areaChart.getData().remove(ser);
                        }
                    }
                }
            });
            vbox.getChildren().add(checkBox);
        }
        scrollPane.setContent(vbox);
        //Készpénz hozzáadása
        for (ReadyCash cash : Main.getLoggedUser().getReadycash()) {
            CheckBox checkBox = new CheckBox(cash.getCurrency().getCode());
            checkBox.setSelected(true);
            checkBoxes.add(checkBox);
            checkBox.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    for (XYChart.Series ser : allseries) {
                        if (checkBox.getText().equals(ser.getName())) {
                            if (checkBox.isSelected() && !areaChart.getData().contains(ser)) {
                                areaChart.getData().add(ser);
                            }
                            if(!checkBox.isSelected() && areaChart.getData().contains(ser)) {
                                areaChart.getData().remove(ser);
                            }
                        }
                    }
                }
            });
            vbox.getChildren().add(checkBox);
        }
        //Vagyontárgyak hozzáadása
        for(Property prop : Main.getLoggedUser().getProperties()){
            CheckBox checkBox = new CheckBox(prop.getName());
            checkBox.setSelected(true);
            checkBoxes.add(checkBox);
            checkBox.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    for (XYChart.Series ser : allseries) {
                        if (checkBox.getText().equals(ser.getName())) {
                            if (checkBox.isSelected() && !areaChart.getData().contains(ser)) {
                                areaChart.getData().add(ser);
                            }
                            if(!checkBox.isSelected() && areaChart.getData().contains(ser)) {
                                areaChart.getData().remove(ser);
                                break;
                            }
                        }
                    }
                }
            });
            vbox.getChildren().add(checkBox);
        }


        buildUltimate();
        allseries = new ArrayList<>();
        refreshAreaChart();

    }

    public void refreshAreaChart() {
        errorLabel.setVisible(false);
        areaChart.getData().clear();
        allseries.clear();
        Format formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date fromDate, toDate;

        areaChart.setTitle(Bundles.getString("balance"));
        final Comparator<XYChart.Data<String, Number>> comparator =
                (XYChart.Data<String, Number> o1, XYChart.Data<String, Number> o2) ->
                        o1.getXValue().compareTo(o2.getXValue());

        //Valid values
        Map<Date, Long> valid = new HashMap<>();
        for(Account acc : Main.getLoggedUser().getAccounts()){
            for(AccountTransaction tra : acc.getAccountTransactions()){
                if(valid.containsKey(tra.getDate()) && tra.getId() > valid.get(tra.getDate())){
                    valid.replace(tra.getDate(), tra.getId());
                }else{
                    valid.put(tra.getDate(), tra.getId());
                }
            }
        }
        Map<Date, Long> validCash = new HashMap<>();
        for (ReadyCash cash : Main.getLoggedUser().getReadycash()) {
            for (CashTransaction cashTra : cash.getCashTransaction()) {
                if(validCash.containsKey(cashTra.getDate())){
                    if(cashTra.getId() > validCash.get(cashTra.getDate())){
                        validCash.replace(cashTra.getDate(), cashTra.getId());
                    }
                }else{
                    validCash.put(cashTra.getDate(), cashTra.getId());
                }
            }
        }
        Map<Date, Long> validAsset = new HashMap<>();
        for (Property prop : Main.getLoggedUser().getProperties()) {
            for (PropertyValue pv : prop.getValues()) {
                if(validAsset.containsKey(pv.getDate())){
                    if(pv.getId() > validAsset.get(pv.getDate())){
                        validAsset.replace(pv.getDate(), pv.getId());
                    }
                }else{
                    validAsset.put(pv.getDate(), pv.getId());
                }
            }
        }
        //Valid checkbox names
        Set<String> validNames = new HashSet<>();
        for(CheckBox check : checkBoxes){
            if(check.isSelected()){
                validNames.add(check.getText());
            }
        }
        //Find cash transactions
        Map<String, String> seriesID = new HashMap<>();
        Map<String, String> firstCashDates = new HashMap<>();
        for (ReadyCash cash : Main.getLoggedUser().getReadycash()) {
            boolean added = false;
            try{
                fromDate = Date.from(searchFromDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                toDate = Date.from(searchToDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                toDate.setTime(toDate.getTime());
                CashTransaction first = null;
                for (CashTransaction cashTra : cash.getCashTransaction()){
                    if(validNames.contains(cashTra.getCurrency().getCode())){
                        if(!added){
                            allseries.add(new XYChart.Series());
                            allseries.get(allseries.size() - 1).setName(cashTra.getCurrency().getCode());
                            seriesID.put(cashTra.getCurrency().getCode(), "Cash");
                            while (!fromDate.after(toDate)) {
                                allseries.get(allseries.size() - 1).getData().add(new XYChart.Data(formatter.format(fromDate), 0.0f));
                                fromDate.setTime(fromDate.getTime() + 1000 * 60 * 60 * 24);
                            }
                            fromDate = Date.from(searchFromDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                            added = true;
                        }
                        if((cashTra.getDate().after(fromDate) || cashTra.getDate().equals(fromDate)) &&
                                (cashTra.getDate().before(toDate) || cashTra.getDate().equals(toDate)) &&
                                validCash.containsValue(cashTra.getId())){
                            Float tmp = countCashMoney(cash, cashTra);
                            tmp = (float) Math.floor(CurrencyExchange.getValue(cashTra.getCurrency()) * tmp);
                            //allseries.get(allseries.size() - 1).getData().add(new XYChart.Data(cashTra.getDate().toString(), tmp));
                        }

                    }
                    //Find first transaction
                    if(first == null){
                        first = cashTra;
                    }else{
                        if(first.getDate().after(cashTra.getDate())){
                            first = cashTra;
                        }
                    }
                }
                firstCashDates.put(first.getCurrency().getCode(), formatter.format(first.getDate()));
            }catch (NullPointerException e){
                //e.printStackTrace();
            }
        }
        //Find account transactions and add to series
        for (Account acc : Main.getLoggedUser().getAccounts()) {
            if(validNames.contains(acc.toString())){
                allseries.add(new XYChart.Series());
                allseries.get(allseries.size() - 1).setName(acc.toString());
                //seriesID.put(acc.toString(), "Account");
                try {
                    fromDate = Date.from(searchFromDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                    toDate = Date.from(searchToDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                    toDate.setTime(toDate.getTime());
                    while (!fromDate.after(toDate)) {
                        allseries.get(allseries.size() - 1).getData().add(new XYChart.Data(formatter.format(fromDate), 0.0f));
                        fromDate.setTime(fromDate.getTime() + 1000 * 60 * 60 * 24);
                    }
                    fromDate = Date.from(searchFromDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());

                    for (AccountTransaction tr : acc.getAccountTransactions()) {

                        if ((tr.getDate().after(fromDate) || tr.getDate().equals(fromDate)) &&
                                (tr.getDate().before(toDate) || tr.getDate().equals(toDate)) &&
                                valid.containsValue(tr.getId())) {
                                //System.out.println(tr.getId() + " - " + tr.getDate().toString());
                                Float tmp = countMoney(acc, tr);
                                //allseries.get(allseries.size() - 1).getData().add(new XYChart.Data(tr.getDate().toString(), tmp));
                        }
                    }

                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        }
        //Find asset transactions
        for (Property prop : Main.getLoggedUser().getProperties()) {
            if(validNames.contains(prop.getName())){
                allseries.add(new XYChart.Series());
                allseries.get(allseries.size() - 1).setName(prop.getName());
                seriesID.put(prop.getName(), "Asset");
                try {
                    fromDate = Date.from(searchFromDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                    toDate = Date.from(searchToDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                    toDate.setTime(toDate.getTime());
                    while (!fromDate.after(toDate)) {
                        allseries.get(allseries.size() - 1).getData().add(new XYChart.Data(formatter.format(fromDate), 0.0f));
                        fromDate.setTime(fromDate.getTime() + 1000 * 60 * 60 * 24);
                    }
                    fromDate = Date.from(searchFromDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());

                    for (PropertyValue pv : prop.getValues()) {
                        if ((pv.getDate().after(fromDate) || pv.getDate().equals(fromDate)) &&
                                (pv.getDate().before(toDate) || pv.getDate().equals(toDate)) &&
                                validAsset.containsValue(pv.getId())) {
                            //System.out.println(tr.getId() + " - " + tr.getDate().toString());
                            Float tmp = pv.getValue();
                            allseries.get(allseries.size() - 1).getData().add(new XYChart.Data(pv.getDate().toString(), tmp));

                            //allseries.get(allseries.size() - 1).getData().add(new XYChart.Data(tr.getDate().toString(), tr.getBeforeMoney()));
                        }
                    }

                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        }

        //Add series to chart
        for (XYChart.Series ser : allseries) {
            ser.getData().sort(comparator);
            areaChart.getData().add(ser);
        }

        for (XYChart.Series<String, Float> s : areaChart.getData()) {
            /*float ertek = 0;
            for (XYChart.Data<String, Float> d : s.getData()) {
                if (ertek > 0 && d.getYValue() == 0) {
                    d.setYValue(ertek);
                }
                if (d.getYValue() > 0) {
                    ertek = d.getYValue();
                }
            }*/
            //Set null values
            /*boolean was = false;
            boolean wasCash = false;
            boolean wasAss = false;
            for (int i = s.getData().size() - 2; i >= 0; i--) {
                if (s.getData().get(i).getYValue() == 0) {
                    //Set money before first transaction
                    for (Account acc : Main.getLoggedUser().getAccounts()) {
                        for (AccountTransaction tra : acc.getAccountTransactions()) {
                            if (formatter.format(tra.getDate()).equals(s.getData().get(i + 1).getXValue()) && s.getData().get(i).getYValue() == 0
                                    && acc.toString().equals(s.getName()) && !was) {
                                Float tmp;
                                tmp = tra.getMoney();
                                if (tra.getType().getSign().equals("+")) {
                                    tmp = tmp * -1;
                                }
                                //System.out.println(formatter.format(tra.getDate()) + " ---- " + acc.toString() + " - " + tmp.toString());
                                s.getData().get(i).setYValue(s.getData().get(i + 1).getYValue() + tmp);
                                was = true;
                            }
                        }
                    }
                    //Set money before first cashtransaction
                    for(ReadyCash red : Main.getLoggedUser().getReadycash()){
                        for(CashTransaction cash : red.getCashTransaction()){
                            if (formatter.format(cash.getDate()).equals(s.getData().get(i + 1).getXValue()) && s.getData().get(i).getYValue() == 0
                                    && cash.getCurrency().getCode().equals(s.getName()) && !wasCash) {
                                Float tmp;
                                tmp = cash.getMoney();
                                if (cash.getType().getSign().equals("+")) {
                                    tmp = tmp * -1;
                                }
                                s.getData().get(i).setYValue(s.getData().get(i + 1).getYValue() + tmp);
                                wasCash = true;
                            }
                        }
                    }
                    //Set 0 values
                    if(seriesID.containsValue(s.getName()) ){
                        if (s.getData().get(i).getYValue() == 0 &&
                                ( seriesID.get(s.getName()).equals("Account") || !firstCashDates.get(s.getName()).equals(s.getData().get(i).getXValue()))) {
                            s.getData().get(i).setYValue(s.getData().get(i + 1).getYValue());
                        }
                    }
                }
            }*/

            //ULTIMATE
            //if(ultimate.containsValue(s.getName())){
                //System.out.println("Here.");
                for (int i = 0; i < s.getData().size(); i++) {
                    for(Map.Entry<String, Map<String,Float>> entry : ultimate.entrySet()){
                        if(entry.getKey().equals(s.getName())){
                            s.getData().get(i).setYValue(entry.getValue().get(s.getData().get(i).getXValue()));
                            if(s.getData().get(i).getYValue() == null && i > 0){
                                s.getData().get(i).setYValue(s.getData().get(i-1).getYValue());
                            }
                            if(s.getData().get(i).getYValue() == null && i == 0){
                                s.getData().get(i).setYValue(0.0f);
                            }
                        }
                    }
                }
            //}
        }

        //Error handling
        for (XYChart.Series<String, Float> s : areaChart.getData()) {
            //boolean empty = true;
            for (XYChart.Data<String, Float> d : s.getData()) {
                //Correct accountTransactions
                /*if(seriesID.get(s.getName()).equals("Account") && d.getYValue() == 0){
                    AccountTransaction before = null;
                    for(Account acc : Main.getLoggedUser().getAccounts()) {
                        if(acc.toString().equals(s.getName())){
                            for (AccountTransaction tra : acc.getAccountTransactions()) {
                                if(before == null && valid.containsValue(tra.getId()) && formatter.format(tra.getDate()).compareTo(d.getXValue()) < 0){
                                    before = tra;
                                }
                                if(before != null && tra.getId() > before.getId() && formatter.format(tra.getDate()).compareTo(d.getXValue()) < 0){
                                    before = tra;
                                }
                            }
                        }
                    }
                    if(before != null){
                        d.setYValue(countMoney(before.getAccount(), before));
                    }
                }*/
                //Correct cashTransactions
                /*if(seriesID.get(s.getName()).equals("Cash") && d.getYValue() == 0){
                    CashTransaction before = null;
                    for(ReadyCash red : Main.getLoggedUser().getReadycash()) {
                        if(red.getCurrency().getCode().equals(s.getName())){
                            for (CashTransaction tra : red.getCashTransaction()) {
                                if(before == null && validCash.containsValue(tra.getId()) && formatter.format(tra.getDate()).compareTo(d.getXValue()) < 0){
                                    before = tra;
                                }
                                if(before != null && tra.getId() > before.getId() && formatter.format(tra.getDate()).compareTo(d.getXValue()) < 0){
                                    before = tra;
                                }
                            }
                        }
                    }
                    if(before != null){
                        d.setYValue(countCashMoney(before.getCash(), before));
                    }
                }*/
                //Correct assets
                /*if(seriesID.get(s.getName()).equals("Asset") && d.getYValue() == 0){
                    PropertyValue before = null;
                    for(Property prop : Main.getLoggedUser().getProperties()) {
                        if(prop.getName().equals(s.getName())){
                            for (PropertyValue pv : prop.getValues()) {
                                if(before == null && validAsset.containsValue(pv.getId()) && formatter.format(pv.getDate()).compareTo(d.getXValue()) < 0){
                                    before = pv;
                                }
                                if(before != null && pv.getId() > before.getId() && formatter.format(pv.getDate()).compareTo(d.getXValue()) < 0){
                                    before = pv;
                                }
                            }
                        }
                    }
                    if(before != null){
                        d.setYValue(before.getValue());
                    }
                }*/
                //if(d.getYValue() > 0){
                //    empty = false;
                //}

            }
            /*if(empty && !errorLabel.isVisible()){
                errorLabel.setVisible(true);
                errorLabel.setText(Bundles.getString("errorlistmonth"));
            }
            if(empty && errorLabel.isVisible()){
                if(errorLabel.getText().equals(Bundles.getString("errorlistmonth"))){
                    errorLabel.setText(errorLabel.getText() + " " + s.getName());
                }else{
                    errorLabel.setText(errorLabel.getText() + ", " + s.getName());
                }
            }*/
        }

        //Placing tooltip
        for (XYChart.Series<String, Float> se : areaChart.getData()) {
            for (XYChart.Data<String, Float> d : se.getData()) {
                Tooltip.install(d.getNode(), new Tooltip(
                        //s.getName()  + "\n" +
                        d.getXValue().toString() + "\n" +
                                Bundles.getString("accountedmoney") + ": " + d.getYValue() + " Ft"));
            }
        }
    }

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

    public Float countMoney2(Account acc, AccountTransaction tra){
        Float tmp = 0.0f;
        for(AccountTransaction tr : acc.getAccountTransactions()){
            if(tra.getId() >= tr.getId()){
                Float tmpMoney = tr.getMoney();
                if(tr.getType().getSign().equals("-")){
                    tmpMoney = tmpMoney *-1;
                }
                tmp += tmpMoney;
            }
        }
        return tmp;
    }

    public Float countCashMoney(ReadyCash ready, CashTransaction tra){
        Float tmp = ready.getMoney();
        for(CashTransaction tr : ready.getCashTransaction()){
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
    public Float countCashMoney2(ReadyCash ready, CashTransaction tra){
        Float tmp = 0.0f;
        for(CashTransaction tr : ready.getCashTransaction()){
            if(tra.getId() >= tr.getId()){
                Float tmpMoney = tr.getMoney();
                if(tr.getType().getSign().equals("-")){
                    tmpMoney = tmpMoney *-1;
                }
                if(!tra.getCurrency().getCode().equals("HUF")){
                    tmpMoney = (float) Math.floor(CurrencyExchange.getValue(tra.getCurrency()) * tmpMoney);
                }
                tmp += tmpMoney;
            }
        }
        return tmp;
    }

    private void buildUltimate(){
        Format formatter = new SimpleDateFormat("yyyy-MM-dd");
        for(Account acc : Main.getLoggedUser().getAccounts()) {
            Date first = null;
            Date last = new Date();
            Map<String, Float> tmp = new HashMap<>();
            for (AccountTransaction tra : acc.getAccountTransactions()) {
                //System.out.println(tra.getDate() + " - " + countMoney2(acc, tra));
                if(tmp.containsKey(formatter.format(tra.getDate()))){
                    for(AccountTransaction before : acc.getAccountTransactions()){
                        if(before.getDate().equals(tra.getDate()) && tmp.get(formatter.format(tra.getDate())) == countMoney2(acc,before)
                                && before.getId() < tra.getId()){
                            tmp.replace(formatter.format(tra.getDate()), countMoney2(acc, tra));
                            break;
                        }
                    }
                }
                //if(tmp.containsKey(formatter.format(tra.getDate())) && tra.getId() > tmp.get(formatter.format(tra.getDate()))){
                //    tmp.replace(formatter.format(tra.getDate()), countMoney2(acc, tra));
                //}
                else{
                    tmp.put(formatter.format(tra.getDate()), countMoney2(acc, tra));
                }
                if(first == null){
                    first = new Date();
                    first.setTime(tra.getDate().getTime());
                }
                if(tra.getDate().before(first)){
                    first.setTime(tra.getDate().getTime());
                }
            }
            if(first != null){
                last.setTime(last.getTime());
                while(!formatter.format(first).equals(formatter.format(last))){
                    if(tmp.get(formatter.format(first)) == null){
                        tmp.put(formatter.format(first), tmp.get(formatter.format(first.getTime() - 1000 * 60 * 60 * 24)));
                    }
                    first.setTime(first.getTime() + 1000 * 60 * 60 * 24);
                }
                ultimate.put(acc.toString(), tmp);
            }
            for(Map.Entry<String, Map<String,Float>> entry : ultimate.entrySet()){
                for(Map.Entry<String, Float> entry2 : entry.getValue().entrySet()){
                    //System.out.println(entry.getKey() + " - " + entry2.getKey() + " - " + entry2.getValue());
                }
                //System.out.printf("Key : %s \n", entry.getKey());
            }
        }
        for (ReadyCash cash : Main.getLoggedUser().getReadycash()) {
            Date first = null;
            Date last = new Date();
            Map<String, Float> tmp = new HashMap<>();
            for (CashTransaction cashTra : cash.getCashTransaction()) {
                if(tmp.containsKey(formatter.format(cashTra.getDate()))){
                    for(CashTransaction before : cash.getCashTransaction()){
                        if(before.getDate().equals(cashTra.getDate()) && tmp.get(formatter.format(cashTra.getDate())) == countCashMoney2(cash,before)
                                && before.getId() < cashTra.getId()){
                            tmp.replace(formatter.format(cashTra.getDate()), countCashMoney2(cash, cashTra));
                            break;
                        }
                    }
                }/*
                if (tmp.containsKey(formatter.format(cashTra.getDate())) && cashTra.getId() > tmp.get(formatter.format(cashTra.getDate()))) {
                    tmp.replace(formatter.format(cashTra.getDate()), countCashMoney2(cash, cashTra));
                }*/
                else {
                    tmp.put(formatter.format(cashTra.getDate()), countCashMoney2(cash, cashTra));
                }
                if (first == null) {
                    first = new Date();
                    first.setTime(cashTra.getDate().getTime());
                }
                if (cashTra.getDate().before(first)) {
                    first.setTime(cashTra.getDate().getTime());
                }
            }
            if(first != null){
                last.setTime(last.getTime());
                while(!formatter.format(first).equals(formatter.format(last))){
                    if(tmp.get(formatter.format(first)) == null){
                        tmp.put(formatter.format(first), tmp.get(formatter.format(first.getTime() - 1000 * 60 * 60 * 24)));
                    }
                    //System.out.println(formatter.format(first) + " - " + tmp.get(formatter.format(first)));
                    first.setTime(first.getTime() + 1000 * 60 * 60 * 24);
                }
                ultimate.put(cash.getCurrency().getCode(), tmp);
            }
        }
        for (Property prop : Main.getLoggedUser().getProperties()) {
            Date first = null;
            Date last = new Date();
            Map<String, Float> tmp = new HashMap<>();
            for (PropertyValue pv : prop.getValues()) {
                if(tmp.containsKey(formatter.format(pv.getDate()))){
                    for(PropertyValue before : prop.getValues()){
                        if(before.getDate().equals(pv.getDate()) && tmp.get(formatter.format(pv.getDate())) == pv.getValue()
                                && before.getId() < pv.getId()){
                            tmp.replace(formatter.format(pv.getDate()), pv.getValue());
                            break;
                        }
                    }
                }
                //if(tmp.containsKey(formatter.format(pv.getDate())) && pv.getId() > tmp.get(formatter.format(pv.getDate()))){
                //    tmp.replace(formatter.format(pv.getDate()), pv.getValue());
                //}
                else{
                    tmp.put(formatter.format(pv.getDate()), pv.getValue());
                }
                if(first == null){
                    first = new Date();
                    first.setTime(pv.getDate().getTime());
                }
                if(pv.getDate().before(first)){
                    first.setTime(pv.getDate().getTime());
                }
            }
            if(first != null){
                last.setTime(last.getTime());
                while(!formatter.format(first).equals(formatter.format(last))){
                    if(tmp.get(formatter.format(first)) == null){
                        tmp.put(formatter.format(first), tmp.get(formatter.format(first.getTime() - 1000 * 60 * 60 * 24)));
                    }
                    //System.out.println(formatter.format(first) + " - " + tmp.get(formatter.format(first)));
                    first.setTime(first.getTime() + 1000 * 60 * 60 * 24);
                }
                ultimate.put(prop.getName().toString(), tmp);
            }
        }
    }

    public void exportCSV(){
        FileWriter writer = null;
        try {
            //CSV
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter1 =
                    new FileChooser.ExtensionFilter("CSV file (*.csv)", "*.csv");
            fileChooser.getExtensionFilters().add(extFilter1);
            FileChooser.ExtensionFilter extFilter2 = new FileChooser.ExtensionFilter("Excel file (*.xls)", "*.xls");
            fileChooser.getExtensionFilters().add(extFilter2);
            File file = fileChooser.showSaveDialog(Main.getPrimaryStage());
            if(file != null && fileChooser.getSelectedExtensionFilter() == extFilter1){
                writer = new FileWriter(file);
                writer.append("Account;Money;Date\n");
                for (XYChart.Series<String, Float> se : areaChart.getData()) {
                    for (XYChart.Data<String, Float> d : se.getData()) {
                        writer.append(se.getName());
                        writer.append(';');
                        writer.append(String.valueOf(d.getYValue()));
                        writer.append(';');
                        writer.append(d.getXValue());
                        writer.append('\n');
                        writer.flush();
                    }
                }
                writer.close();
            }
            //XLS
            if(file != null && fileChooser.getSelectedExtensionFilter() == extFilter2){
                writer = new FileWriter(file);
                writer.append("Account\tMoney\tDate\n");
                for (XYChart.Series<String, Float> se : areaChart.getData()) {
                    for (XYChart.Data<String, Float> d : se.getData()) {
                        writer.append(se.getName());
                        writer.append('\t');
                        writer.append(String.valueOf(d.getYValue()));
                        writer.append('\t');
                        writer.append(d.getXValue());
                        writer.append('\n');
                        writer.flush();
                    }
                }
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
