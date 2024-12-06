package ibnk.repositories.internet;

import ibnk.models.internet.client.SecurityQuestions;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityQuestionRepository extends JpaRepository<SecurityQuestions,Long> {


}
