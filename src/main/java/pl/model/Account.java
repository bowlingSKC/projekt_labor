package pl.model;

import org.hibernate.*;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "pl_account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", nullable = false, length = 24, updatable = false)
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "account")
    private Set<AccountTransaction> accountTransactions = new HashSet<>(0);

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "account")
    private Set<Pocket> pockets = new HashSet<>(0);

    public Account() {

    }

    // Készpénz tárolásának
    public Account(String name) {
        this.name = name;
    }

    public Account(String accountNumber, String name, float money, Date createdDate, User owner, Bank bank, Currency currency) {
        this.accountNumber = accountNumber;
        this.name = name;
        this.money = money;
        this.createdDate = createdDate;
        this.owner = owner;
        this.bank = bank;
        this.currency = currency;
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

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Set<AccountTransaction> getAccountTransactions() {
        return accountTransactions;
    }

    public void setAccountTransactions(Set<AccountTransaction> accountTransactions) {
        this.accountTransactions = accountTransactions;
    }

    public Set<Pocket> getPockets() {
        return pockets;
    }

    public void setPockets(Set<Pocket> pockets) {
        this.pockets = pockets;
    }

    public AccountTransaction getLatestTransaction() {
        AccountTransaction tmp = null;
        for( AccountTransaction tra : accountTransactions ) {
            if( tmp == null ) {
                tmp = tra;
            } else {
                if( tra.getDate().compareTo(tmp.getDate()) == 0 ) {
                    if( tra.getId() > tmp.getId() ) {
                        tmp = tra;
                    }
                } else if(tra.getDate().compareTo(tmp.getDate()) == 1) {
                        tmp = tra;
                }
            }
        }
        return tmp;
    }

    public void tickAllTransactions() {
        for(AccountTransaction transaction : accountTransactions) {
            transaction.tick();
        }
    }

    public void delete(Session session) {
        accountTransactions.forEach(session::delete);
        session.delete(this);
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
        return name + " [" + accountNumber.substring(0, 8) + "-" + accountNumber.substring(8, 16) + "-" + accountNumber.substring(16) + "]";
    }


}
