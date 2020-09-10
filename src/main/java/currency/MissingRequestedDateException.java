package currency;

public class MissingRequestedDateException extends Exception {
    public MissingRequestedDateException(Exception e) {
        super(e);
    }

    public MissingRequestedDateException(String message) {
        super(message);
    }
}
