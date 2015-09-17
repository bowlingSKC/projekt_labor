package pl;

import javafx.scene.control.Alert;

public class MessageBox {

    public static void showErrorMessage(String title, String header, String content, boolean wait) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.show();

        if( wait ) {
            alert.showAndWait();
        } else {
            alert.show();
        }
    }

    public static void showInformationMessage(String title, String header, String content, boolean wait) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.show();

        if( wait ) {
            alert.showAndWait();
        } else {
            alert.show();
        }
    }

    public static void showWarningMessage(String title, String header, String content, boolean wait) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.show();

        if( wait ) {
            alert.showAndWait();
        } else {
            alert.show();
        }
    }

}
