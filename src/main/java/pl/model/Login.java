package pl.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "pl_login")
public class Login implements Comparable<Login> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;
    @Column(name = "ip", nullable = false, length = 50)
    private String ip;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date", nullable = false)
    private Date date;
    @Column(name = "success", nullable = false)
    private boolean success;

    public Login() {

    }

    public Login(User user, String ip, Date date, boolean success) {
        this.user = user;
        this.ip = ip;
        this.date = date;
        this.success = success;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Login)) return false;

        Login login = (Login) o;

        if (!date.equals(login.date)) return false;
        if (!ip.equals(login.ip)) return false;
        if (!user.equals(login.user)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 31 * ip.hashCode();
        result = 31 * result + date.hashCode();
        return result;
    }

    @Override
    public int compareTo(Login o) {
        return getDate().compareTo(o.getDate());
    }
}
