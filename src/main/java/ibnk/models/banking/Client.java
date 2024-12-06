package ibnk.models.banking;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Nationalized;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;

@Getter
@Setter
@Entity(name = "ClientBnk")
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Client implements UserDetails {
    @Id
    @Size(max = 6)
    @Nationalized
    @Column(name = "client", nullable = false, length = 6)
    private String clientId;

    @Size(max = 5)
    @NotNull
    @Nationalized
    @Column(name = "Agence", nullable = false, length = 5)
    private String branch;

    @Size(max = 1000)
    @Nationalized
    @Column(name = "Adresse1", length = 1000)
    private String address1;

    @Size(max = 1000)
    @Nationalized
    @Column(name = "Adresse2", length = 1000)
    private String address2;

    @Size(max = 10)
    @Nationalized
    @Column(name = "TypeClient", length = 10)
    private String clientType;

    @Size(max = 80)
    @Nationalized
    @Column(name = "LibAgence", length = 80)
    private String branchName;


    @Size(max = 100)
    @Column(name = "LieuNaissance", length = 100)
    private String placeOfBirth;

    @Size(max = 30)
    @Nationalized
    @Column(name = "Telephone1", length = 30)
    private String phoneNumber;

    @Size(max = 30)
    @Nationalized
    @Column(name = "Telephone2", length = 30)
    private String phoneNumber2;

    @Size(max = 30)
    @Nationalized
    @Column(name = "Telephone3", length = 30)
    private String phoneNumber3;

    @Size(max = 100)
    @Nationalized
    @Column(name = "RaisonSociale", length = 100)
    private String raisonSociale;

    @Size(max = 50)
    @Column(name = "SiegeSocial", length = 50)
    private String siegeSocial;

    @Size(max = 50)
    @Column(name = "RegistreCce", length = 50)
    private String registreCce;

    @Size(max = 60)
    @Nationalized
    @Column(name = "NomAbrege", length = 60)
    private String nomAbrege;

    @Size(max = 30)
    @Nationalized
    @Column(name = "NumContrib", length = 30)
    private String numContrib;

    @Column(name = "DateCreatSoc")
    private LocalDateTime dateCreatSoc;

    @Size(max = 60)
    @Nationalized
    @Column(name = "ObjetSocial", length = 60)
    private String objetSocial;

    @Size(max = 50)
    @Nationalized
    @Column(name = "LibNatBeac", length = 50)
    private String libNatBeac;

    @Size(max = 50)
    @Nationalized
    @Column(name = "LibAgentEco", length = 50)
    private String libAgentEco;

    @Size(max = 50)
    @Nationalized
    @Column(name = "LibActiviteEco", length = 50)
    private String libActiviteEco;

    @Size(max = 50)
    @Nationalized
    @Column(name = "LibNatJuridique", length = 50)
    private String libNatJuridique;



    @Size(max = 50)
    @Nationalized
    @Column(name = "LibTitre", length = 50)
    private String libTitre;

    @Size(max = 50)
    @Nationalized
    @Column(name = "LibNationalite", length = 50)
    private String libNationalite;

    @Size(max = 50)
    @Nationalized
    @Column(name = "LibSiegeSocial", length = 50)
    private String libSiegeSocial;

    @Column(name = "DateCreation")
    private LocalDateTime dateCreation;

    @Size(max = 100)
    @Column(name = "NomJumelle", length = 100)
    private String fullName;

    @Size(max = 30)
    @Nationalized
    @Column(name = "Profession", length = 30)
    private String profession;


    @Size(max = 5)
    @Nationalized
    @Column(name = "Employe", length = 5)
    private String employe;

    @Size(max = 50)
    @Nationalized
    @Column(name = "email", length = 50)
    private String email;

    @Size(max = 10)
    @Nationalized
    @Column(name = "EbnkSub", length = 10)
    private String eBankSub;

    @Size(max = 30)
    @Nationalized
    @Column(name = "CNIPass", length = 30)
    private String identification;

    @Size(max = 10)
    @Nationalized
    @Column(name = "TypeCNIPass", length = 10)
    private String IdentificationType;

    @Column(name = "DteIssueCNI")
    private LocalDateTime IdentificationIssueDate;


    @Column(name = "DteExpCNI")
    private LocalDateTime identificationExpireDate;

    @Size(max = 100)
    @Column(name = "PlaceIssueCNI", length = 100)
    private String placeIssueIdentification;

    @Size(max = 1000)
    @Nationalized
    @Column(name = "AdresseContact", length = 1000)
    private String pContactAddress;

    @Size(max = 25)
    @Nationalized
    @Column(name = "TelContact", length = 25)
    private String pContactPhoneNumber;


    @Column(name = "EDateSubscription")
    private LocalDateTime eSubscriptionDate;


    @Size(max = 50)
    @Nationalized
    @Column(name = "MobileType", length = 50)
    private String eBankPackage;

    @Size(max = 50)
    @Nationalized
    @Column(name = "passwordmb", length = 50)
    private String password;

    @Size(max = 50)
    @Nationalized
    @Column(name = "passwordOperation", length = 50)
    private String passwordOperation;



    @Size(max = 11)
    @Nationalized
    @Column(name = "Eaccount", length = 11)
    private String eBankPrimaryAccount;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getUsername() {
        return this.getPhoneNumber();
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}