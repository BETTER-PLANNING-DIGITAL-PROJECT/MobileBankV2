package ibnk.repositories.banking;
import ibnk.models.banking.MobileBankConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MbConfigRepository extends JpaRepository<MobileBankConfiguration, String> {

}
