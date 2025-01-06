package ibnk.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import ibnk.models.internet.client.ClientDevice;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DeviceDto {

    private String uuid;
    @JsonIgnore
    private String deviceId;
    private String deviceName;
    private String deviceType;
    private String osName;
    private String osVersion;
    private String appVersion;
    private String browserName;
    private String browserVersion;
    @JsonIgnore
    private String deviceToken;
    private String ipAddress;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastLoginTime;
    private Boolean isTrusted;
    private Boolean isActive;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private UserDto.CreateSubscriberClientDto user; // Use the dedicated DTO here

    // Getters and Setters for all fields
    public DeviceDto mapToDeviceDto(ClientDevice device) {
        DeviceDto dto = new DeviceDto();
        dto.setUuid(device.getUuid());
        dto.setDeviceId(device.getId().getDeviceId());
        dto.setDeviceName(device.getDeviceName());
        dto.setDeviceType(device.getDeviceType().name());
        dto.setOsName(device.getOsName());
        dto.setOsVersion(device.getOsVersion());
        dto.setAppVersion(device.getAppVersion());
        dto.setBrowserName(device.getBrowserName());
        dto.setBrowserVersion(device.getBrowserVersion());
        dto.setDeviceToken(device.getDeviceToken());
        dto.setIpAddress(device.getIpAddress());
        dto.setLastLoginTime(device.getLastLoginTime());
        dto.setIsTrusted(device.getIsTrusted());
        dto.setIsActive(device.getIsActive());
        dto.setLatitude(device.getLatitude());
        dto.setLongitude(device.getLongitude());
        dto.setUser( UserDto.CreateSubscriberClientDto.modelToDao(device.getId().getUserId()));

        return dto;
    }
}