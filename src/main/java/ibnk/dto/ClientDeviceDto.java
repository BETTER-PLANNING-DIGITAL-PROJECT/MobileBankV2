package ibnk.dto;

import lombok.Data;

@Data
public class ClientDeviceDto {
    private String deviceId;
    private String deviceName;
    private String deviceType;
    private String osName;
    private String osVersion;
    private String appVersion;
    private String browserName;
    private String browserVersion;
    private String deviceToken;
    private String ipAddress;
    private Double latitude;
    private Double longitude;
}
