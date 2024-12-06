package ibnk.repositories.internet;

import ibnk.models.internet.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MediaRepository extends JpaRepository<Media,Long> {
    Optional<Media> findByUuid(String uuid);
//    Optional<Media> findByOwnerAndUuid(Subscriptions user, String uuid);
}
