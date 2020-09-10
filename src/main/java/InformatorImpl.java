import currency.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weather.CityValidationException;
import weather.WeatherRequestException;
import weather.WeatherRequester;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.TreeSet;

public class InformatorImpl implements Informator {


    private String city;
    private CurrencyCode currencyCode;
    private CurrencyRequester currencyRequester = new CurrencyRequester();
    private WeatherRequester weatherRequester = new WeatherRequester();


    private static final Logger logger = LoggerFactory.getLogger(InformatorImpl.class);

    /**
     * При вызове данного конструктора проверяется корректность принятого города city.
     *
     * @param city
     * @param currencyCode
     * @throws CityValidationException возникает, если город не найден в списке городов на сервере погоды
     * @throws WeatherRequestException возникает, если произошла ошибка при запросе погоды
     */
    public InformatorImpl(String city, CurrencyCode currencyCode) throws CityValidationException, WeatherRequestException {
        this.currencyCode = currencyCode;
        this.city = weatherRequester.getCityFromResponse(city); //записываем город из response сервера
    }

    /**
     * Возвращает название города.
     * Может не совпадать с введенным названием города в конструкторе {@link InformatorImpl}.
     * Сервер может испаравить название города.
     *
     * @return
     */
    @Override
    public String getCity() {
        return this.city;
    }


    /**
     * Время в выбранном городе (на момент запроса).
     * Время возвращается с точностью до 1 минуты.
     *
     * @return
     * @throws WeatherRequestException
     */
    @Override
    public LocalTime getTimeInCurrentCity() throws WeatherRequestException {
        return weatherRequester.getLocalDateTimeInCity(city).toLocalTime();
    }

    /**
     * Дата в выбранном городе (на момент запроса).
     *
     * @return
     * @throws WeatherRequestException
     */
    private LocalDate getDateInCurrentCity() throws WeatherRequestException {
        return weatherRequester.getLocalDateTimeInCity(city).toLocalDate();
    }

    /**
     * Возвращает информацию  о погоде в выбранном городе за период времени
     * (сейчас, вчера в это же время и завтра ожидается):
     * температура, скорость  и направление ветра (на русском языке), описание (ветренно, ясно и тд)
     */
    @Override
    public String getThreeDaysWeather() throws WeatherRequestException {
        return weatherRequester.getWeather(city);
    }


    /**
     * Возвращает строку с информацией о курсе выбранной валюты currencyCode за три дня: вчера, сегодня, завтра
     * Даты в часовом поясе выбранного города city
     *
     * @return Строка в формате "X руб Y коп за Z штук" в каждый из трех дней: вчера, сегодня, завтра
     * @throws CurrencyRequestException
     * @throws WeatherRequestException  может возникнуть при проблемах с запросом даты в заданном городе.
     *                                  Запрашивание даты происходит через сервер с погодой.
     */
    @Override
    public String getThreeDaysCurrencyRate() throws CurrencyRequestException {
        logger.info(String.format("Запрошен курс валюты %s", currencyCode));
        String rateGreeting = "Информация о валюте " + currencyCode;
        LocalDate dateToday = null;
        try {
            dateToday = getDateInCurrentCity();
        } catch (WeatherRequestException e) {
            logger.error("Произошла ошибка при запросе даты с погодного сервера");
            throw new CurrencyRequestException(e);
        }
        String rateYesterday;
        String rateToday;
        String rateTomorrow;
        try {
            logger.debug("Получаем информацию о курсе валюты на вчера");
            LocalDate dateYesterday = dateToday.minusDays(1);
            rateYesterday = "Вчера стоимость составила - " +
                    currencyRequester.getCurrency(dateYesterday, currencyCode).toHumanReadableString();
        } catch (MissingRequestedDateException e) {
            rateYesterday = "На вчера валютный курс недоступен";
        }
        try {
            logger.debug("Получаем информацию о курсе валюты на сегодня");
            rateToday = "Сегодня стоимость составляет - " +
                    currencyRequester.getCurrency(dateToday, currencyCode).toHumanReadableString();
        } catch (MissingRequestedDateException e) {
            rateToday = "На сегодня валютный курс недоступен";
        }

        try {
            logger.debug("Получаем информацию о курсе валюты на завтра");
            LocalDate dateTomorrow = dateToday.plusDays(1);
            rateTomorrow = "Завтра стоимость составит - " +
                    currencyRequester.getCurrency(dateTomorrow, currencyCode).toHumanReadableString();
        } catch (MissingRequestedDateException e) {
            rateTomorrow = "На завтра валютный курс недоступен";
        }
        return rateGreeting + "\n" + rateYesterday + "\n" + rateToday + "\n" + rateTomorrow;
    }


