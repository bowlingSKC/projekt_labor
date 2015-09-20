package pl.model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "accountNumber", nullable = false, length = 24, updatable = false)
    private String accountNumber;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "money", nullable = false)
    private float money;
    @Temporal(TemporalType.DATE)
    @Column(name = "created", nullable = false)
    private Date createdDate;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
    @ManyToOne
    @JoinColumn(name = "bank_id", nullable = false)
    private Bank bank;
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "account")
    private Set<MoneyGroup> moneyGroups;
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "from")
    private Set<Transaction> fromTransactions;

    public Account() {

    }

    public Account(String accountNumber, String name, float money, Date createdDate, User owner, Bank bank) {
        this.accountNumber = accountNumber;
        this.name = name;
        this.money = money;
        this.createdDate = createdDate;
        this.owner = owner;
        this.bank = bank;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
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

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    public Set<MoneyGroup> getMoneyGroups() {
        return moneyGroups;
    }

    public void setMoneyGroups(Set<MoneyGroup> moneyGroups) {
        this.moneyGroups = moneyGroups;
    }

    public Set<Transaction> getFromTransactions() {
        return fromTransactions;
    }

    public void setFromTransactions(Set<Transaction> fromTransactions) {
        this.fromTransactions = fromTransactions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;

        Account account = (Account) o;

        if (!accountNumber.equals(account.accountNumber)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = accountNumber != null ? accountNumber.hashCode() : 0;
        result = 31 * result + (money != +0.0f ? Float.floatToIntBits(money) : 0);
        return result;
    }

    @Override
    public String toString() {
        return name + " [" + accountNumber + "]";
    }
}
