package ibnk.repositories.internet;

import ibnk.models.internet.client.Subscriptions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscriptions,Long> {
    Optional<Subscriptions> findByUuid(String uuid);
    Optional<Subscriptions> findByUserLogin(String username);
    int countSubscriptionsByStatus(String status);
    Optional<Subscriptions> findByClientMatricul(String clientMatricul);



}
