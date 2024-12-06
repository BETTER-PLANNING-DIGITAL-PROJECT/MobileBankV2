package ibnk.repositories.banking;

import ibnk.models.banking.ChequierEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChequierEntityRepository extends JpaRepository<ChequierEntity,Long> {
}
