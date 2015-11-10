package pl.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

public class SplashController {

    @FXML
    private Label closeAppLabel;

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            closeAppLabel.setOnMouseClicked((MouseEvent event) -> {
                Platform.exit();
                System.exit(0);
            });
        });
    }

    public void setStatusLabel(String text) {
        statusLabel.setText(text);
    }

}
