package ibnk.models.banking;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@Entity(name = "Employe")
public class Employee {
    @EmbeddedId
    private EmployeId id;

    @Size(max = 45)
    @Nationalized
    @Column(name = "NomPrenom", length = 45)
    private String fullName;

    @NotNull
    @Column(name = "Suspension", nullable = false)
    private Boolean suspended = false;

    @Size(max = 5)
    @Nationalized
    @Column(name = "Employe", length = 5)
    private String employee;

    @Size(max = 50)
    @Nationalized
    @Column(name = "NomEmploye", length = 50)
    private String employeeName;

    @Size(max = 3)
    @Nationalized
    @Column(name = "Status", length = 3)
    private String status;

}