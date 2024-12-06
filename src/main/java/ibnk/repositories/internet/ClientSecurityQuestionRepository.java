package ibnk.repositories.internet;

import ibnk.models.internet.client.ClientSecurityQuestion;
import ibnk.models.internet.client.SecurityQuestions;
import ibnk.models.internet.client.Subscriptions;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientSecurityQuestionRepository extends JpaRepository<ClientSecurityQuestion,Long> {
    @Query("SELECT r.id,r.question FROM ClientSecurityQuestion u JOIN SecurityQuestions r ON u.securityQuestions.id = r.id WHERE u.id = :clientId")
    List<SecurityQuestions> listClientSecurityQuestions(@Param("clientId") Long clientId);

    @Query("SELECT u.securityQuestions FROM ClientSecurityQuestion u  WHERE u.subscriptions.id = :clientId ORDER BY NEWID()")
    List<SecurityQuestions> listClientSecurityQuestionsRandomly(@Param("clientId") Long clientId);


    Integer countBySecurityQuestions(SecurityQuestions securityQuestions);

    Integer countBySubscriptions(Subscriptions subscriptions);


   ClientSecurityQuestion findClientSecurityQuestionBySubscriptionsAndId(Subscriptions subs, Long Id);

    @Query("SELECT u FROM ClientSecurityQuestion u  WHERE u.subscriptions.id = :clientId AND u.securityQuestions.id = :questionId")
   Optional<ClientSecurityQuestion> findByClientIdAndQuestionId(@Param("clientId") Long clientId, @Param("questionId") Long questionId);

   @Transactional
   @Modifying
    void deleteClientSecurityQuestionsBySubscriptions(Subscriptions subscription);
//   Optional<ClientSecurityQuestion> findClientSecurityQuestionBySubscriptionsAndId(Subscriptions subs, Long Id);

}
