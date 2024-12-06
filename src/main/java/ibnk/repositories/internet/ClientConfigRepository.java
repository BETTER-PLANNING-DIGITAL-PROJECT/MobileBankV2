package ibnk.repositories.internet;

import ibnk.models.internet.client.ClientConfig;
import ibnk.models.internet.client.Subscriptions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientConfigRepository extends JpaRepository<ClientConfig,Long> {

    List<ClientConfig> findClientConfigBySubscriptions(Subscriptions subscriptions);

    Optional<ClientConfig> findByIdAndSubscriptions(Long id, Subscriptions subscriptions);

}
