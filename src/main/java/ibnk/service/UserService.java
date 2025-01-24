package ibnk.service;

import ibnk.dto.DeviceDto;
import ibnk.dto.UserDto;
import ibnk.dto.auth.UpdatePasswordDto;
import ibnk.models.banking.Employee;
import ibnk.models.internet.UserEntity;
import ibnk.models.internet.authorization.Roles;
import ibnk.models.internet.client.ClientDevice;
import ibnk.models.internet.client.ClientDeviceArchive;
import ibnk.models.internet.client.ClientDeviceId;
import ibnk.models.internet.client.Subscriptions;
import ibnk.repositories.banking.EmployeeRepository;
import ibnk.repositories.internet.ClientDeviceArchiveRepository;
import ibnk.repositories.internet.ClientDeviceRepository;
import ibnk.repositories.internet.RolesRepo;
import ibnk.repositories.internet.UserRepository;
import ibnk.tools.error.ResourceNotFoundException;
import ibnk.tools.error.UnauthorizedUserException;
import ibnk.tools.error.ValidationException;
import ibnk.tools.security.PasswordConstraintValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Service()
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RolesRepo rolesRepo;
    public final PasswordEncoder passwordEncoder;
    public final ClientDeviceRepository clientDeviceRepository;
    public final ClientDeviceArchiveRepository clientDeviceArchiveRepository;
    private final EmployeeRepository employeeRepository;
    @Autowired
    @Qualifier("bankingJdbcTemplate")
    JdbcTemplate bankingJdbcTemplate;
    private final Logger LOGGER = LogManager.getLogger(UserService.class);

    public Employee findEmployeeById(String employeeId) throws SQLException, ResourceNotFoundException {
        Optional<Employee> employee = employeeRepository.findByMatricule(employeeId);
        if (employee.isEmpty()) throw new ResourceNotFoundException("employee_not_found");
        return employee.get();
    }

    /**
     * @param uuid the uuid of the user
     * @return CreateUserDto
     * @throws ResourceNotFoundException not found
     */
    public UserEntity findUserByUuid(String uuid) throws ValidationException {
        var user = userRepository.findUserByUuid(uuid);
        if (user.isEmpty()) {
            throw new ValidationException("User does not Exist");
        }
        return user.get();
    }

    public String createUser(UserDto.CreateUserDto dao) throws  ResourceNotFoundException, SQLException {
        if (userRepository.findByUserLogin(dao.getUserLogin()).isPresent()) {
            throw new ResourceNotFoundException("user already Exist");
        }
        if (!Objects.equals(dao.getPassword(), dao.getConfirm_password())) {
            throw new ResourceNotFoundException("password_dont_match");
        }
        Optional<Roles> userRole = rolesRepo.findById(dao.getUserRoleId());
        if (userRole.isEmpty()) throw new ResourceNotFoundException("role_not_found");
        Employee employee = findEmployeeById(dao.getUserLogin());
        var user = UserEntity.builder()
                .passExpiration(false)
                .userLogin(dao.getUserLogin())
                .branchCode(employee.getId().getAgence())
                .name(employee.getEmployeeName())
                .role(userRole.get())
                .firstLogin(false)
                .password(passwordEncoder.encode(dao.getPassword()))
                .doubleAuthentication(false)
                .passDuration(dao.getPassDuration())
                .passPeriodicity(dao.getPassPeriodicity())
                .build();
        LOGGER.info("Enter >> register()");
        userRepository.save(user);
        return "successful";
    }

    public String updateUser(String uuid, UserDto.CreateUserDto dto) throws  ValidationException {
        UserEntity fnd = findUserByUuid(uuid);
        UserEntity updUser = UserDto.CreateUserDto.DtoToModel(dto);
        if(dto.getRoleId() != null) {
            Optional<Roles> role = rolesRepo.findById(dto.getRoleId());
            fnd.setRole(role.orElseThrow(() -> new ValidationException("Role Error")));
        }
        if(updUser.getPassword() != null) {
            fnd.setPassword(passwordEncoder.encode(updUser.getPassword()));
        }
        userRepository.save(fnd);
//        UserDto.CreateUserDto.modelToDao(val);
        return "Updated";

    }


    /**
     * @param id
     * @return
     * @throws UnauthorizedUserException
     */
    public UserDto.CreateUserDto findById(long id) throws UnauthorizedUserException {
        var user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new UnauthorizedUserException("User does not Exist");
        }
        return UserDto.CreateUserDto.modelToDao(user.get());
    }

    /**
     * @param user
     * @param pass
     * @return
     * @throws UnauthorizedUserException
     */
    public UserEntity UpdatePassword(UserEntity user, UpdatePasswordDto pass) throws UnauthorizedUserException, ValidationException {
        var isValid = PasswordConstraintValidator.isAcceptablePassword(pass.getConfirmPassword());
        if (!isValid) {
            LOGGER.catching(new UnauthorizedUserException("Verify PasswordConstraintValidator Class"));
        }
        boolean isNewPass = pass.getNewPassword().matches(pass.getConfirmPassword());
        if (!isNewPass) {
            throw new ValidationException("NewPassword does not match ConfirmPassword.");
        }

        boolean isPasswordMatch = passwordEncoder.matches(pass.getOldPassword(),
                user.getPassword());
        if (!isPasswordMatch) {
            throw new ValidationException("Old Password does not match");
        }
        pass.setNewPassword(passwordEncoder.encode(pass.getConfirmPassword()));
        user.setPassword(pass.getNewPassword());
        userRepository.save(user);
        return userRepository.save(user);
    }


    public UserEntity loadUserByUuid(String uuid) throws UsernameNotFoundException {
        LOGGER.info("Enter >> loadUserByUuiService");
        UserEntity user = userRepository.findUserByUuid(uuid).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        LOGGER.info("Exit >> loadUserByUuiService");
        return user;
    }

     public List<DeviceDto> listDevices(String uuid) {
        return clientDeviceRepository.findByUserUuid(uuid).stream().map(clientDevice -> new DeviceDto().mapToDeviceDto(clientDevice)).toList();
    }

    @Transactional
    public void archiveClientDevices(String uuid) {
        List<ClientDevice> devices = clientDeviceRepository.findByUserUuid(uuid);

        List<ClientDeviceArchive> archives = new ArrayList<>();
        for (ClientDevice device : devices) {
            ClientDeviceArchive archive = new ClientDeviceArchive();

            archive.setId(device.getId());
            archive.setUuid(device.getUuid());
            archive.setDeviceName(device.getDeviceName());
            archive.setDeviceType(device.getDeviceType());
            archive.setBrowserName(device.getBrowserName());
            archive.setAppVersion(device.getAppVersion());
            archive.setIpAddress(device.getIpAddress());
            archive.setLastLoginTime(device.getLastLoginTime());
            archive.setBrowserVersion(device.getBrowserVersion());
            archive.setIsTrusted(device.getIsTrusted());
            archive.setLongitude(device.getLongitude());
            archive.setLatitude(device.getLatitude());
            archives.add(archive);
        }
        clientDeviceArchiveRepository.saveAll(archives);
        clientDeviceRepository.deleteAllInBatch(devices);

    }

//    @Transactional
//    public void archiveClientDevice(String deviceId, Subscriptions sub) throws ResourceNotFoundException {
//        ClientDeviceId clientDeviceId = new ClientDeviceId(sub, deviceId, );
//        ClientDevice devices = clientDeviceRepository.findById(clientDeviceId).orElseThrow(()-> new ResourceNotFoundException("device-not-found"));
//
//            ClientDeviceArchive archive = new ClientDeviceArchive();
//            archive.setId(devices.getId());
//            archive.setUuid(devices.getUuid());
//            archive.setDeviceName(devices.getDeviceName());
//            archive.setDeviceType(devices.getDeviceType());
//            archive.setBrowserName(devices.getBrowserName());
//            archive.setAppVersion(devices.getAppVersion());
//            archive.setIpAddress(devices.getIpAddress());
//            archive.setLastLoginTime(devices.getLastLoginTime());
//            archive.setBrowserVersion(devices.getBrowserVersion());
//            archive.setIsTrusted(devices.getIsTrusted());
//            archive.setLongitude(devices.getLongitude());
//            archive.setLatitude(devices.getLatitude());
//
//        clientDeviceArchiveRepository.save(archive);
//        clientDeviceRepository.delete(devices);
//    }


}