package ibnk.repositories.internet;

import ibnk.models.internet.TransactionStatusMessage;
import ibnk.models.internet.enums.Status;
import ibnk.models.internet.enums.TypeOperations;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionStatusMessageRepository extends JpaRepository<TransactionStatusMessage,Long> {
    Optional<TransactionStatusMessage> findByTypeOp(String type);
    Optional<TransactionStatusMessage> findTransactionStatusMessageByTypeOpAndStatus(TypeOperations type, Status status);

}
