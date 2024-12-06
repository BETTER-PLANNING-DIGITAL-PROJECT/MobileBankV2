package ibnk.tools.error;


import java.util.Date;

public class ErrorDetails {

    private Date timestamp;
    private String message;
    private String details;

    private Object object;

    public ErrorDetails(Date timestamp, String message, String details) {
        super();
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
    }

    public ErrorDetails(Date timestamp, String message, Object object) {
        super();
        this.timestamp = timestamp;
        this.message = message;
        this.object = object;
    }

    public ErrorDetails(Date timestamp, String message, String details, Object object) {
        super();
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
        this.object = object;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
    }

    public Object getObject() {
        return object;
    }
}
