package pl.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pl.Main;
import pl.model.Account;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class PersonalSummaryController {

    private static final SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyy. MM. dd");
    private static final SimpleDateFormat yyyyMMddkkmmss = new SimpleDateFormat("yyy. MM. dd kk:mm:ss");

    @FXML
    private Label numOfAccountLabel;
    @FXML
    private Label sumOfMoneyOnAccountsLabel;
    @FXML
    private Label lastOutTransactionDateLabel;
    @FXML
    private Label lastInTransactionDateLabel;
    @FXML
    private Label lastLoginDateLabel;
    @FXML
    private Label registartionLabel;

    @FXML
    private void handleShowLogins() {
        System.out.println("Majd mutatom ...");
    }

    @FXML
    private void handleDeleteUser() {
        System.out.println("Delete ...");
    }

    @FXML
    private void handleChangePassword() {
        try {
            Stage dialogStage = new Stage();

            FXMLLoader loader = new FXMLLoader( Main.class.getResource("../layout/NewPassword.fxml") );
            AnchorPane pane = (AnchorPane) loader.load();

            Scene scene = new Scene(pane);
            dialogStage.setScene(scene);

            NewPasswordController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.initOwner(Main.getPrimaryStage());
            dialogStage.initModality(Modality.WINDOW_MODAL);

            dialogStage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void initialize() {

        numOfAccountLabel.setText(Integer.toString(Main.getLoggedUser().getAccounts().size()));
        sumOfMoneyOnAccountsLabel.setText( Float.toString(Main.getLoggedUser().getAllMoneyFromAccounts()) );
        registartionLabel.setText( yyyyMMdd.format(Main.getLoggedUser().getRegistredDate()) );

        if( Main.getLoggedUser().getLastLogin() != null ) {
            lastLoginDateLabel.setText( yyyyMMddkkmmss.format(Main.getLoggedUser().getLastLogin()) );
        }


    }


}
