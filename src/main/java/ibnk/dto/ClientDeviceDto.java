package ibnk.dto;

import ibnk.models.internet.client.ClientDevice;
import ibnk.models.internet.enums.DeviceType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ClientDeviceDto {
    private String deviceId;
    private String deviceName;
    private DeviceType deviceType;
    private String osName;
    private String osVersion;
    private String appVersion;
    private String browserName;
    private String browserVersion;
    private String deviceToken;
    private String ipAddress;
    private BigDecimal latitude;
    private BigDecimal longitude;

    public static ClientDevice mapToEntity(ClientDeviceDto headerDevice) {
        ClientDevice clientDevice = new ClientDevice();
        return ClientDevice.builder()
                .appVersion(headerDevice.getAppVersion())
                .browserName(headerDevice.getBrowserName())
                .deviceName(headerDevice.getDeviceName())
                .deviceType(headerDevice.getDeviceType())
                .browserVersion(headerDevice.getBrowserVersion())
                .isTrusted(true)
                .isActive(true)
                .lastLoginTime(LocalDateTime.now())
                .latitude(headerDevice.getLatitude())
                .longitude(headerDevice.getLongitude())
                .osName(headerDevice.getOsName())
                .osVersion(headerDevice.getOsVersion())
                .build();
    }
}
