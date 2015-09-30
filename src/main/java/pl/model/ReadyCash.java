package pl.model;

import javax.persistence.*;

@Entity
@Table(name = "readycash")
public class ReadyCash {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.EAGER, mappedBy = "readycash", cascade = CascadeType.ALL)
    private User owner;
    @Column(name = "money", nullable = false)
    private float money = 0.0f;

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
}
