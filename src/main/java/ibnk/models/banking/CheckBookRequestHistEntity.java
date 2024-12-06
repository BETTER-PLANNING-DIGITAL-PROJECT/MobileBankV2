package ibnk.models.banking;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;
import java.util.Objects;

@Entity
@Data
@Table(name = "CheckBookRequestHist", schema = "dbo", catalog = "Banking")
public class CheckBookRequestHistEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false, precision = 0)
    private int id;
    @Basic
    @Column(name = "CpteJumelle", nullable = true, length = 15)
    private String cpteJumelle;
    @Basic
    @Column(name = "Client", nullable = false, length = 6)
    private String client;
    @Basic
    @Column(name = "Employe", nullable = true, length = 250)
    private String employe;
    @Basic
    @Column(name = "Description", nullable = true, length = 250)
    private String description;
    @Basic
    @Column(name = "Agence", nullable = true, length = 5)
    private String agence;
    @Basic
    @Column(name = "DateCreation", nullable = true)
    private Date dateCreation;
    @Basic
    @Column(name = "Nombrepage", nullable = true, length = 100)
    private String nombrepage;
    @Basic
    @Column(name = "Statut", nullable = true, length = 50)
    private String statut;
    @Basic
    @Column(name = "RejectReason", nullable = true, length = 350)
    private String rejectReason;



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CheckBookRequestHistEntity that = (CheckBookRequestHistEntity) o;
        return id == that.id && Objects.equals(cpteJumelle, that.cpteJumelle) && Objects.equals(client, that.client) && Objects.equals(employe, that.employe) && Objects.equals(description, that.description) && Objects.equals(agence, that.agence) && Objects.equals(dateCreation, that.dateCreation) && Objects.equals(nombrepage, that.nombrepage) && Objects.equals(statut, that.statut) && Objects.equals(rejectReason, that.rejectReason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cpteJumelle, client, employe, description, agence, dateCreation, nombrepage, statut, rejectReason);
    }
}
