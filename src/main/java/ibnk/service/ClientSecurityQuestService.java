package ibnk.service;

import ibnk.dto.BankingDto.ClientQuestDto;
import ibnk.models.internet.InstitutionConfig;
import ibnk.models.internet.client.ClientSecurityQuestion;
import ibnk.models.internet.client.SecurityQuestions;
import ibnk.models.internet.client.Subscriptions;
import ibnk.repositories.internet.ClientSecurityQuestionRepository;
import ibnk.repositories.internet.SecurityQuestionRepository;
import ibnk.tools.error.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ClientSecurityQuestService {
    private final ClientSecurityQuestionRepository clientSecurityQuestionRepository;
    private final InstitutionConfigService institutionConfigService;
    public final PasswordEncoder passwordEncoder;
    private final SecurityQuestionRepository securityQuestionRepository;
    private final QuestionService questionService;


    @Transactional
    public String saveClientQuestion(List<ClientQuestDto> dto, Subscriptions subscription) throws ResourceNotFoundException {
        InstitutionConfig config = institutionConfigService.getInstConfig();
        List<ClientSecurityQuestion> clientSecurityQuestions = new ArrayList<>();
        if (dto.size() <= config.getMaxSecurityQuest() || dto.size() >= config.getMinSecurityQuest()) {
            dto.forEach(clientQuestDto -> {
                Optional<SecurityQuestions> question = securityQuestionRepository.findById(clientQuestDto.getSecurityQuestionId());
                if (question.isEmpty()) throw new RuntimeException("");
                ClientSecurityQuestion securityQuestion = new ClientSecurityQuestion();
                securityQuestion.setSubscriptions(subscription);
                securityQuestion.setSecurityAns(passwordEncoder.encode(clientQuestDto.getSecurityAns().toLowerCase()));
                securityQuestion.setSecurityQuestions(question.get());
                clientSecurityQuestions.add(securityQuestion);
            });
            updateClientQuestion(clientSecurityQuestions, subscription);
        } else {
            throw new ResourceNotFoundException("max or min Questions not respected");
        }
        return "Success";
    }

    @Transactional
    public String updateClientQuestion(List<ClientSecurityQuestion> clientSecurityQuestions, Subscriptions subscription) {
        clientSecurityQuestionRepository.deleteClientSecurityQuestionsBySubscriptions(subscription);
        clientSecurityQuestionRepository.saveAll(clientSecurityQuestions);
        return "Updated";
    }

    private String deleteClientQuestion(List<ClientSecurityQuestion> clientSecurityQuestions) {
        clientSecurityQuestionRepository.deleteAll(clientSecurityQuestions);
        return "Deleted";
    }

}
