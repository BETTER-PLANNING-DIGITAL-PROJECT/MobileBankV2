package ibnk.webController.client;

import ibnk.service.ClientSecurityQuestService;
import ibnk.service.QuestionService;
import lombok.RequiredArgsConstructor;
import ibnk.service.CustomerService;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/client/subscribedClient")
public class ClientHelloController {
    private final CustomerService customerService;
    private final ClientSecurityQuestService clientSecurityQuestService;
    private final QuestionService questionService;

}