    /**
     * Вовзвращает информацию в какой из дней: (вчера, сегодня, завтра) стоимость валюты имеет максимальное значение.
     *
     * @return Человекочитаемая строка с описанием наибольшего курса за три дня
     * @throws CurrencyRequestException
     * @throws WeatherRequestException  может возникнуть при запросе даты в заданном городе внутри этого метода
     */
    @Override
    public String getThreeDaysMaxCurrencyRateInfo() throws CurrencyRequestException {
        LocalDate dateToday = null;
        try {
            dateToday = getDateInCurrentCity();
        } catch (WeatherRequestException e) {
            logger.error("Произошла ошибка при запросе даты с погодного сервера");
            throw new CurrencyRequestException(e);
        }
        LocalDate dateYesterday = dateToday.minusDays(1);
        LocalDate dateTomorrow = dateToday.plusDays(1);
        TreeSet<Currency> currencyTreeSet = new TreeSet<>();

        try {
            logger.debug("Получаем информацию о курсе валюты на вчера");
            Currency currency = currencyRequester.getCurrency(dateYesterday, currencyCode);
            currencyTreeSet.add(currency);
        } catch (MissingRequestedDateException e) {
        }
        try {
            logger.debug("Получаем информацию о курсе валюты на сегодня");
            Currency currency = currencyRequester.getCurrency(dateToday, currencyCode);
            currencyTreeSet.add(currency);
        } catch (MissingRequestedDateException e) {
        }

        try {
            logger.debug("Получаем информацию о курсе валюты на завтра");
            Currency currency = currencyRequester.getCurrency(dateTomorrow, currencyCode);
            currencyTreeSet.add(currency);
        } catch (MissingRequestedDateException e) {
        }
        String answer = null;

        if (currencyTreeSet.size() == 0) { // Если у нас нет информации ни обо одном дне среди трех запрошенных
            String message = "За вчера сегодня и завтра значение курса " + currencyCode + " неизвестно.";
            logger.warn(message);
            return message;
        }

        Currency maxRate = currencyTreeSet.last();
        String suffixAnswer = null;
        if (maxRate.getDate().equals(dateYesterday)) {
            suffixAnswer = "вчера и составил - " + maxRate.toHumanReadableString();
            logger.debug("Самый большой курс был вчера");
        }
        if (maxRate.getDate().equals(dateToday)) {
            suffixAnswer = "сегодня и составляет - " + maxRate.toHumanReadableString();
            logger.debug("Самый большой курс сегодня");
        }
        if (maxRate.getDate().equals(dateTomorrow)) {
            suffixAnswer = "завтра и составит - " + maxRate.toHumanReadableString();
            logger.debug("Самый большой курс будет завтра");
        }
        switch (currencyTreeSet.size()) {
            case 1: // Если у нас есть информация только о одном дне среди трех запрошенных
                answer = "Курс " + currencyCode + " за (вчера, сегодня, завтра) известен только на " + suffixAnswer;
                break;
            case 2: // Если у нас есть информация только двух днях среди трех запрошенных
            case 3: // Если у нас есть информация о всех трех днях среди трех запрошенных
                answer = "Максимальный курс " + currencyCode + " наблюдаем " + suffixAnswer;
                break;
        }
        return answer;
    }

}
