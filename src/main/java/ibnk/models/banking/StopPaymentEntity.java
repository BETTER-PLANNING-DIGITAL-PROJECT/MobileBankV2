package ibnk.models.banking;

import jakarta.persistence.*;

import java.sql.Date;
import java.util.Objects;

@Entity
@Table(name = "StopPayment", schema = "dbo", catalog = "Banking")
public class StopPaymentEntity {
    @Basic
    @Column(name = "CpteJumelle", nullable = false, length = 20)
    private String cpteJumelle;
    @Basic
    @Column(name = "TypeValeur", nullable = true, length = 12)
    private String typeValeur;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "NumChequier", nullable = false, length = 15)
    private String numChequier;
    @Basic
    @Column(name = "Montant", nullable = true, precision = 0)
    private Double montant;
    @Basic
    @Column(name = "Beneficiaire", nullable = true, length = 50)
    private String beneficiaire;
    @Basic
    @Column(name = "DateEmission", nullable = true)
    private Date dateEmission;
    @Basic
    @Column(name = "Description", nullable = true, length = 100)
    private String description;
    @Basic
    @Column(name = "Denomination", nullable = true, length = 100)
    private String denomination;
    @Basic
    @Column(name = "Agence", nullable = true, length = 5)
    private String agence;
    @Basic
    @Column(name = "LibAgence", nullable = true, length = 80)
    private String libAgence;
    @Basic
    @Column(name = "DateOpposition", nullable = true)
    private Date dateOpposition;
    @Basic
    @Column(name = "Employe", nullable = true, length = 5)
    private String employe;
    @Basic
    @Column(name = "LibEmploye", nullable = true, length = 50)
    private String libEmploye;
    @Basic
    @Column(name = "ValSaisie", nullable = true, length = 5)
    private String valSaisie;
    @Basic
    @Column(name = "DateLeveeOpposition", nullable = true)
    private Date dateLeveeOpposition;
    @Basic
    @Column(name = "Datetransfert", nullable = true)
    private Date datetransfert;
    @Basic
    @Column(name = "Reference", nullable = true, length = 30)
    private String reference;
    @Basic
    @Column(name = "rowguid", nullable = false)
    private Object rowguid;

    public String getCpteJumelle() {
        return cpteJumelle;
    }

    public void setCpteJumelle(String cpteJumelle) {
        this.cpteJumelle = cpteJumelle;
    }

    public String getTypeValeur() {
        return typeValeur;
    }

    public void setTypeValeur(String typeValeur) {
        this.typeValeur = typeValeur;
    }

    public String getNumChequier() {
        return numChequier;
    }

    public void setNumChequier(String numChequier) {
        this.numChequier = numChequier;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public String getBeneficiaire() {
        return beneficiaire;
    }

    public void setBeneficiaire(String beneficiaire) {
        this.beneficiaire = beneficiaire;
    }

    public Date getDateEmission() {
        return dateEmission;
    }

    public void setDateEmission(Date dateEmission) {
        this.dateEmission = dateEmission;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDenomination() {
        return denomination;
    }

    public void setDenomination(String denomination) {
        this.denomination = denomination;
    }

    public String getAgence() {
        return agence;
    }

    public void setAgence(String agence) {
        this.agence = agence;
    }

    public String getLibAgence() {
        return libAgence;
    }

    public void setLibAgence(String libAgence) {
        this.libAgence = libAgence;
    }

    public Date getDateOpposition() {
        return dateOpposition;
    }

    public void setDateOpposition(Date dateOpposition) {
        this.dateOpposition = dateOpposition;
    }

    public String getEmploye() {
        return employe;
    }

    public void setEmploye(String employe) {
        this.employe = employe;
    }

    public String getLibEmploye() {
        return libEmploye;
    }

    public void setLibEmploye(String libEmploye) {
        this.libEmploye = libEmploye;
    }

    public String getValSaisie() {
        return valSaisie;
    }

    public void setValSaisie(String valSaisie) {
        this.valSaisie = valSaisie;
    }

    public Date getDateLeveeOpposition() {
        return dateLeveeOpposition;
    }

    public void setDateLeveeOpposition(Date dateLeveeOpposition) {
        this.dateLeveeOpposition = dateLeveeOpposition;
    }

    public Date getDatetransfert() {
        return datetransfert;
    }

    public void setDatetransfert(Date datetransfert) {
        this.datetransfert = datetransfert;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Object getRowguid() {
        return rowguid;
    }

    public void setRowguid(Object rowguid) {
        this.rowguid = rowguid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StopPaymentEntity that = (StopPaymentEntity) o;
        return Objects.equals(cpteJumelle, that.cpteJumelle) && Objects.equals(typeValeur, that.typeValeur) && Objects.equals(numChequier, that.numChequier) && Objects.equals(montant, that.montant) && Objects.equals(beneficiaire, that.beneficiaire) && Objects.equals(dateEmission, that.dateEmission) && Objects.equals(description, that.description) && Objects.equals(denomination, that.denomination) && Objects.equals(agence, that.agence) && Objects.equals(libAgence, that.libAgence) && Objects.equals(dateOpposition, that.dateOpposition) && Objects.equals(employe, that.employe) && Objects.equals(libEmploye, that.libEmploye) && Objects.equals(valSaisie, that.valSaisie) && Objects.equals(dateLeveeOpposition, that.dateLeveeOpposition) && Objects.equals(datetransfert, that.datetransfert) && Objects.equals(reference, that.reference) && Objects.equals(rowguid, that.rowguid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cpteJumelle, typeValeur, numChequier, montant, beneficiaire, dateEmission, description, denomination, agence, libAgence, dateOpposition, employe, libEmploye, valSaisie, dateLeveeOpposition, datetransfert, reference, rowguid);
    }
}
