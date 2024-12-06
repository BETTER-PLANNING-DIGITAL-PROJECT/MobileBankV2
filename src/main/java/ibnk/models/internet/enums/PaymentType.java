package ibnk.models.internet.enums;

public enum PaymentType {
    cash_collect("cash_collect"),

    payout("payout");

    private String value;
    PaymentType(String value) {
        this.value = value;
    }
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
