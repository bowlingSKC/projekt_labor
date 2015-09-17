package pl;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import pl.controllers.RootLayoutController;

import java.io.IOException;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Ide ki kéne találni valami jót ...");

        initLayout();
    }

    private void initLayout() {
        try {
            FXMLLoader loader = new FXMLLoader( Main.class.getResource("../layout/RootLayout.fxml") );
            AnchorPane pane = (AnchorPane) loader.load();

            Scene scene = new Scene(pane);
            this.primaryStage.setScene(scene);

            RootLayoutController rootLayoutController = loader.getController();

            this.primaryStage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
