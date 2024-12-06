package ibnk.repositories.internet;

import ibnk.models.internet.TermAndCondition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TermAndConditionRepository extends JpaRepository<TermAndCondition,Long> {
    Optional<TermAndCondition> findByCode(String code);
}
