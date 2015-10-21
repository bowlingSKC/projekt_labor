package pl.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "debit")
public class Debit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "money", nullable = false)
    private float money;
    @ManyToOne
    private Currency currency;
    @Column(name = "deadline")
    @Temporal(TemporalType.DATE)
    private Date deadline;
    @Column(name = "description")
    private String description;

    public Debit() {

    }

    public Debit(User owner, String name, float money, Currency currency, Date deadline, String description) {
        this.owner = owner;
        this.name = name;
        this.money = money;
        this.currency = currency;
        this.deadline = deadline;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Debit)) return false;

        Debit debit = (Debit) o;

        if (Float.compare(debit.money, money) != 0) return false;
        if (!currency.equals(debit.currency)) return false;
        if (deadline != null ? !deadline.equals(debit.deadline) : debit.deadline != null) return false;
        if (description != null ? !description.equals(debit.description) : debit.description != null) return false;
        if (!name.equals(debit.name)) return false;
        if (!owner.equals(debit.owner)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = owner.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (money != +0.0f ? Float.floatToIntBits(money) : 0);
        result = 31 * result + currency.hashCode();
        result = 31 * result + (deadline != null ? deadline.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }
}
