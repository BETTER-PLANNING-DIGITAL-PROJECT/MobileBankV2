package ibnk.models.banking;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;
import java.util.Objects;

@Entity
@Data
@Table(name = "CheqTrait", schema = "dbo", catalog = "Banking")
@IdClass(CheqTraitEntityPK.class)
public class CheqTraitEntity {
    @Basic
    @Column(name = "Agence", nullable = true, length = 5)
    private String agence;
    @Basic
    @Column(name = "DateOP", nullable = true)
    private Date dateOp;
    @Basic
    @Column(name = "CptejumelleCC", nullable = true, length = 20)
    private String cptejumelleCc;
    @Id
    @Column(name = "Tracer", nullable = false, length = 30)
    private String tracer;
    @Basic
    @Column(name = "SerieChequier", nullable = true, length = 100)
    private String serieChequier;
    @Basic
    @Column(name = "Numero", nullable = true, length = 20)
    private String numero;
    @Basic
    @Column(name = "Montant", nullable = true, precision = 0)
    private Double montant;
    @Basic
    @Column(name = "DateCreation", nullable = true)
    private Date dateCreation;
    @Basic
    @Column(name = "Denomination", nullable = true, length = 100)
    private String denomination;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "Serie", nullable = false, precision = 0)
    private int serie;
    @Basic
    @Column(name = "rowguid", nullable = false)
    private Object rowguid;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CheqTraitEntity that = (CheqTraitEntity) o;
        return serie == that.serie && Objects.equals(agence, that.agence) && Objects.equals(dateOp, that.dateOp) && Objects.equals(cptejumelleCc, that.cptejumelleCc) && Objects.equals(tracer, that.tracer) && Objects.equals(serieChequier, that.serieChequier) && Objects.equals(numero, that.numero) && Objects.equals(montant, that.montant) && Objects.equals(dateCreation, that.dateCreation) && Objects.equals(denomination, that.denomination) && Objects.equals(rowguid, that.rowguid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agence, dateOp, cptejumelleCc, tracer, serieChequier, numero, montant, dateCreation, denomination, serie, rowguid);
    }
}
