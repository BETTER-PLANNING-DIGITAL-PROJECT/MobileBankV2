package ibnk.repositories.internet;

import ibnk.models.internet.client.ClientRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRequestRepository extends JpaRepository<ClientRequest,Long> {
    Optional<ClientRequest> findByUuid(String uuid);
    Optional<ClientRequest> findByEmailAndCustomerType(String email,String type);

    Optional<ClientRequest> findByEmail(String email);
   int  countClientRequestByStatus(String status);
}
