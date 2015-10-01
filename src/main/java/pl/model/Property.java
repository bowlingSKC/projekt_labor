package pl.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "property")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    @Column(name = "bought", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date bought;
    @Column(name = "comment")
    private String comment;
    @OneToOne
    @PrimaryKeyJoinColumn
    private Depriciation depriciation;

    public Property() {

    }

    public Property(User owner, String name, Date bought, String comment, Depriciation depriciation) {
        this.owner = owner;
        this.name = name;
        this.bought = bought;
        this.comment = comment;
        this.depriciation = depriciation;
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

    public Date getBought() {
        return bought;
    }

    public void setBought(Date bought) {
        this.bought = bought;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Depriciation getDepriciation() {
        return depriciation;
    }

    public void setDepriciation(Depriciation depriciation) {
        this.depriciation = depriciation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Property)) return false;

        Property property = (Property) o;

        if (bought != null ? !bought.equals(property.bought) : property.bought != null) return false;
        if (depriciation != null ? !depriciation.equals(property.depriciation) : property.depriciation != null)
            return false;
        if (name != null ? !name.equals(property.name) : property.name != null) return false;
        if (owner != null ? !owner.equals(property.owner) : property.owner != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = owner != null ? owner.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (bought != null ? bought.hashCode() : 0);
        result = 31 * result + (depriciation != null ? depriciation.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Property{" +
                "id=" + id +
                ", owner=" + owner +
                ", name='" + name + '\'' +
                ", bought=" + bought +
                ", comment='" + comment + '\'' +
                ", depriciation=" + depriciation +
                '}';
    }
}
