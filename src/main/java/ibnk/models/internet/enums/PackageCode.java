package ibnk.models.internet.enums;

public enum PackageCode {
    STANDARD_MOBILE("001");

    private final String code;

    PackageCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
