package ibnk.service;

import ibnk.models.internet.client.SecurityQuestions;
import ibnk.repositories.internet.ClientSecurityQuestionRepository;
import ibnk.repositories.internet.InstitutionConfigRepository;
import ibnk.repositories.internet.SecurityQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class QuestionService {
    private final SecurityQuestionRepository securityQuestionRepository;
    private final ClientSecurityQuestionRepository clientSecurityQuestionRepository;

    public String saveSecurityQuestions(SecurityQuestions quests) {
        ////TODO: Verify number of Questios max and min

        securityQuestionRepository.save(quests);
        return "Success";
    }

    public String deleteSecurityQuestion(Long id) {
        securityQuestionRepository.deleteById(id);
        return "Deleted";
    }

    public List<SecurityQuestions> listAllQuestions() {
        return securityQuestionRepository.findAll();
    }
    public List<SecurityQuestions> listQuestionsByClientId(Long ClientId){
        return clientSecurityQuestionRepository.listClientSecurityQuestions(ClientId);
    }


}
