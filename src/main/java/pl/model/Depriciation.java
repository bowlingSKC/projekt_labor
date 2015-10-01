package pl.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "depriciation")
public abstract class Depriciation {

    @Id
    protected Long id;
    @Column(name = "purchaseValue", nullable = false, updatable = false)
    protected float purchaseValue;
    @Column(name = "residualValue", nullable = false, updatable = false)
    protected float residualValue;
    @Column(name = "startDate", nullable = false, updatable = false)
    protected Date startDate;
    @Column(name = "type")
    protected String type;

    public Depriciation() {

    }

    public Depriciation(float purchaseValue, float residualValue, Date startDate, String type) {
        this.purchaseValue = purchaseValue;
        this.residualValue = residualValue;
        this.startDate = startDate;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public float getPurchaseValue() {
        return purchaseValue;
    }

    public void setPurchaseValue(float purchaseValue) {
        this.purchaseValue = purchaseValue;
    }

    public float getResidualValue() {
        return residualValue;
    }

    public void setResidualValue(float residualValue) {
        this.residualValue = residualValue;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }



}
