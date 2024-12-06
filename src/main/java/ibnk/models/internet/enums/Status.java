package ibnk.models.internet.enums;

public enum Status {
    SUCCESS("SUCCESS"),

    INITIATED("INITIATED"),
    ACTIVATED("ACTIVATED"),
    DISACTIVATED("DISACTIVATED"),
    PENDING("PENDING"),
    DOWNLOADED("DOWNLOADED"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),

    FAILED("FAILED"),

    CANCELLED("CANCELLED"),

    WAITING_FOR_PAYMENT("WAITING_FOR_PAYMENT");

    private final String value;

    Status(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
