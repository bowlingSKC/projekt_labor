package pl.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import pl.Main;
import pl.model.Account;
import pl.model.Transaction;

import java.io.*;
import java.util.Date;


public class SyncDataController {

    @FXML
    private ListView<File> fileList;

    @FXML
    private TextArea transactionArea;

    @FXML
    public void initialize() {

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

            transactionArea.appendText("Fájl neve: " + fileList.getSelectionModel().getSelectedItem().getName() + "\t");
            transactionArea.appendText("Fálj helye: " + fileList.getSelectionModel().getSelectedItem().getAbsolutePath() + "\n\n");

            //Try to read
            String csvFile = fileList.getSelectionModel().getSelectedItem().getAbsolutePath();
            BufferedReader br = null;
            String line = "";
            String cvsSplitBy = ",";

            try {

                br = new BufferedReader(new FileReader(csvFile));
                while ((line = br.readLine()) != null) {

                    // use comma as separator
                    String[] transaction = line.split(";");

                    transactionArea.appendText("Számlaszám: " + transaction[0] + "  Összeg:  " + transaction[2] +
                            "   Pénznem: " + transaction[3] + "   Dátum: " + transaction[4] +
                            "   Könyvelt egyenleg: " + transaction[6] + " " + transaction[3] +
                            "   Ellenoldali számlaszám: " + transaction[7] + "   Ellenoldali név: " + transaction[8] +
                            "   Közlemény: " + transaction[9] + " " + transaction[10] + " " + transaction[12] + "\n");

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

}
