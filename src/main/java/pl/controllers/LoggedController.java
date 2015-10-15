package pl.controllers;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import pl.Main;
import pl.bundles.Bundles;

import java.io.IOException;

public class LoggedController {

    private final TreeSelectionListener listener = new TreeSelectionListener();

    @FXML
    private Button logoutButton;

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
    private TreeView<String> menuTree;
    @FXML
    private BorderPane layout;
    @FXML
    private Label nameLabel;
    @FXML
    private Label titleLabel;

    @FXML
    public void initialize() {

        logoutButton.setTooltip(new Tooltip( Bundles.getString("logout") ));

        // Ablak vezérléséhez
        rec2 = Screen.getPrimary().getVisualBounds();
        w = 0.1;
        h = 0.1;

        String username = Main.getLoggedUser().getFirstname() + " " + Main.getLoggedUser().getLastname();
        nameLabel.setText( username );
        titleLabel.setText( Bundles.getString("logged.welcome") );

        createMenu();

        try {
            FXMLLoader loader = new FXMLLoader( Main.class.getResource("../layout/PersonalSummary.fxml") );
            AnchorPane pane = loader.load();

            layout.setCenter(pane);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        /*
        // ablak mozgatása
        final Delta dragDelta = new Delta();
        nameLabel.setOnMousePressed(event -> {
            dragDelta.x = dialogStage.getX() - event.getScreenX();
            dragDelta.y = dialogStage.getY() - event.getScreenY();
            dialogStage.getScene().setCursor(Cursor.MOVE);
        });
        nameLabel.setOnMouseReleased(event -> dialogStage.getScene().setCursor(Cursor.HAND));
        nameLabel.setOnMouseDragged(event -> {
            dialogStage.setX( event.getSceneX() +  dragDelta.x);
            dialogStage.setY( event.getSceneY() +  dragDelta.y);
        });
        nameLabel.setOnMouseEntered(event -> {
            if( !event.isPrimaryButtonDown() ) {
                dialogStage.getScene().setCursor(Cursor.HAND);
            }
        });
        nameLabel.setOnMouseExited(event -> {
            if( !event.isPrimaryButtonDown() ) {
                dialogStage.getScene().setCursor(Cursor.DEFAULT);
            }
        });
        */

    }

    private void createMenu() {
        // Menü létrehozása
        menuTree.setShowRoot(false);
        TreeItem<String> root = new TreeItem<>("");
        menuTree.setRoot(root);
        menuTree.getSelectionModel().selectedItemProperty().addListener(listener);

        ChangeListener<Boolean> expandedListener = (obs, wasExpanded, isNowExpanded) -> {
            if( isNowExpanded ) {
                ReadOnlyProperty<?> expandedProperty = (ReadOnlyProperty<?>) obs;
                Object itemThatWasJustExpanded = expandedProperty.getBean();
                menuTree.getRoot().getChildren().stream().filter(item -> item != itemThatWasJustExpanded).forEach(item -> item.setExpanded(false));
            }
        };

        TreeItem<String> cashMenu = new TreeItem<>(Bundles.getString("menu.cash"));
        cashMenu.expandedProperty().addListener(expandedListener);

        TreeItem<String> readyMenu = new TreeItem<>(Bundles.getString("menu.cash.ready"));
        TreeItem<String> accountsMenu = new TreeItem<>(Bundles.getString("menu.cash.account"));
        TreeItem<String> propertiesMenu = new TreeItem<>(Bundles.getString("menu.cash.property"));
        TreeItem<String> listPocketsMenu = new TreeItem<>(Bundles.getString("menu.cash.pocket"));
        cashMenu.getChildren().addAll(readyMenu, accountsMenu, propertiesMenu, listPocketsMenu);

        TreeItem<String> orderMenu = new TreeItem<>(Bundles.getString("menu.transaction"));
        orderMenu.expandedProperty().addListener(expandedListener);
        TreeItem<String> orderMenuItem = new TreeItem<>(Bundles.getString("menu.transaction.transactions"));
        TreeItem<String> listOrderMenu = new TreeItem<>(Bundles.getString("menu.transaction.report"));
        orderMenu.getChildren().addAll(orderMenuItem, listOrderMenu);

        TreeItem<String> importExportMenu = new TreeItem<>(Bundles.getString("menu.importexport"));
        importExportMenu.expandedProperty().addListener(expandedListener);
        TreeItem<String> sysWebbank = new TreeItem<>(Bundles.getString("menu.importexport.syn"));
        importExportMenu.getChildren().addAll(sysWebbank);

        TreeItem<String> settingsMenu = new TreeItem<>(Bundles.getString("menu.settings"));
        settingsMenu.expandedProperty().addListener(expandedListener);
        TreeItem<String> personalSettings = new TreeItem<>(Bundles.getString("menu.settings.personal"));
        TreeItem<String> newPassword = new TreeItem<>(Bundles.getString("menu.settings.newPassword"));
        settingsMenu.getChildren().addAll(personalSettings, newPassword);

        root.getChildren().addAll(cashMenu, orderMenu, importExportMenu, settingsMenu);

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
    }

    @FXML
    private void handleClose() {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void handleMaximize() {
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
        } else {
            dialogStage.setMaximized(true);
            dialogStage.setHeight(rec2.getHeight());
            maximizeButton.getStylesheets().add("decoration-button-restore");
            resizeButton.setVisible(false);
        }
    }

    @FXML
    private void handleMinimize() {
        if( dialogStage.isMaximized() ) {
            w = rec2.getWidth();
            h = rec2.getHeight();
            dialogStage.setMaximized(false);
            dialogStage.setHeight(h);
            dialogStage.setWidth(w);
            dialogStage.centerOnScreen();
            Platform.runLater(() -> {
                dialogStage.setIconified(true);
            });
        } else {
            dialogStage.setIconified(true);
        }
    }

    @FXML
    private void handleResize() {
        // TODO
    }

    @FXML
    private void handleFullScreen() {
        if( dialogStage.isFullScreen() ) {
            dialogStage.setFullScreen(false);
        } else {
            dialogStage.setFullScreen(true);
        }
    }

    @FXML
    private void handleLogout() {
        Main.logout();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
        Platform.runLater(() -> {
            dialogStage.setMaximized(true);
            dialogStage.setHeight(rec2.getHeight());
            maximizeButton.getStylesheets().add("decoration-button-restore");
            resizeButton.setVisible(false);
        });
    }

    public void setLayout(BorderPane layout) {
        this.layout = layout;
    }

    private class TreeSelectionListener implements ChangeListener<TreeItem<String>> {
        @Override
        public void changed(ObservableValue<? extends TreeItem<String>> observable, TreeItem<String> oldValue, TreeItem<String> newValue) {
            final String value = newValue.getValue();
            if( value.equals(Bundles.getString("menu.cash.account")) ) {
                readAccountListLayout();
            } else if( value.equals(Bundles.getString("menu.cash.pocket")) ) {
                readListPockets();
            } else if( value.equals(Bundles.getString("menu.transaction.transactions")) ) {
                readListTransactions();
            } else if( value.equals(Bundles.getString("menu.transaction.report")) ) {
                readListMonth();
            } else if( value.equals(Bundles.getString("menu.importexport.syn")) ) {
                readSyncData();
            } else if( value.equals(Bundles.getString("menu.cash.property")) ) {
                readPropertiesPanel();
            } else if( value.equals(Bundles.getString("menu.cash.ready")) ) {
                readReadyCashPane();
            } else if( value.equals(Bundles.getString("menu.settings.newPassword")) ) {
                readNewPassword();
            }
        }

        private void readNewPassword() {
            try {
                FXMLLoader loader = new FXMLLoader( Main.class.getResource("../layout/NewPassword.fxml"), Bundles.getBundle() );
                AnchorPane pane = loader.load();

                layout.setCenter(pane);
                titleLabel.setText( Bundles.getString("menu.personal.newpass.title") );
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        private void readReadyCashPane() {
            try {
                FXMLLoader loader = new FXMLLoader( Main.class.getResource("../layout/ReadyCash.fxml") );
                AnchorPane pane = loader.load();

                layout.setCenter(pane);
                titleLabel.setText( Bundles.getString("readycash.title") );
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        private void readPropertiesPanel() {
            try {
                FXMLLoader loader = new FXMLLoader( Main.class.getResource("../layout/Properties.fxml") );
                AnchorPane pane = loader.load();

                layout.setCenter(pane);
                titleLabel.setText(Bundles.getString("menu.properties.title"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        private void readListTransactions() {
            try {
                FXMLLoader loader = new FXMLLoader( Main.class.getResource("../layout/ListTransactions.fxml") );
                AnchorPane pane = loader.load();

                layout.setCenter(pane);
                titleLabel.setText(Bundles.getString("menu.transaction"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        private void readListPockets() {
            try {
                FXMLLoader loader = new FXMLLoader( Main.class.getResource("../layout/Pocket.fxml") );
                AnchorPane pane = loader.load();

                layout.setCenter(pane);
                titleLabel.setText(Bundles.getString("menu.cash.pocket"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        private void readSyncData() {
            try {
                FXMLLoader loader = new FXMLLoader( Main.class.getResource("../layout/SyncData.fxml") );
                AnchorPane pane = loader.load();

                layout.setCenter(pane);
                titleLabel.setText( Bundles.getString("menu.syn.title") );
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        private void readListMonth() {
            try {
                FXMLLoader loader = new FXMLLoader( Main.class.getResource("../layout/ListMonth.fxml") );
                AnchorPane pane = loader.load();

                layout.setCenter(pane);
                titleLabel.setText(Bundles.getString("menu.demonstration.title"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        private void readAccountListLayout() {
            try {
                FXMLLoader loader = new FXMLLoader( Main.class.getResource("../layout/AccountList.fxml"), Bundles.getBundle() );
                AnchorPane pane = loader.load();

                layout.setCenter(pane);
                titleLabel.setText(Bundles.getString("cash.bankaccount.title"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

}
