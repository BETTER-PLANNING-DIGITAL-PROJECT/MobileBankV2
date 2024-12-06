package ibnk.models.banking;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Nationalized;


@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Product {
    @Id
    @Column(name = "ProductCode", nullable = false)
    private Integer id;

    @Size(max = 60)
    @NotNull
    @Nationalized
    @Column(name = "LibProduct", nullable = false, length = 60)
    private String productName;

    @Size(max = 50)
    @Nationalized
    @Column(name = "ProductType", length = 50)
    private String productType;


    @Size(max = 3)
    @Column(name = "mobileDeposit", length = 3)
    private String authorizeDeposit;

    @Size(max = 3)
    @Column(name = "mobileWithdrawal", length = 3)
    private String authorizeWithdraw;

    @Size(max = 3)
    @Nationalized
    @Column(name = "AbonnementEbanking", length = 3)
    private String authorizeEbanking;
}