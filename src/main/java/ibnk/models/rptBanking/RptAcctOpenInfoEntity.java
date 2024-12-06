package ibnk.models.rptBanking;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.Nationalized;

import java.sql.Date;
import java.util.Arrays;
import java.util.Objects;
@Entity
@Table(name = "rptAcctOpenInfo", schema = "dbo", catalog = "rptBanking")
@Data
public class RptAcctOpenInfoEntity {
    @Basic
    @Column(name = "Agence", nullable = true, length = 255)
    private String agence;


    @Basic
    @Id
    @Size(max = 20)
    @Nationalized
    @Column(name = "Client", nullable = true, length = 255)
    private String client;


    @Basic
    @Column(name = "CpteJumelle", nullable = true, length = 255)
    private String cpteJumelle;


    @Basic
    @Column(name = "LibClient", nullable = true, length = 255)
    private String libClient;



    @Basic
    @Column(name = "DateOuverture", nullable = true)
    private String dateOuverture;



    @Basic
    @Column(name = "address2", nullable = true, length = 2147483647)
    private String address2;



    @Basic
    @Column(name = "address1", nullable = true, length = 2147483647)
    private String address1;


    @Basic
    @Column(name = "telephone1", nullable = true, length = 255)
    private String telephone1;



    @Basic
    @Column(name = "LibAgence", nullable = true, length = 255)
    private String libAgence;



    @Basic
    @Column(name = "LibGestionnaire", nullable = true, length = 255)
    private String libGestionnaire;



    @Basic
    @Column(name = "TypeClient", nullable = true, length = 255)
    private String typeClient;


    @Basic
    @Column(name = "CatClient", nullable = true, length = 255)
    private String catClient;



    @Basic
    @Column(name = "DateNaissance", nullable = true)
    private String dateNaissance;


    @Basic
    @Column(name = "LieuNaissance", nullable = true, length = 255)
    private String lieuNaissance;


    @Basic
    @Column(name = "CNIPass", nullable = true, length = 255)
    private String cniPass;



    @Basic
    @Column(name = "Sigle", nullable = true, length = 255)
    private String sigle;



    @Basic
    @Column(name = "RegistreCce", nullable = true, length = 255)
    private String registreCce;



    @Basic
    @Column(name = "NumContrib", nullable = true, length = 255)
    private String numContrib;



    @Basic
    @Column(name = "DateCreatSoc", nullable = true)
    private String dateCreatSoc;



    @Basic
    @Column(name = "LibAgentEco", nullable = true, length = 255)
    private String libAgentEco;


    @Basic
    @Column(name = "LibActiviteEco", nullable = true, length = 255)
    private String libActiviteEco;


    @Basic
    @Column(name = "LibNatJuridique", nullable = true, length = 255)
    private String libNatJuridique;



    @Basic
    @Column(name = "LibNationalite", nullable = true, length = 255)
    private String libNationalite;



    @Basic
    @Column(name = "LibSiegeSocial", nullable = true, length = 255)
    private String libSiegeSocial;



    @Basic
    @Column(name = "bp", nullable = true, length = 255)
    private String bp;



    @Basic
    @Column(name = "libville", nullable = true, length = 255)
    private String libville;


    @Basic
    @Column(name = "DateCreation", nullable = true)
    private String dateCreation;


    @Basic
    @Column(name = "Profession", nullable = true, length = 255)
    private String profession;



    @Basic
    @Column(name = "Balance", nullable = true)
    private Integer balance;


    @Basic
    @Column(name = "Logo", nullable = true)
    private byte[] logo;



    @Basic
    @Column(name = "Photo1", nullable = true)
    private byte[] photo1;



    @Basic
    @Column(name = "Photo2", nullable = true)
    private byte[] photo2;


    @Basic
    @Column(name = "Photo3", nullable = true)
    private byte[] photo3;


    @Basic
    @Column(name = "Signature1", nullable = true)
    private byte[] signature1;



    @Basic
    @Column(name = "Signature2", nullable = true)
    private byte[] signature2;


    @Basic
    @Column(name = "Signature3", nullable = true)
    private byte[] signature3;



    @Basic
    @Column(name = "Matricule", nullable = true, length = 255)
    private String matricule;




    @Basic
    @Column(name = "sexe", nullable = true, length = 255)
    private String sexe;


    @Basic
    @Column(name = "NomPere", nullable = true, length = 255)
    private String nomPere;



    @Basic
    @Column(name = "NomMere", nullable = true, length = 255)
    private String nomMere;



    @Basic
    @Column(name = "NomContact", nullable = true, length = 255)
    private String nomContact;

    public String getNomContact() {
        return nomContact;
    }

    public void setNomContact(String nomContact) {
        this.nomContact = nomContact;
    }

    @Basic
    @Column(name = "AdresseContact", nullable = true, length = 255)
    private String adresseContact;

    public String getAdresseContact() {
        return adresseContact;
    }

    public void setAdresseContact(String adresseContact) {
        this.adresseContact = adresseContact;
    }

    @Basic
    @Column(name = "TelContact", nullable = true, length = 255)
    private String telContact;

    public String getTelContact() {
        return telContact;
    }

    public void setTelContact(String telContact) {
        this.telContact = telContact;
    }

    @Basic
    @Column(name = "telephone2", nullable = true, length = 255)
    private String telephone2;

    public String getTelephone2() {
        return telephone2;
    }

    public void setTelephone2(String telephone2) {
        this.telephone2 = telephone2;
    }

    @Basic
    @Column(name = "DteExpCni", nullable = true, length = 255)
    private String dteExpCni;

    public String getDteExpCni() {
        return dteExpCni;
    }

    public void setDteExpCni(String dteExpCni) {
        this.dteExpCni = dteExpCni;
    }

    @Basic
    @Column(name = "DteDelCni", nullable = true, length = 255)
    private String dteDelCni;

    public String getDteDelCni() {
        return dteDelCni;
    }

    public void setDteDelCni(String dteDelCni) {
        this.dteDelCni = dteDelCni;
    }

    @Basic
    @Column(name = "LieuCni", nullable = true, length = 255)
    private String lieuCni;

    public String getLieuCni() {
        return lieuCni;
    }

    public void setLieuCni(String lieuCni) {
        this.lieuCni = lieuCni;
    }
}
