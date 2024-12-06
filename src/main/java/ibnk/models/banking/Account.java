package ibnk.models.banking;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Nationalized;


/**
 * @author PHILFONTAH
 */
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Entity(name = "CpteClt")
public class Account {
    @Id
    @Size(max = 20)
    @Nationalized
    @Column(name = "CpteJumelle", nullable = false, length = 20)
    private String accountNumber;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "client", nullable = false)
    private Client client;

    @Size(max = 100)
    @Nationalized
    @Column(name = "LibClient", length = 100)
    private String accountHolder;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "Devise")
    private Currency currency;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CatCpte")
    private Product accountProduct;

    @Size(max = 100)
    @Nationalized
    @Column(name = "LibCatCpte", length = 100)
    private String productName;


    @Size(max = 5)
    @Nationalized
    @Column(name = "Gestionnaire", length = 5)
    private String manager;

    @Size(max = 10)
    @Nationalized
    @Column(name = "Statut", length = 10)
    private String status;


    @Size(max = 80)
    @Nationalized
    @Column(name = "LibAgence", length = 80)
    private String branchName;

    @Size(max = 80)
    @Nationalized
    @Column(name = "Agence", length = 80)
    private String branchCode;

    @Size(max = 100)
    @Nationalized
    @Column(name = "LibGestionnaire", length = 100)
    private String managerName;


}