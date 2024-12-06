package ibnk.models.internet.enums;

public enum ChannelCode {
    CHANNEL_MTN_CM("CHANNEL_MTN_CM"),
    CHANNEL_OM_CM("CHANNEL_OM_CM"),
    CHANNEL_MTN_AIRTIME_CM("CHANNEL_MTN_AIRTIME_CM"),
    CHANNEL_ORANGE_AIRTIME_CM("CHANNEL_ORANGE_AIRTIME_CM"),
    CHANNEL_NEXTTEL_AIRTIME_CM("CHANNEL_NEXTTEL_AIRTIME_CM"),
    CHANNEL_ENEO_BILLS_CM("CHANNEL_ENEO_BILLS_CM"),
    CHANNEL_CAMWATER_BILLS_CM("CHANNEL_CAMWATER_BILLS_CM"),
    CHANNEL_CANAL_PLUS_BILLS_CM("CHANNEL_CANAL_PLUS_BILLS_CM");
    private String value;

    ChannelCode(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

//CHANNEL_ENEO_BILLS_CM
}
