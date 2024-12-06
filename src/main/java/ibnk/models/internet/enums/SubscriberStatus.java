package ibnk.models.internet.enums;

public enum SubscriberStatus {
    ACTIVE("ACTIVE"),

    INACTIVE("INACTIVE"),

    PENDING("PENDING"),

    SUSPENDED("SUSPENDED"),

    BLOCKED("BLOCKED");

    private final String value;

    SubscriberStatus(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
