package pl.model;

import org.hibernate.*;
import pl.CurrencyExchange;
import pl.jpa.SessionUtil;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "firstname", nullable = false, length = 50)
    private String firstname;

    @Column(name = "lastname", nullable = false, length = 50)
    private String lastname;

    @Column(name = "email", nullable = false, length = 50, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "salt", nullable = false, length = 20)
    private String salt;

    @Temporal(TemporalType.DATE)
    @Column(name = "registred", nullable = false)
    private Date registredDate;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "owner", cascade = CascadeType.ALL)
    private Set<ReadyCash> readycash;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Login> logins = new HashSet<>(0);

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "owner", cascade = CascadeType.ALL)
    private Set<Account> accounts = new HashSet<>(0);

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "owner", cascade = CascadeType.ALL)
    private Set<Property> properties = new HashSet<>(0);

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "owner", cascade = CascadeType.ALL)
    private Set<Debit> debits = new HashSet<>(0);

    public User() {

    }

    public User(String firstname, String lastname, String email, String password, String salt, Date registredDate) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.salt = salt;
        this.registredDate = registredDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Set<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(Set<Account> accounts) {
        this.accounts = accounts;
    }

    public Date getRegistredDate() {
        return registredDate;
    }

    public void setRegistredDate(Date registredDate) {
        this.registredDate = registredDate;
    }

    public Set<Login> getLogins() {
        return logins;
    }

    public void setLogins(Set<Login> logins) {
        this.logins = logins;
    }

    public Set<ReadyCash> getReadycash() {
        return readycash;
    }

    public void setReadycash(Set<ReadyCash> readycash) {
        this.readycash = readycash;
    }

    public Set<Property> getProperties() {
        return properties;
    }

    public void setProperties(Set<Property> properties) {
        this.properties = properties;
    }

    public Set<Debit> getDebits() {
        return debits;
    }

    public void setDebits(Set<Debit> debits) {
        this.debits = debits;
    }

    public float getAllMoneyInProperties() {
        float money = 0;
        if( properties != null ) {
            for(Property prop : properties) {
                money += prop.getMoney();
            }
        }
        return money;
    }

    public float getAllMoneyFromAccounts() {
        float sum = 0;
        if( accounts != null ) {
            for(Account acc : accounts) {
                sum += acc.getMoney();
            }
        }
        return sum;
    }

    public float getAllMoneyInReadyCash() {
        float sum = 0;
        if( readycash != null ) {
            for(ReadyCash readyCash : readycash) {
                /*
                if( !readyCash.getCurrency().getCode().equals("HUF") ) {
                    sum += CurrencyExchange.toHuf(readyCash.getCurrency(), readyCash.getMoney());
                } else {
                    sum += readyCash.getMoney();
                }
                */
                sum += readyCash.getMoney();
            }
        }
        return sum;
    }

    public float getAllMoney() {
        return getAllMoneyFromAccounts() + getAllMoneyInReadyCash() + getAllMoneyInProperties();
    }

    public float getAllDebitsInValue() {
        float sum = 0;
        for(Debit debit : debits) {
            sum += debit.getMoney();
        }
        return sum;
    }

    private Set<AccountTransaction> getAllTransactions() {
        Set<AccountTransaction> accountTransactions = new HashSet<>();
        for(Account account : accounts) {
            accountTransactions.addAll(account.getAccountTransactions());
        }
        return accountTransactions;
    }

    public CashTransaction getLatestCashTransaction() {
        CashTransaction tmp = null;
        for( ReadyCash rc : readycash ) {
            for( CashTransaction ct : rc.getCashTransaction() ) {
                if( tmp == null ) {
                    tmp = ct;
                } else {
                    if( tmp.getDate().after(ct.getDate()) ) {
                        tmp = ct;
                    }
                }
            }
        }
        return tmp;
    }

    public CashTransaction getLatestCashTransaction(Currency currency) {
        CashTransaction tmp = null;
        for( ReadyCash rc : readycash ) {
            if( rc.getCurrency().equals(currency) ) {
                for( CashTransaction ct : rc.getCashTransaction() ) {
                    if( tmp == null ) {
                        tmp = ct;
                    } else {
                        if( ct.getDate().after(tmp.getDate()) ) {
                            tmp = ct;
                        }
                    }
                }
            }
        }
        return tmp;
    }

    public void normalizeProfil() {
        deleteOldLogins();

        updateProfile();
    }

    private void updateProfile() {
        Session session = SessionUtil.getSession();
        org.hibernate.Transaction tx = session.beginTransaction();
        session.update(this);
        tx.commit();
        session.close();
    }

    private void deleteOldLogins() {
        if( logins.size() > 30 ) {
            List<Login> tmpList = new ArrayList<>(logins);
            tmpList.sort(Login::compareTo);
            tmpList = tmpList.subList(tmpList.size()-30, tmpList.size()-1);
            logins = new HashSet<>(tmpList);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        if (!email.equals(user.email)) return false;

        return true;
    }
}
