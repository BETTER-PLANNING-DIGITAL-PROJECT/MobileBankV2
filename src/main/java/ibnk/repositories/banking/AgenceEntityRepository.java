package ibnk.repositories.banking;

import ibnk.models.banking.AgenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgenceEntityRepository extends JpaRepository<AgenceEntity,String> {
}
