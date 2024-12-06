package ibnk.models.banking;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Objects;

@Entity
@Table(name = "MobileBeneficiairy", schema = "dbo", catalog = "Banking")
@Data
public class MobileBeneficiairyEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    @JsonIgnore
    private Integer id;
    @Basic
    @Column(name = "agence")
    private String agence;
    @Basic
    @Column(name = "client")
    private String client;
    @Basic
    @Column(name = "donneur")
    private String subscriberAccount;
    @Basic
    @Column(name = "status")
    private String status;
    @Basic
    @Column(name = "uuid",unique = true)
    @JsonIgnore
    private String uuid;
    @Basic
    @Column(name = "beneficiaire")
    private String beneficiary;
    @Basic
    @Column(name = "telephone")
    private String telephone;
    @Basic
    @Column(name = "cni")
    private String cni;
    @Basic
    @Column(name = "nom")
    private String nom;
    @Basic
    @Column(name = "onInstitution")
    private String onInstitution;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MobileBeneficiairyEntity that = (MobileBeneficiairyEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(agence, that.agence) && Objects.equals(client, that.client) && Objects.equals(subscriberAccount, that.subscriberAccount) && Objects.equals(beneficiary, that.beneficiary) && Objects.equals(telephone, that.telephone) && Objects.equals(cni, that.cni) && Objects.equals(nom, that.nom) && Objects.equals(onInstitution, that.onInstitution);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, agence, client, subscriberAccount, beneficiary, telephone, cni, nom, onInstitution);
    }
}
