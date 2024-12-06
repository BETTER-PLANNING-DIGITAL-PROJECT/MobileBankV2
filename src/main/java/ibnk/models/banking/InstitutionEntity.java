package ibnk.models.banking;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Arrays;
import java.util.Objects;

@Entity
@Data
@Table(name = "Institution", schema = "dbo", catalog = "Banking")
public class InstitutionEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "CobacID", nullable = false, length = 10)
    private String cobacId;
    @Basic
    @Column(name = "motto", nullable = true, length = 100)
    private String motto;
    @Basic
    @Column(name = "publicite", nullable = true, length = 100)
    private String publicite;
    @Basic
    @Column(name = "institution", nullable = true, length = 80)
    private String institution;
    @Basic
    @Column(name = "poBox", nullable = true, length = 80)
    private String poBox;
    @Basic
    @Column(name = "town", nullable = true, length = 80)
    private String town;
    @Basic
    @Column(name = "telep", nullable = true, length = 80)
    private String tele;
    @Basic
    @Column(name = "Typesociete", nullable = true, length = 50)
    private String typesociete;
    @Basic
    @Column(name = "CompanyAbbreviation", nullable = true, length = 32)
    private String companyAbbreviation;
    @Basic
    @Column(name = "Sauvegarde", nullable = true, length = 1)
    private String sauvegarde;
    @Basic
    @Column(name = "PathLogo", nullable = true, length = 255)
    private String pathLogo;
    @Basic
    @Column(name = "SizeLogo", nullable = true)
    private Integer sizeLogo;
    @Basic
    @Column(name = "Logo", nullable = true)
    private byte[] logo;
    @Basic
    @Column(name = "PhotoName", nullable = true, length = 50)
    private String photoName;
    @Basic
    @Column(name = "rowguid", nullable = false)
    private Object rowguid;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstitutionEntity that = (InstitutionEntity) o;
        return Objects.equals(cobacId, that.cobacId) && Objects.equals(motto, that.motto) && Objects.equals(publicite, that.publicite) && Objects.equals(institution, that.institution) && Objects.equals(typesociete, that.typesociete) && Objects.equals(companyAbbreviation, that.companyAbbreviation) && Objects.equals(sauvegarde, that.sauvegarde) && Objects.equals(pathLogo, that.pathLogo) && Objects.equals(sizeLogo, that.sizeLogo) && Arrays.equals(logo, that.logo) && Objects.equals(photoName, that.photoName) && Objects.equals(rowguid, that.rowguid);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(cobacId, motto, publicite, institution, typesociete, companyAbbreviation, sauvegarde, pathLogo, sizeLogo, photoName, rowguid);
        result = 31 * result + Arrays.hashCode(logo);
        return result;
    }
}
