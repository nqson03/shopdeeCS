package MainSystem;

public class SystemResponse {
    private final boolean success;
    private final String message;

    public SystemResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
