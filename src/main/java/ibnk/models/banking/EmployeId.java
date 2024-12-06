package ibnk.models.banking;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Nationalized;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class EmployeId implements Serializable {
    private static final long serialVersionUID = 6534087298284574963L;
    @Size(max = 5)
    @NotNull
    @Nationalized
    @Column(name = "Agence", nullable = false, length = 5)
    private String agence;

    @Size(max = 5)
    @NotNull
    @Nationalized
    @Column(name = "Matricule", nullable = false, length = 5)
    private String matricule;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        EmployeId entity = (EmployeId) o;
        return Objects.equals(this.agence, entity.agence) &&
                Objects.equals(this.matricule, entity.matricule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agence, matricule);
    }

}