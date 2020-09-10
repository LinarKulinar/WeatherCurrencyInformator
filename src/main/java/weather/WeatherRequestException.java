package weather;

public class WeatherRequestException extends Exception{
    public WeatherRequestException(Exception e) {
        super(e);
    }

    public WeatherRequestException(String message) {
        super(message);
    }
}
