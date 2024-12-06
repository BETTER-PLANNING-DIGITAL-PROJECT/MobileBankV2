package ibnk.models.banking;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;
import java.util.Objects;

@Entity
@Data
@Table(name = "Chequier", schema = "dbo", catalog = "Banking")
public class ChequierEntity {
    @Basic
    @Column(name = "Cptejumellecc", nullable = true, length = 30)
    private String cptejumellecc;
    @Basic
    @Column(name = "Denomination", nullable = true, length = 100)
    private String denomination;
    @Basic
    @Column(name = "NbreCheq", nullable = false, precision = 0)
    private int nbreCheq;
    @Basic
    @Column(name = "CheckKey", nullable = false, length = 10)
    private String checkKey;
    @Basic
    @Column(name = "Dserie", nullable = false, length = 20)
    private String dserie;
    @Basic
    @Column(name = "Fserie", nullable = false, length = 20)
    private String fserie;
    @Basic
    @Column(name = "TypeCheque", nullable = true, length = 30)
    private String typeCheque;
    @Basic
    @Column(name = "DatePers", nullable = true)
    private Date datePers;
    @Basic
    @Column(name = "DateSuspension", nullable = true)
    private Date dateSuspension;
    @Basic
    @Column(name = "DateLeveSuspension", nullable = true)
    private Date dateLeveSuspension;
    @Basic
    @Column(name = "Suspension", nullable = true, length = 3)
    private String suspension;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "table_id", nullable = false)
    private int tableId;
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChequierEntity that = (ChequierEntity) o;
        return nbreCheq == that.nbreCheq && tableId == that.tableId && Objects.equals(cptejumellecc, that.cptejumellecc) && Objects.equals(denomination, that.denomination) && Objects.equals(checkKey, that.checkKey) && Objects.equals(dserie, that.dserie) && Objects.equals(fserie, that.fserie) && Objects.equals(typeCheque, that.typeCheque) && Objects.equals(datePers, that.datePers) && Objects.equals(dateSuspension, that.dateSuspension) && Objects.equals(dateLeveSuspension, that.dateLeveSuspension) && Objects.equals(suspension, that.suspension);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cptejumellecc, denomination, nbreCheq, checkKey, dserie, fserie, typeCheque, datePers, dateSuspension, dateLeveSuspension, suspension, tableId);
    }
}
