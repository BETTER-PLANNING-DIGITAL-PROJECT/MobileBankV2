package ibnk.repositories.internet;
import ibnk.models.internet.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity,Long> {
    Optional<UserEntity> findByUserLogin(String userLogin);
    Optional<UserEntity> findUserByUuid(String uuid);
}
