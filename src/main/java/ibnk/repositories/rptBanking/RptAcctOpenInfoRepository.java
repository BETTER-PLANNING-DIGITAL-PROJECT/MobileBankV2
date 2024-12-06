package ibnk.repositories.rptBanking;

import ibnk.models.rptBanking.RptAcctOpenInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RptAcctOpenInfoRepository extends JpaRepository<RptAcctOpenInfoEntity,Long> {
    void deleteAllByCpteJumelle(String account);

}
