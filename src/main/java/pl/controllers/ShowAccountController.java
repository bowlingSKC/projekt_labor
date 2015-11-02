package pl.controllers;

import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.hibernate.Query;
import org.hibernate.Session;
import pl.jpa.SessionUtil;
import pl.model.Account;
import pl.model.AccountTransaction;

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
    private TreeTableView<AccountTransaction> treeTableView;
    @FXML
    private TreeTableColumn<AccountTransaction, Date> dateColumn;
    @FXML
    private TreeTableColumn<AccountTransaction, String> typeColumn;
    @FXML
    private TreeTableColumn<AccountTransaction, Float> moneyColumn;
    @FXML
    private TreeTableColumn<AccountTransaction, String> toColumn;
    @FXML
    private TreeTableColumn<AccountTransaction, String> commentColumn;

    @FXML
    public void initialize() {
        // d�tum
        dateColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<AccountTransaction, Date> param) ->
            new ReadOnlyObjectWrapper<>(param.getValue().getValue().getDate())
        );

        dateColumn.setCellFactory(cell -> new TreeTableCell<AccountTransaction, Date>() {
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

        // t�pus
        typeColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<AccountTransaction, String> param) ->
            new ReadOnlyStringWrapper(param.getValue().getValue().getType().getName())
        );

        // �sszeg
        moneyColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<AccountTransaction, Float> param) ->
                new ReadOnlyObjectWrapper<>(param.getValue().getValue().getMoney())
        );
        moneyColumn.setCellFactory(cell -> new TreeTableCell<AccountTransaction, Float>() {
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
        toColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<AccountTransaction, String> param) ->
                        new ReadOnlyStringWrapper(param.getValue().getValue().getAnotherAccount())
        );

        // komment
        commentColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<AccountTransaction, String> param) ->
                        new ReadOnlyStringWrapper(param.getValue().getValue().getComment())
        );
    }

    @FXML
    private void getTransactionsFromDatabase() {
        Map<Date, List<AccountTransaction>> datas = new HashMap<>();
        Session session = SessionUtil.getSession();
        Query outQuery = session.createQuery("from AccountTransaction where account = :account");
        Query inQuery = session.createQuery("from AccountTransaction where anotherAccount = :other");
        outQuery.setParameter("account", account.getAccountNumber());
        inQuery.setParameter("other", account.getAccountNumber());
        for( Object transactionObj : outQuery.list() ) {
            AccountTransaction accountTransaction = (AccountTransaction) transactionObj;
            if( !datas.containsKey(accountTransaction.getDate()) ) {
                datas.put(accountTransaction.getDate(), new ArrayList<>());
            }
            datas.get(accountTransaction.getDate()).add(accountTransaction);
        }
        for( Object transactionObj : inQuery.list() ) {
            AccountTransaction accountTransaction = (AccountTransaction) transactionObj;
            if( !datas.containsKey(accountTransaction.getDate()) ) {
                datas.put(accountTransaction.getDate(), new ArrayList<>());
            }
            datas.get(accountTransaction.getDate()).add(accountTransaction);
        }
        session.close();


        TreeItem<AccountTransaction> root = new TreeItem<>( new AccountTransaction() );
        treeTableView.setRoot(root);
        treeTableView.setShowRoot(false);

        for(Map.Entry<Date, List<AccountTransaction>> entry : datas.entrySet()) {
            TreeItem<AccountTransaction> transactionTreeItem = new TreeItem<>( new AccountTransaction(entry.getKey()) );
            for( AccountTransaction accountTransaction : entry.getValue() ) {
                transactionTreeItem.getChildren().add(new TreeItem<>(accountTransaction));
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
