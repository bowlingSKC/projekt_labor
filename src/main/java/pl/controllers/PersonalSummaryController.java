package pl.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import pl.Constant;
import pl.Main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class PersonalSummaryController {

    @FXML
    private Label sumOfMoneyLabel;
    @FXML
    private Label inReadyCashLabel;
    @FXML
    private Label inAccountsLabel;
    @FXML
    private Label inPropertiesLabel;
    @FXML
    private Label debitLabel;

    @FXML
    public void initialize() {
        computeSumOfMoney();
        computePropertyCash();
        computeReadyCash();
        computeAccountsLabel();
        computeDepit();
    }

    private void computeSumOfMoney() {
        sumOfMoneyLabel.setText( Constant.getNumberFormat().format(Main.getLoggedUser().getAllMoney() - Main.getLoggedUser().getAllDebitsInValue()) );
    }

    private void computeAccountsLabel() {
        inAccountsLabel.setText( Constant.getNumberFormat().format(Main.getLoggedUser().getAllMoneyFromAccounts()) );
    }

    private void computeReadyCash() {
        inReadyCashLabel.setText(Constant.getNumberFormat().format(Main.getLoggedUser().getAllMoneyInReadyCash()));
    }

    private void computePropertyCash() {
        inPropertiesLabel.setText(Constant.getNumberFormat().format(Main.getLoggedUser().getAllMoneyInProperties()));
    }

    private void computeDepit() {
        debitLabel.setText( Constant.getNumberFormat().format(Main.getLoggedUser().getAllDebitsInValue()) );
    }

    public void handleToCSV() {
        FileWriter writer = null;
        //Write first table
        try {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter =
                    new FileChooser.ExtensionFilter("CSV file (*.csv)", "*.csv");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showSaveDialog(Main.getPrimaryStage());
            if (file != null) {

                writer = new FileWriter(file);
                writer.append("Sum_of_Money;");
                writer.append(sumOfMoneyLabel.getText() + "\n");
                writer.append("Sum_of_Cash;");
                writer.append(inReadyCashLabel.getText() + "\n");
                writer.append("Sum_of_Accounts;");
                writer.append(inAccountsLabel.getText() + "\n");
                writer.append("Sum_of_Asset;");
                writer.append(inPropertiesLabel.getText() + "\n");
                writer.append("Sum_of_Debit;");
                writer.append(debitLabel.getText());
                writer.append('\n');
                writer.flush();
                writer.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
