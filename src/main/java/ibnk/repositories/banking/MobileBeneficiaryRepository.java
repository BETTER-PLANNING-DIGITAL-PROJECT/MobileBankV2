package ibnk.repositories.banking;

import ibnk.models.banking.MobileBeneficiairy;
import ibnk.models.banking.MobileBeneficiairyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.Optional;

public interface MobileBeneficiaryRepository extends JpaRepository<MobileBeneficiairyEntity, Integer> {
 Optional<MobileBeneficiairyEntity> findByUuid(String uuid);


 Optional<MobileBeneficiairyEntity> findByClientAndBeneficiary(String client,String beneficiary);
 Optional<MobileBeneficiairyEntity> findByClientAndTelephone(String client,String phoneNumber);

 Optional<MobileBeneficiairyEntity> findByClientAndBeneficiaryAndTelephone(String client, String beneficiary, String tel);
 Optional<MobileBeneficiairyEntity> findByClientAndBeneficiaryAndTelephoneAndStatus(String client, String beneficiary, String tel,String status);
}
