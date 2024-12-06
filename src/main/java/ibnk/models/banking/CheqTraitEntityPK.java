package ibnk.models.banking;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.io.Serializable;
import java.util.Objects;

public class CheqTraitEntityPK implements Serializable {
    @Column(name = "Tracer", nullable = false, length = 30)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String tracer;
    @Column(name = "Serie", nullable = false, precision = 0)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int serie;



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CheqTraitEntityPK that = (CheqTraitEntityPK) o;
        return serie == that.serie && Objects.equals(tracer, that.tracer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tracer, serie);
    }
}
