package ibnk.tools.security;

import ibnk.models.banking.Employee;
import ibnk.models.internet.UserEntity;
import ibnk.repositories.banking.ClientMatriculRepository;
import ibnk.repositories.banking.EmployeeRepository;
import ibnk.repositories.internet.UserRepository;
import ibnk.tools.error.ResourceNotFoundException;
import ibnk.tools.error.UnauthorizedUserException;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


import java.sql.SQLException;
import java.util.Optional;

@Service()
@Transactional

public class SecurityUserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;


    @Autowired
    private EmployeeRepository employeeRepository;
        private final Logger LOGGER = LogManager.getLogger(SecurityUserService.class);

    @Override
    public UserEntity loadUserByUsername(String userLogin) throws UsernameNotFoundException {
        LOGGER.info("Enter >> loadUserByUsername");
        UserEntity user = userRepository.findByUserLogin(userLogin).orElseThrow(() -> new UsernameNotFoundException("failed_login"));
            LOGGER.info("Exit >> loadUserByUsername");

        return user;
    }

    public UserEntity loadUserByUuid(String uuid) throws UnauthorizedUserException, SQLException, ResourceNotFoundException {
            LOGGER.info("Enter >> loadUserByUuid");
        Optional<UserEntity> user = userRepository.findUserByUuid(uuid);
        if (user.isEmpty()) {
            throw new UnauthorizedUserException("Unauthorized");
        }
        UserEntity userDetails = user.get();
        Employee employee = findEmployeeById(userDetails.getUserLogin());
        if (employee.getSuspended()) {
            throw new UnauthorizedUserException("Unauthorized");
        }
            LOGGER.info("Exit >> loadUserByUuid");
        return userDetails;

    }

    public Employee findEmployeeById(String employeeId) throws ResourceNotFoundException {
        Optional<Employee> employee = employeeRepository.findByMatricule(employeeId);
        if(employee.isEmpty()) throw new ResourceNotFoundException("employee_not_found");
        return employee.get();
    }
}
