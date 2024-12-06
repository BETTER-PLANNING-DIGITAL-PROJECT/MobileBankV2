package ibnk.dto;

import ibnk.models.internet.client.ClientRequest;
import ibnk.models.internet.enums.*;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClientRequestDto {

    @NotNull
    private String status;
    private String comment;
    private String rejectedOn;

    @Data
    public static class BasicRequestDto {
        @NotNull
        private String name;
        private String surname;
        private String town;
        private String uuid;
        @NotNull
        private String accountType;
        @NotNull
        private String countryOfResidence;
        @NotNull
        private String birthDate;
        @NotNull
        private String profession;
        @NotNull
        private String nationality;
        @NotNull
        private String customerType;
        @NotNull
        private String gender;
        @NotNull
        private String email;
        @NotNull
        private String poBox;
        @NotNull
        private String address1;
        private String address2;
        @NotNull
        private String phoneNumber1;

        private String status;
        private String phoneNumber2;
        private String longitude;
        private String latitude;
        //

        private String identificationType;

        private String identificationNumber;
        private String idIssueDate;
        private String idExpDate;
        private String photoUuid;
        private String frontIdUuid;
        private String backIdUuid;
        private String ptcName;
        private String ptcAddress;
        private String ptcPhone;


        public static ClientRequest DtoToModel(BasicRequestDto dto) {
            ClientRequest model = new ClientRequest();
            model.setName(dto.getName());
            model.setSurName(dto.getSurname());
            model.setCountryOfResidence(dto.getCountryOfResidence());
            model.setProfession(dto.getProfession());
            model.setNationality(dto.getNationality());
            model.setBirthDate(dto.getBirthDate());
            model.setCustomerType(CustomerType.valueOf(dto.getCustomerType()).name());
            model.setGender(dto.getGender());
            model.setEmail(dto.getEmail());
            model.setPoBox(dto.getPoBox());
            model.setAddress1(dto.getAddress1());
            model.setAddress2(dto.getAddress2());
            model.setPhoneNumber1(dto.getPhoneNumber1());
            model.setPhoneNumber2(dto.getPhoneNumber2());
            model.setPtcName(dto.getPtcName());
            model.setPtcAddress(dto.getPtcAddress());
            model.setPtcPhoneNumber(dto.getPtcPhone());
            return model;
        }
        public static ClientRequestDto.BasicRequestDto ModelToDto(ClientRequest model) {
            BasicRequestDto dto = new BasicRequestDto();
            dto.setName(model.getName());
            dto.setSurname(model.getSurName());
            dto.setUuid(model.getUuid());
            dto.setCountryOfResidence(model.getCountryOfResidence());
            dto.setBirthDate(model.getBirthDate());
            dto.setProfession(model.getProfession());
            dto.setNationality(model.getNationality());
            dto.setCustomerType(CustomerType.valueOf(model.getCustomerType()).name());
            dto.setGender(model.getGender());
            dto.setEmail(model.getEmail());
            dto.setPoBox(model.getPoBox());
            dto.setAddress1(model.getAddress1());
            dto.setAddress2(model.getAddress2());
            dto.setPhoneNumber1(model.getPhoneNumber1());
            dto.setPhoneNumber2(model.getPhoneNumber2());
            dto.setIdentificationType(model.getIdentificationType());
            dto.setIdIssueDate(model.getIdIssueDate());
            dto.setIdentificationNumber(model.getIdentificationNumber());
            dto.setIdExpDate(model.getIdExpDate());
            if(model.getPhoto() != null)  dto.setPhotoUuid(model.getPhoto().getUuid());
            if(model.getFrontIdentification() != null) dto.setFrontIdUuid(model.getFrontIdentification().getUuid());
            if(model.getBackIdentification() != null) dto.setBackIdUuid(model.getBackIdentification().getUuid());
            dto.setPtcName(model.getPtcName());
            dto.setPtcAddress(model.getPtcAddress());
            dto.setPtcPhone(model.getPtcPhoneNumber());
            dto.setStatus(model.getStatus());
            return dto;
        }

    }

    @Data
    public static class IdentificationDto {
        @Nullable
        private String identificationType;
        @Nullable
        private String identificationNumber;
        @Nullable
        private String idIssueDate;
        private String idExpDate;
        @Nullable
        private String photoUuid;
        @Nullable
        private String frontIdUuid;
        @Nullable
        private String backIdUuid;
        private String ptcName;
        private String ptcAddress;
        private String ptcPhone;
    }
}
