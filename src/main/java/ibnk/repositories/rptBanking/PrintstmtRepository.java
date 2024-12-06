package ibnk.repositories.rptBanking;

import ibnk.models.rptBanking.PrintStmtEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;


public interface PrintstmtRepository extends JpaRepository<PrintStmtEntity, Long> {

    @Modifying
    @Transactional
    void deleteAllByEmployeeId(String uuid);
}
