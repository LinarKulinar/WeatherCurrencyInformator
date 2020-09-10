import currency.CurrencyRequestException;
import weather.WeatherRequestException;

import java.time.LocalTime;

public interface Informator {
    /**
     * Возвращает название города.
     */
    String getCity();

    /**
     * Возвращает время в выбранном городе (на момент запроса).
     */
    LocalTime getTimeInCurrentCity() throws WeatherRequestException;

    /**
     * Возвращает информацию  о погоде в выбранном городе за период времени
     * (сейчас, вчера в это же время и завтра ожидается):
     * температура, скорость  и направление ветра (на русском языке), описание (ветренно, ясно и тд)
     */
    String getThreeDaysWeather() throws WeatherRequestException;

    /**
     * Возвращает информацию  о курсе выбранной валюты (X руб Y коп за X штук)
     * за период времени (вчера, сегодня, завтра);
     */
    String getThreeDaysCurrencyRate() throws CurrencyRequestException;

    /**
     * Вовзвращает информацию в какой из дней: (вчера, сегодня, завтра) стоимость валюты имеет максимальное значение.
     */
    String getThreeDaysMaxCurrencyRateInfo() throws CurrencyRequestException;
}
