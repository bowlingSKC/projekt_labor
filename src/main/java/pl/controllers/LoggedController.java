package pl.controllers;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import pl.Main;

import java.io.IOException;

public class LoggedController {

    private final TreeSelectionListener listener = new TreeSelectionListener();

    // Ablakot kezelik
    @FXML
    private Button closeButton;
    @FXML
    private Button maximizeButton;
    @FXML
    private Button minimizeButton;
    @FXML
    private Button resizeButton;
    @FXML
    private Button fullscreenButton;
    private Rectangle2D rec2;
    private Double w,h;

    private Stage dialogStage;

    @FXML
    private Hyperlink nameLabel;
    @FXML
    private TreeView<String> menuTree;
    @FXML
    private BorderPane layout;
    @FXML
    private Button newTransactionButton;

    @FXML
    private void handleLogout() {
        Main.logout();
    }

    @FXML
    public void initialize() {

        // Ablakot vez�rl? gombokhoz
        rec2 = Screen.getPrimary().getVisualBounds();
        w = 0.1;
        h = 0.1;
        Platform.runLater(() -> {
            dialogStage.setMaximized(true);
            dialogStage.setHeight(rec2.getHeight());
            maximizeButton.getStylesheets().add("decoration-button-restore");
            resizeButton.setVisible(false);
        });

        // Felhaszn�l� nev�nek ki�r�sa
        String userName = Main.getLoggedUser().getFirstname() + " " + Main.getLoggedUser().getLastname();
        nameLabel.setText( userName );

        // Icon beolvas�sa
        //newTransactionButton.setGraphic( new ImageView( new Image( Main.class.getResourceAsStream("../imgs/new_transaction.png") )));

        // Menü létrehozása
        menuTree.setShowRoot(false);
        TreeItem<String> root = new TreeItem<>("");
        menuTree.setRoot(root);
        menuTree.getSelectionModel().selectedItemProperty().addListener(listener);

        ChangeListener<Boolean> expandedListener = (obs, wasExpanded, isNowExpanded) -> {
            if( isNowExpanded ) {
                ReadOnlyProperty<?> expandedProperty = (ReadOnlyProperty<?>) obs;
                Object itemThatWasJustExpanded = expandedProperty.getBean();
                for( TreeItem<String> item : menuTree.getRoot().getChildren() ) {
                    if( item != itemThatWasJustExpanded ) {
                        item.setExpanded(false);
                    }
                }
            }
        };

        TreeItem<String> accountsMenu = new TreeItem<>("Sz�ml�k");
        accountsMenu.expandedProperty().addListener(expandedListener);

        TreeItem<String> newAccountsMenu = new TreeItem<>("�j sz�mla l�trehoz�sa");
        TreeItem<String> listAccountsMenu = new TreeItem<>("Sz�ml�k list�z�as");
        accountsMenu.getChildren().addAll(newAccountsMenu, listAccountsMenu);

        TreeItem<String> orderMenu = new TreeItem<>("Tranzakci�k");
        orderMenu.expandedProperty().addListener(expandedListener);
        TreeItem<String> newHufOrderMenu = new TreeItem<>("�j forint megb�z�s");
        TreeItem<String> newDevOrderMenu = new TreeItem<>("�j deviza megb�z�s");
        TreeItem<String> listOrderMenu = new TreeItem<>("Tranzakci�k list�z�sa");
        orderMenu.getChildren().addAll(newHufOrderMenu, newDevOrderMenu, listOrderMenu);

        TreeItem<String> statMenu = new TreeItem<>("Kimutat�s");
        statMenu.expandedProperty().addListener(expandedListener);
        TreeItem<String> listMonthStatMenu = new TreeItem<>("Havi kimutat�s");
        TreeItem<String> sysnStatMenu = new TreeItem<>("Szinkroniz�l�s webbanki adatokkal");
        statMenu.getChildren().addAll(listMonthStatMenu, sysnStatMenu);

        root.getChildren().addAll(accountsMenu, orderMenu, statMenu);

        PseudoClass subElementPseudoClass = PseudoClass.getPseudoClass("sub-tree-item");

        menuTree.setCellFactory( tv -> {
            TreeCell<String> cell = new TreeCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setDisclosureNode(null);

                    if( empty ) {
                        setText("");
                        setGraphic(null);
                    } else {
                        setText(item);
                    }
                }
            };

            cell.treeItemProperty().addListener((obs, oldTreeItem, newTreeItem) -> {
                cell.pseudoClassStateChanged(subElementPseudoClass, newTreeItem != null && newTreeItem.getParent() != cell.getTreeView().getRoot());
            });

            return cell;
        });

        try {
            FXMLLoader loader = new FXMLLoader( Main.class.getResource("../layout/PersonalSummary.fxml") );
            AnchorPane pane = (AnchorPane) loader.load();

            layout.setCenter(pane);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @FXML
    private void handleShowPersonalSummary() {
        listener.readPersonalDataLayout();
    }

    @FXML
    private void handleNewTransaction() {

    }

    @FXML
    private void handleClose() {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void handleMaximize() {

    }

    @FXML
    private void handleMinimize() {
        if( dialogStage.isMaximized() ) {
            if( w == rec2.getWidth() && h == rec2.getHeight() ) {
                dialogStage.setMaximized(false);
                dialogStage.setHeight(450);
                dialogStage.setWidth(750);
                dialogStage.centerOnScreen();
                maximizeButton.getStylesheets().remove("decoration-button-restore");
                resizeButton.setVisible(true);
            } else {
                dialogStage.setMaximized(false);
                maximizeButton.getStylesheets().remove("decoration-button-restore");
                resizeButton.setVisible(true);
            }
        }
    }

    @FXML
    private void handleResize() {

    }

    @FXML
    private void handleFullScreen() {

    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setLayout(BorderPane layout) {
        this.layout = layout;
    }

    private class TreeSelectionListener implements ChangeListener<TreeItem<String>> {
        @Override
        public void changed(ObservableValue<? extends TreeItem<String>> observable, TreeItem<String> oldValue, TreeItem<String> newValue) {
            final String value = newValue.getValue();
            switch (value) {
                case "Sz�ml�k list�z�as":
                    readAccountListLayout();
                    break;
                case "�j forint megb�z�s":
                    readNewHufTransaction();
                    break;
                case "�j sz�mla l�trehoz�sa":
                    readNewAccount();
                    break;
                case "Tranzakci�k list�z�sa":
                    readListTransactions();
                    break;
                case "Szinkroniz�l�s webbanki adatokkal":
                    readSyncData();
                    break;
            }
        }

        private void readListTransactions() {
            try {
                FXMLLoader loader = new FXMLLoader( Main.class.getResource("../layout/ListTransactions.fxml") );
                AnchorPane pane = (AnchorPane) loader.load();

                layout.setCenter(pane);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        private void readSyncData() {
            try {
                FXMLLoader loader = new FXMLLoader( Main.class.getResource("../layout/SyncData.fxml") );
                AnchorPane pane = (AnchorPane) loader.load();

                layout.setCenter(pane);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        private void readNewAccount() {
            try {
                FXMLLoader loader = new FXMLLoader( Main.class.getResource("../layout/NewAccount.fxml") );
                AnchorPane pane = (AnchorPane) loader.load();

                layout.setCenter(pane);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        private void readNewHufTransaction() {
            try {
                FXMLLoader loader = new FXMLLoader( Main.class.getResource("../layout/NewHufTransaction.fxml") );
                AnchorPane pane = (AnchorPane) loader.load();

                layout.setCenter(pane);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        private void readAccountListLayout() {
            try {
                FXMLLoader loader = new FXMLLoader( Main.class.getResource("../layout/AccountList.fxml") );
                AnchorPane pane = (AnchorPane) loader.load();

                layout.setCenter(pane);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        private void readPersonalDataLayout() {
            try {
                FXMLLoader loader = new FXMLLoader( Main.class.getResource("../layout/PersonalSummary.fxml") );
                AnchorPane pane = (AnchorPane) loader.load();

                layout.setCenter(pane);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
