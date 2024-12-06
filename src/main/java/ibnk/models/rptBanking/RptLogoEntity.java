package ibnk.models.rptBanking;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Arrays;
import java.util.Objects;

@Entity
@Data
@Table(name = "rptLogo", schema = "dbo", catalog = "rptBanking")
public class RptLogoEntity {
    @Basic
    @Column(name = "Logo", nullable = true)
    private byte[] logo;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false, length = 50)
    private String id;



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RptLogoEntity that = (RptLogoEntity) o;
        return Arrays.equals(logo, that.logo) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id);
        result = 31 * result + Arrays.hashCode(logo);
        return result;
    }
}
