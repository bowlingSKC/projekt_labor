package pl.model;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "cash_transaction")
public class CashTransaction extends Transaction {

    @ManyToOne
    @JoinColumn(name = "before_id")
    private CashTransaction beforeTransaction;

    @ManyToOne
    private ReadyCash cash;

    @OneToMany(mappedBy = "beforeTransaction")
    private Set<CashTransaction> tmp  = new HashSet<>(0);

    public CashTransaction() {
    }

    public CashTransaction(TransactionType type, Date date, float money, Currency currency, String comment) {
        super(type, date, money, currency, comment);
    }

    public CashTransaction getBeforeTransaction() {
        return beforeTransaction;
    }

    public void setBeforeTransaction(CashTransaction beforeTransaction) {
        this.beforeTransaction = beforeTransaction;
    }

    public Set<CashTransaction> getTmp() {
        return tmp;
    }

    public void setTmp(Set<CashTransaction> tmp) {
        this.tmp = tmp;
    }

    public ReadyCash getCash() {
        return cash;
    }

    public void setCash(ReadyCash cash) {
        this.cash = cash;
    }
}
