package pl.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "account_no", nullable = true, updatable = false, length = 24)
    private String account;
    @Column(name = "another_account_no", nullable = true, updatable = false, length = 24)
    private String anotherAccount;
    @Column(name = "money", nullable = false, updatable = false)
    private float money;
    @Temporal(TemporalType.DATE)
    @Column(name = "date", nullable = false, updatable = false)
    private Date date;
    @Column(name = "comment")
    private String comment;
    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false, updatable = false)
    private TransactionType type;

    public Transaction() {

    }

    public Transaction(String account, String anotherAccount, float money, Date date, String comment, TransactionType type) {
        this.account = account;
        this.anotherAccount = anotherAccount;
        this.money = money;
        this.date = date;
        this.comment = comment;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String from) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;

        Transaction that = (Transaction) o;

        if (Float.compare(that.money, money) != 0) return false;
        if (account != null ? !account.equals(that.account) : that.account != null) return false;
        if (anotherAccount != null ? !anotherAccount.equals(that.anotherAccount) : that.anotherAccount != null)
            return false;
        if (comment != null ? !comment.equals(that.comment) : that.comment != null) return false;
        if (!date.equals(that.date)) return false;
        if (!type.equals(that.type)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = account != null ? account.hashCode() : 0;
        result = 31 * result + (anotherAccount != null ? anotherAccount.hashCode() : 0);
        result = 31 * result + (money != +0.0f ? Float.floatToIntBits(money) : 0);
        result = 31 * result + date.hashCode();
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        result = 31 * result + type.hashCode();
        return result;
    }
}
