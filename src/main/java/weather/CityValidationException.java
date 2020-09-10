package weather;

public class CityValidationException extends Exception {
    public CityValidationException(Exception e) {
        super(e);
    }

    public CityValidationException(String message) {
        super(message);
    }
}