package ibnk.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import ibnk.models.internet.client.Subscriptions;
import ibnk.models.internet.UserEntity;
import ibnk.models.internet.enums.NotificationChanel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Data
public class UserDto {


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateUserDto {
        private String userLogin;
        private Long userRoleId;
        private String password;
        private String status;
        private String confirm_password;
        private Boolean passExpiration;
        private Long passDuration;
        private String passPeriodicity;
        private Long roleId;

        public static CreateUserDto modelToDao(UserEntity model) {
            CreateUserDto cus = new CreateUserDto();
            cus.setUserLogin(model.getUsername());
            return cus;
        }

        public static UserEntity DtoToModel(CreateUserDto dto) {
            UserEntity cus = new UserEntity();
//            cus.setUserLogin(dto.getUserLogin());
            cus.setPassword(dto.getPassword());
//            cus.setPassExpiration(dto.getPassExpiration());

            return cus;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateSubscriberClientDto {
        @JsonIgnore
        private Long id;
        @JsonIgnore
        private String uuid;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime subscriptionDate;
        private String email;
        private String name;
        private String userLogin;
        private Boolean firstLogin;
        private Boolean newDeviceLogin;
        private Boolean subscriber;
        private Integer securityQuestionCounts;
        private String primaryAccount;
        private String productName;
        private String clientMatricule;
        private String preferredOtpChannel;
        private String phoneNumber;
        private String userName;
        private Long subscriberBy;
        @JsonIgnore
        private String password;
        @JsonIgnore
        private String pin;
        private Boolean passExpiration;
        private Long passDuration;
        private String passPeriodicity;
        private Boolean isPasswordExpired;
        private LocalDateTime passwordExpiredDate;
        private String address;
        private String status;

        @JsonIgnore
        private String generatedPassword;
        private Boolean doubleAuthentication;

        public static CreateSubscriberClientDto modelToDao(Subscriptions model) {
            CreateSubscriberClientDto dto = new CreateSubscriberClientDto();
            dto.setName(model.getClientName());
            dto.setClientMatricule(model.getClientMatricul());
            dto.setProductName(model.getProductName());
            dto.setUserLogin(model.getUserLogin());
            dto.setFirstLogin(model.getFirstLogin());
            dto.setClientMatricule(model.getClientMatricul());
            dto.setPrimaryAccount(model.getPrimaryAccount());
            dto.setPhoneNumber(model.getPhoneNumber());
            dto.setUserName(model.getUserLogin());
            dto.setEmail(model.getEmail());
            dto.setId(model.getId());
            dto.setUuid(model.getUuid());
            dto.setSubscriptionDate(model.getSubscriptionDate());
            dto.setAddress(model.getAddress());
            dto.setDoubleAuthentication(model.getDoubleAuthentication());
            dto.setPasswordExpiredDate(model.getPassExpirationDate());
            dto.setIsPasswordExpired(model.isPasswordExpired());
            dto.setPassExpiration(model.getPassExpiration());
            dto.setPassPeriodicity(model.getPassPeriodicity());
            dto.setPassDuration(model.getPassDuration());
            dto.setPreferredOtpChannel(model.getPreferedNotificationChanel().name());
            dto.setStatus(model.getStatus());
            //dto.setIsPasswordExpired(model.getI);

            return dto;
        }

        public static Subscriptions DtoToModel(UserDto.CreateSubscriberClientDto dto) {
            Subscriptions MDL = new Subscriptions();
            MDL.setClientName(dto.getName());
            MDL.setProductName(dto.getProductName());
            MDL.setClientMatricul(dto.getClientMatricule());
            MDL.setPrimaryAccount(dto.getPrimaryAccount());
            MDL.setPassword(dto.password);
            MDL.setId(dto.getId());
            MDL.setUuid(dto.getUuid());
            MDL.setSubscriptionDate(dto.getSubscriptionDate());
            MDL.setUserLogin(dto.getUserName());
            MDL.setPassExpiration(dto.getPassExpiration());
            MDL.setDoubleAuthentication(dto.getDoubleAuthentication());
            MDL.setPreferedNotificationChanel(NotificationChanel.valueOf(dto.getPreferredOtpChannel()));
            return MDL;
        }

    }

    @Data
    public static class EmployeeDto {
        private String employeeId;

        private String branch;

        private String employeeName;

        private String quality;

        private String status;

        private boolean suspension;

        public static EmployeeDto DtoToModel(ResultSet map) throws SQLException {
            EmployeeDto model = new EmployeeDto();
            model.setBranch(map.getString("Agence"));
            model.setEmployeeId(map.getString("Matricule"));
            model.setEmployeeName(map.getString("NomPrenom"));
            model.setQuality(map.getString("LibQualite"));
            model.setStatus(map.getString("Status"));
            model.setSuspension(map.getBoolean("Suspension"));
            return model;
        }
    }


    //

}