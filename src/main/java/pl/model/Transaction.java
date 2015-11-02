package pl.model;


import javax.persistence.*;
import java.util.Date;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false, updatable = false)
    protected TransactionType type;

    @Temporal(TemporalType.DATE)
    @Column(name = "date", nullable = false, updatable = false)
    protected Date date = new Date();

    @Column(name = "money", nullable = false, updatable = false)
    protected float money;

    @ManyToOne
    @JoinColumn(name = "currency_id", nullable = false, updatable = false)
    protected Currency currency;

    @Column(name = "comment")
    protected String comment;

    public Transaction() {

    }

    public Transaction(TransactionType type, Date date, float money, Currency currency, String comment) {
        this.type = type;
        this.date = date;
        this.money = money;
        this.currency = currency;
        this.comment = comment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public float getMoney() {
        return money;
    }

    public void setMoney(float money) {
        this.money = money;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
