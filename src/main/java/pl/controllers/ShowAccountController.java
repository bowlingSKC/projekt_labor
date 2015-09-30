package pl.controllers;

import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.hibernate.Query;
import org.hibernate.Session;
import pl.jpa.SessionUtil;
import pl.model.Account;
import pl.model.Transaction;
import pl.model.TransactionType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ShowAccountController {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy. MM. dd.");
    private Account account = null;

    @FXML
    private Label accNoLabel;
    @FXML
    private Label bankLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private Label regDateLabel;
    @FXML
    private Label sumLabel;

    @FXML
    private TreeTableView<Transaction> treeTableView;
    @FXML
    private TreeTableColumn<Transaction, Date> dateColumn;
    @FXML
    private TreeTableColumn<Transaction, String> typeColumn;
    @FXML
    private TreeTableColumn<Transaction, Float> moneyColumn;
    @FXML
    private TreeTableColumn<Transaction, String> toColumn;
    @FXML
    private TreeTableColumn<Transaction, String> commentColumn;

    @FXML
    public void initialize() {
        // dátum
        dateColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<Transaction, Date> param) ->
            new ReadOnlyObjectWrapper<>(param.getValue().getValue().getDate())
        );

        dateColumn.setCellFactory(cell -> new TreeTableCell<Transaction, Date>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);

                if( item == null ) {
                    setText("");
                } else {
                    setText( sdf.format(item) );
                }
            }
        });

        // típus
        typeColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<Transaction, String> param) ->
            new ReadOnlyStringWrapper(param.getValue().getValue().getType().getName())
        );

        // összeg
        moneyColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<Transaction, Float> param) ->
                new ReadOnlyObjectWrapper<>(param.getValue().getValue().getMoney())
        );
        moneyColumn.setCellFactory(cell -> new TreeTableCell<Transaction, Float>() {
            @Override
            protected void updateItem(Float item, boolean empty) {
                super.updateItem(item, empty);

                if( item == null || item == 0 ) {
                    setText("");
                } else {
                    setText( Float.toString(item) );
                }
            }
        });

        // kinek
        toColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<Transaction, String> param) ->
                        new ReadOnlyStringWrapper(param.getValue().getValue().getAnotherAccount())
        );

        // komment
        commentColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<Transaction, String> param) ->
                        new ReadOnlyStringWrapper(param.getValue().getValue().getComment())
        );
    }

    @FXML
    private void getTransactionsFromDatabase() {
        Map<Date, List<Transaction>> datas = new HashMap<>();
        Session session = SessionUtil.getSession();
        Query outQuery = session.createQuery("from Transaction where account = :account");
        Query inQuery = session.createQuery("from Transaction where anotherAccount = :other");
        outQuery.setParameter("account", account.getAccountNumber());
        inQuery.setParameter("other", account.getAccountNumber());
        for( Object transactionObj : outQuery.list() ) {
            Transaction transaction = (Transaction) transactionObj;
            if( !datas.containsKey(transaction.getDate()) ) {
                datas.put(transaction.getDate(), new ArrayList<>());
            }
            datas.get(transaction.getDate()).add(transaction);
        }
        for( Object transactionObj : inQuery.list() ) {
            Transaction transaction = (Transaction) transactionObj;
            if( !datas.containsKey(transaction.getDate()) ) {
                datas.put(transaction.getDate(), new ArrayList<>());
            }
            datas.get(transaction.getDate()).add(transaction);
        }
        session.close();


        TreeItem<Transaction> root = new TreeItem<>( new Transaction() );
        treeTableView.setRoot(root);
        treeTableView.setShowRoot(false);

        for(Map.Entry<Date, List<Transaction>> entry : datas.entrySet()) {
            TreeItem<Transaction> transactionTreeItem = new TreeItem<>( new Transaction(entry.getKey()) );
            for( Transaction transaction : entry.getValue() ) {
                transactionTreeItem.getChildren().add(new TreeItem<>( transaction ));
            }
            root.getChildren().add(transactionTreeItem);
        }
    }

    public void setAccount(Account account) throws ParseException {
        this.account = account;
        accNoLabel.setText( account.getAccountNumber() );
        bankLabel.setText( account.getBank().getName() );
        nameLabel.setText( account.getName() );
        regDateLabel.setText( sdf.format(account.getCreatedDate()) );
        sumLabel.setText( Float.toString(account.getMoney()) );
    }

}
