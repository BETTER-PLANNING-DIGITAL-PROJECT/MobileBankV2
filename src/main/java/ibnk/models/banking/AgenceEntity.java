package ibnk.models.banking;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;
import java.util.Objects;

@Entity
@Data
@Table(name = "agence", schema = "dbo", catalog = "Banking")
public class AgenceEntity {
    @Id
    @Column(name = "agcod", nullable = false, length = 5)
    private String agcod;
    @Basic
    @Column(name = "aglib", nullable = true, length = 100)
    private String aglib;
    @Basic
    @Column(name = "agadr", nullable = true, length = 100)
    private String agadr;
    @Basic
    @Column(name = "agbp", nullable = true, length = 6)
    private String agbp;
    @Basic
    @Column(name = "agville", nullable = true, length = 50)
    private String agville;
    @Basic
    @Column(name = "agpays", nullable = true, length = 30)
    private String agpays;
    @Basic
    @Column(name = "agtelp", nullable = true, length = 20)
    private String agtelp;
    @Basic
    @Column(name = "agfax", nullable = true, length = 20)
    private String agfax;
    @Basic
    @Column(name = "agdcr", nullable = true)
    private Date agdcr;
    @Basic
    @Column(name = "agdats", nullable = true)
    private Date agdats;
    @Basic
    @Column(name = "CobacID", nullable = true, length = 10)
    private String cobacId;
    @Basic
    @Column(name = "BeacId", nullable = true, length = 10)
    private String beacId;
    @Basic
    @Column(name = "cptecltres", nullable = true, length = 20)
    private String cptecltres;
    @Basic
    @Column(name = "Employe", nullable = true, length = 5)
    private String employe;
    @Basic
    @Column(name = "BranchSituation", nullable = true, length = 2)
    private String branchSituation;
    @Basic
    @Column(name = "DateDerFerm", nullable = true)
    private Date dateDerFerm;
    @Basic
    @Column(name = "DateJourEnCours", nullable = true)
    private Date dateJourEnCours;
    @Basic
    @Column(name = "Sauvegarde", nullable = true, length = 1)
    private String sauvegarde;
    @Basic
    @Column(name = "ControlBranchAcct", nullable = true, length = 1)
    private String controlBranchAcct;
    @Basic
    @Column(name = "cptecltresint", nullable = true, length = 50)
    private String cptecltresint;
    @Basic
    @Column(name = "TransfPhone", nullable = true, length = 20)
    private String transfPhone;
    @Basic
    @Column(name = "ServerDate", nullable = true)
    private Date serverDate;
    @Basic
    @Column(name = "ServerOpen", nullable = true, length = 3)
    private String serverOpen;
    @Basic
    @Column(name = "ServerBackDate", nullable = true)
    private Date serverBackDate;
    @Basic
    @Column(name = "BackOpen", nullable = true, length = 3)
    private String backOpen;
    @Basic
    @Column(name = "Ipserver", nullable = true, length = 50)
    private String ipserver;
    @Basic
    @Column(name = "Mdpserver", nullable = true, length = 50)
    private String mdpserver;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgenceEntity that = (AgenceEntity) o;
        return Objects.equals(agcod, that.agcod) && Objects.equals(aglib, that.aglib) && Objects.equals(agadr, that.agadr) && Objects.equals(agbp, that.agbp) && Objects.equals(agville, that.agville) && Objects.equals(agpays, that.agpays) && Objects.equals(agtelp, that.agtelp) && Objects.equals(agfax, that.agfax) && Objects.equals(agdcr, that.agdcr) && Objects.equals(agdats, that.agdats) && Objects.equals(cobacId, that.cobacId) && Objects.equals(beacId, that.beacId) && Objects.equals(cptecltres, that.cptecltres) && Objects.equals(employe, that.employe) && Objects.equals(branchSituation, that.branchSituation) && Objects.equals(dateDerFerm, that.dateDerFerm) && Objects.equals(dateJourEnCours, that.dateJourEnCours) && Objects.equals(sauvegarde, that.sauvegarde) && Objects.equals(controlBranchAcct, that.controlBranchAcct) && Objects.equals(cptecltresint, that.cptecltresint) && Objects.equals(transfPhone, that.transfPhone) && Objects.equals(serverDate, that.serverDate) && Objects.equals(serverOpen, that.serverOpen) && Objects.equals(serverBackDate, that.serverBackDate) && Objects.equals(backOpen, that.backOpen) && Objects.equals(ipserver, that.ipserver) && Objects.equals(mdpserver, that.mdpserver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agcod, aglib, agadr, agbp, agville, agpays, agtelp, agfax, agdcr, agdats, cobacId, beacId, cptecltres, employe, branchSituation, dateDerFerm, dateJourEnCours, sauvegarde, controlBranchAcct, cptecltresint, transfPhone, serverDate, serverOpen, serverBackDate, backOpen, ipserver, mdpserver);
    }
}
