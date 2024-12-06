package ibnk.webController;

import ibnk.dto.BankingDto.AccountEntityDto;
import ibnk.service.BankingService.AccountService;
import ibnk.tools.ResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author PHILF
 */
@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/api/v1/admin/accounts")
public class AccountsController {
    private final AccountService accountService;

    @PostMapping("find/{accountId}")
    public ResponseEntity<Object> findClientAccount(@PathVariable String accountId) {
        List<AccountEntityDto> cus = accountService.findClientAccounts(accountId);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", cus);
    }

}
