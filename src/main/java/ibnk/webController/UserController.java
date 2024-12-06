package ibnk.webController;

import com.fasterxml.jackson.annotation.JsonView;
import ibnk.dto.UserDto;
import ibnk.dto.auth.UpdatePasswordDto;
import ibnk.models.internet.UserEntity;
import ibnk.repositories.internet.UserRepository;
import ibnk.service.UserService;
import ibnk.tools.ResponseHandler;
import ibnk.tools.Views;
import ibnk.tools.error.ResourceNotFoundException;
import ibnk.tools.error.UnauthorizedUserException;
import ibnk.tools.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/api/v1/admin/user")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping("create")
    public ResponseEntity<Object> RegisterUser(@RequestBody UserDto.CreateUserDto dto) throws UnauthorizedUserException, ResourceNotFoundException, SQLException {
        String cus = userService.createUser(dto);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", cus);
    }

    @PutMapping("update/{uuid}")
    public ResponseEntity<Object> UpdateUser(@PathVariable(value = "uuid") String uuid,@RequestBody UserDto.CreateUserDto dto   ) throws  ValidationException {
        String cus = userService.updateUser(uuid,dto);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", cus);
    }

    @JsonView(Views.UserView.class)
    @GetMapping("listAll")
    public ResponseEntity<Object> listAllUsers() {
        List<UserEntity> cus = userRepository.findAll().stream().filter(userEntity -> userEntity.getId() != 1).toList();
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", cus);
    }

    @PostMapping("update/password")
    public ResponseEntity<Object> UpdateUserPassword(@RequestBody UpdatePasswordDto update, @AuthenticationPrincipal UserEntity user) throws UnauthorizedUserException, ValidationException {
        UserEntity cus = userService.UpdatePassword(user, update);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", cus);
    }

}
