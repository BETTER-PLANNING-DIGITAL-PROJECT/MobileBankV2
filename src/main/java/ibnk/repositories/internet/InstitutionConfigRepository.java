package ibnk.repositories.internet;

import ibnk.models.internet.InstitutionConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstitutionConfigRepository extends JpaRepository<InstitutionConfig,Long> {


    Optional<InstitutionConfig> findByApplication(String app);

}
