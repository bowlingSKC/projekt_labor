package pl.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "pl_bank")
public class Bank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", nullable = false, length = 100, unique = true)
    private String name;
    @Column(name = "phone_number", nullable = true)
    private String phoneNumber;
    @Column(name = "giro", length = 3, nullable = false, updatable = false, unique = true)
    private String giro;
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "bank")
    private Set<Account> accounts;

    public Bank() {
    }

    public Bank(String name) {
        this.name = name;
    }

    public Bank(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getGiro() {
        return giro;
    }

    public void setGiro(String giro) {
        this.giro = giro;
    }

    public Set<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(Set<Account> accounts) {
        this.accounts = accounts;
    }

    public float getAccountNumber() {
        return accounts.size();
    }

    public float getAllMoney() {
        float sum = 0;
        for(Account account : accounts) {
            sum += account.getMoney();
        }
        return sum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bank)) return false;

        Bank bank = (Bank) o;

        if (giro != null ? !giro.equals(bank.giro) : bank.giro != null) return false;
        if (name != null ? !name.equals(bank.name) : bank.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
