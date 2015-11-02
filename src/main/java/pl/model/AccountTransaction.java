package pl.model;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "account_transaction")
public class AccountTransaction extends Transaction {

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = true)
    private Account account;

    @Column(name = "another_account_no", nullable = true, updatable = false, length = 24)
    private String anotherAccount;

    @ManyToOne
    @JoinColumn(name = "before_id")
    private AccountTransaction beforeAccountTransaction;

    @OneToMany(mappedBy = "beforeAccountTransaction")
    private Set<AccountTransaction> tmp = new HashSet<>(0);

    public AccountTransaction() {

    }

    public AccountTransaction(Account account, String anotherAccount, float money, Date date, String comment, TransactionType type, Currency curr) {
        this.account = account;
        this.anotherAccount = anotherAccount;
        this.money = money;
        this.date = date;
        this.comment = comment;
        this.type = type;
        this.currency = curr;
    }

    //Currency nélküli konstruktor
    public AccountTransaction(Account account, String anotherAccount, float money, Date date, String comment, TransactionType type) {
        this.account = account;
        this.anotherAccount = anotherAccount;
        this.money = money;
        this.date = date;
        this.comment = comment;
        this.type = type;
    }

    // konstuktor a TreeTableView-hoz
    public AccountTransaction(Date date) {
        this.date = date;
        this.anotherAccount = "";
        this.money = 0.0f;
        this.comment = "";
        this.type = new TransactionType("");
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account from) {
        this.account = from;
    }

    public String getAnotherAccount() {
        return anotherAccount;
    }

    public void setAnotherAccount(String to) {
        this.anotherAccount = to;
    }

    public float getMoney() {
        return money;
    }

    public void setMoney(float money) {
        this.money = money;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public Currency getCurrency() {return currency;}

    public void setCurrency(Currency currency) {this.currency = currency;}

    public AccountTransaction getBeforeAccountTransaction() {
        return beforeAccountTransaction;
    }

    public void setBeforeAccountTransaction(AccountTransaction beforeAccountTransaction) {
        this.beforeAccountTransaction = beforeAccountTransaction;
    }

    public Set<AccountTransaction> getTmp() {
        return tmp;
    }

    public void setTmp(Set<AccountTransaction> tmp) {
        this.tmp = tmp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountTransaction)) return false;

        AccountTransaction that = (AccountTransaction) o;

        if (Float.compare(that.money, money) != 0) return false;
        if (account != null ? !account.equals(that.account) : that.account != null) return false;
        if (anotherAccount != null ? !anotherAccount.equals(that.anotherAccount) : that.anotherAccount != null)
            return false;
        if (comment != null ? !comment.equals(that.comment) : that.comment != null) return false;
        if (!date.equals(that.date)) return false;
        if (!type.equals(that.type)) return false;

        return true;
    }

}
