package ibnk.webController;

import ibnk.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin/question")
public class SecurityQuestionsController {
    private final QuestionService questionService;
}
