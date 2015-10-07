package pl.model;

import javax.persistence.*;

@Entity
@Table(name = "Currency")
public class Currency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "code", nullable = false, updatable = false, length = 3)
    private String code;
    @Column(name = "name", nullable = false, updatable = false, length = 50)
    private String name;
    @Column(name = "eng_name", nullable = false, updatable = false, length = 50)
    private String engName;

    public Currency() {

    }

    public Currency(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Currency)) return false;

        Currency currency = (Currency) o;

        if (!code.equals(currency.code)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public String toString() {
        return code;
    }
}
