package ibnk.repositories.banking;

import ibnk.models.banking.CheckBookRequestHistEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckBookRequestHistEntityRepository extends JpaRepository<CheckBookRequestHistEntity,Long> {

  long  countByCpteJumelleAndClientAndStatut(String accoountId,String clientId,String status);
}
