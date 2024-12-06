package ibnk.repositories.banking;

import ibnk.models.banking.MobilePayment;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface MobilePaymentRepository extends JpaRepository<MobilePayment, BigDecimal> {
    //    List<MobilePayment> findByStatusAndApplication(String status, String app);
    @Query("SELECT m FROM MobilePayment m WHERE (:from IS NULL OR m.date >= :from) AND (:to IS NULL OR m.date <= :to)")
    Page<MobilePayment> findAllWithinDateRange(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

}
