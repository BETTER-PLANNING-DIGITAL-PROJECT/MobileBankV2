package ibnk.models.banking;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
public class MobileBeneficiairy {
    @Id
    @Column(name = "id", nullable = false, precision = 18)
    private BigDecimal id;

    @Size(max = 50)
    @Nationalized
    @Column(name = "agence", length = 50)
    private String agence;

    @Column(name = "uuid", unique = true, nullable = false)
    private String uuid;

    @Nationalized
    @Column(name = "status", length = 50)
    private String status;

    @Size(max = 50)
    @Nationalized
    @Column(name = "client", length = 50)
    private String client;

    @Size(max = 20)
    @Nationalized
    @Column(name = "donneur", length = 20)
    private String subscriberAccount;

    @Size(max = 50)
    @Nationalized
    @Column(name = "beneficiaire", length = 50)
    private String beneficiary;

    @Size(max = 50)
    @Nationalized
    @Column(name = "telephone", length = 50)
    private String telephone;

    @Size(max = 10)
    @Nationalized
    @Column(name = "cni", length = 10)
    private String cni;

    @Size(max = 50)
    @Nationalized
    @Column(name = "nom", length = 50)
    private String nom;

    @Size(max = 3)
    @Nationalized
    @Column(name = "onInstitution", length = 3)
    private String onInstitution;

}