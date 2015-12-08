package pl.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("C")
@Table(name = "pl_readycash")
public class ReadyCash {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = true)
    private User owner;

    @Column(name = "money", nullable = false)
    private float money = 0.0f;

    @ManyToOne
    @JoinColumn(name = "currency_id", nullable = true)
    private Currency currency;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "cash", cascade = CascadeType.ALL)
    private Set<CashTransaction> cashTransaction = new HashSet<>(0);

    public ReadyCash() {
        money = 0.0f;
    }

    public ReadyCash(User owner, float money) {
        this.owner = owner;
        this.money = money;
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

    public Set<CashTransaction> getCashTransaction() {
        return cashTransaction;
    }

    public void setCashTransaction(Set<CashTransaction> cashTransaction) {
        this.cashTransaction = cashTransaction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReadyCash)) return false;

        ReadyCash readyCash = (ReadyCash) o;

        if (Float.compare(readyCash.money, money) != 0) return false;
        if (!owner.equals(readyCash.owner)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = owner.hashCode();
        result = 31 * result + (money != +0.0f ? Float.floatToIntBits(money) : 0);
        return result;
    }

    public CashTransaction getLatestTransaction() {
        CashTransaction transaction = null;
        for( CashTransaction tra : cashTransaction ) {
            if( transaction == null ) {
                transaction = tra;
            } else {
                if( tra.getDate().compareTo(transaction.getDate()) == 0 ) {
                    if( tra.getId() > transaction.getId() ) {
                        transaction = tra;
                    }
                } else if(tra.getDate().compareTo(transaction.getDate()) == 1) {
                    transaction = tra;
                }
            }
        }
        return transaction;
    }
}
