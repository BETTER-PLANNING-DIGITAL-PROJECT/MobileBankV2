package ibnk.repositories.banking;

import ibnk.models.banking.InstitutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstitutionRepository extends JpaRepository<InstitutionEntity,String> {
}
