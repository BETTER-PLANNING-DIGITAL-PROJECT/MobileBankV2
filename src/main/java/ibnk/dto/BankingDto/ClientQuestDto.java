package ibnk.dto.BankingDto;

import lombok.Data;

@Data
public class ClientQuestDto{
        private Long id;
        private Long securityQuestionId;
        private String securityAns;
}