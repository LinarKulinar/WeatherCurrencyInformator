package currency;

/**
 * Данным классом оборачивают ошибки, возникаюшие при запросе курса валюты в классе {@link CurrencyRequester}
 */
public class CurrencyRequestException extends Exception {
    public CurrencyRequestException(Exception e) {
        super(e);
    }

    public CurrencyRequestException(String message) {
        super(message);
    }
}
