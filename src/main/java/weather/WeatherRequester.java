package weather;

import URLBuilder.URLBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WeatherRequester {
    private String urlToJson = "https://api.weatherapi.com/v1/";
    private String token = "e981d951eed148cb837101739200309";

    private static final Logger logger = LoggerFactory.getLogger(WeatherRequester.class);

    public LocalDateTime getLocalDateTimeInCity(String city) throws WeatherRequestException {
        try {
            URL url = new URLBuilder(urlToJson + "current.json")
                    .withParam("key", token)
                    .withParam("q", city)
                    .withParam("lang", "ru")
                    .build();
            logger.info("Сформирован URL: " + url);

            String jsonText = getJSONFromURL(url);

            // Считываем json
            JSONObject jo = (JSONObject) new JSONParser().parse(jsonText);
            //достаем данные
            JSONObject location = (JSONObject) jo.get("location");
            String dateTimeString = (String) location.get("localtime");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd H:m"); // может попасться строка вида: 2020-09-09 1:36
            return LocalDateTime.parse(dateTimeString, formatter);
        } catch (MalformedURLException e) {
            throw new WeatherRequestException(e);
        } catch (ParseException e) {
            throw new WeatherRequestException(e);
        } catch (UnsupportedEncodingException e) {
            throw new WeatherRequestException(e);
        } catch (ProtocolException e) {
            throw new WeatherRequestException(e);
        } catch (IOException e) {
            throw new WeatherRequestException(e);
        }
    }

    public String getCityFromResponse(String city) throws CityValidationException, WeatherRequestException {
        logger.info("Проверяем принятие города \"" + city + "\" сервером погоды");
        try {
            URL url = new URLBuilder(urlToJson + "current.json")
                    .withParam("key", token)
                    .withParam("q", city)
                    .withParam("lang", "ru")
                    .build();
            logger.info("Сформирован URL: " + url);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            logger.debug("Код ответа сервера - " + connection.getResponseCode());
            if (connection.getResponseCode() == 400)
                throw new CityValidationException("Город не найден в списке городов на сервере погоды");

            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            logger.debug(jsonText);

            // Считываем json
            JSONObject jo = (JSONObject) new JSONParser().parse(jsonText);
            //достаем данные
            JSONObject location = (JSONObject) jo.get("location");
            String cityInResponse = (String) location.get("region");
            return cityInResponse;
        } catch (MalformedURLException e) {
            throw new WeatherRequestException(e);
        } catch (ParseException e) {
            throw new WeatherRequestException(e);
        } catch (UnsupportedEncodingException e) {
            throw new WeatherRequestException(e);
        } catch (ProtocolException e) {
            throw new WeatherRequestException(e);
        } catch (IOException e) {
            throw new WeatherRequestException(e);
        }
    }

    /**
     * Метод предназначен для считывания json из {@link Reader} в строку
     *
     * @param rd
     * @return
     * @throws IOException
     */
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    private String getJSONFromURL(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        logger.debug("Код ответа сервера - " + connection.getResponseCode());

        InputStream is = connection.getInputStream();

        BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        String jsonText = readAll(rd);
        logger.debug(jsonText);
        return jsonText;
    }

    private String getWeatherToday(String city) throws WeatherRequestException {
        try {
            URL url = new URLBuilder(urlToJson + "current.json")
                    .withParam("key", token)
                    .withParam("q", city)
                    .withParam("lang", "ru")
                    .build();
            logger.info("Сформирован URL: " + url);

            String jsonText = getJSONFromURL(url);

            // Считываем json и кастим
            JSONObject jo = (JSONObject) new JSONParser().parse(jsonText);
            //достаем данные
            JSONObject current = (JSONObject) jo.get("current");
            double temperature = (double) current.get("temp_c");
            double windVelocity = (double) current.get("wind_kph");
            long windDirect = (long) current.get("wind_degree");
            JSONObject condition = (JSONObject) current.get("condition");
            String weatherCondition = (String) condition.get("text");
            return "Сегодня сейчас:\n" +
                    weatherCondition + "\n" +
                    "Температура " + temperature + " °C\n" +
                    "Скорость ветра " + windVelocity + " км/ч\n" +
                    "Направление ветра " + windDirect + " градусов"; //todo переделать на словарные обозначения
        } catch (MalformedURLException e) {
            throw new WeatherRequestException(e);
        } catch (ParseException e) {
            throw new WeatherRequestException(e);
        } catch (UnsupportedEncodingException e) {
            throw new WeatherRequestException(e);
        } catch (ProtocolException e) {
            throw new WeatherRequestException(e);
        } catch (IOException e) {
            throw new WeatherRequestException(e);
        }
    }

    private String getWeatherYesterday(String city) throws WeatherRequestException {
        try {
            LocalDateTime timeYesterday = getLocalDateTimeInCity(city).minusDays(1);
            URL url = new URLBuilder(urlToJson + "history.json")
                    .withParam("key", token)
                    .withParam("q", city)
                    .withParam("lang", "ru")
                    .withParam("dt", String.valueOf(timeYesterday.toLocalDate()))
//                    .withParam("unixdt", String.valueOf(unixTime))
                    .withParam("hour", String.valueOf(timeYesterday.getHour()))
                    .build();
            logger.info("Сформирован URL: " + url);

            String jsonText = getJSONFromURL(url);

            // Считываем json и кастим
            JSONObject jo = (JSONObject) new JSONParser().parse(jsonText);
            //достаем данные
            //todo разобраться, какие типы данных тут парсятся. Почему simplejson что-то предлагает/ругается?
            JSONObject forecast = (JSONObject) jo.get("forecast");
            JSONArray forecastdayArr = (JSONArray) forecast.get("forecastday");
            JSONObject forecastday = (JSONObject) forecastdayArr.get(0);
            JSONArray hourArray = (JSONArray) forecastday.get("hour");
            JSONObject hour = (JSONObject) hourArray.get(0);
            double temperature = (double) hour.get("temp_c");
            double windVelocity = (double) hour.get("wind_kph");
            long windDirect = (long) hour.get("wind_degree"); //todo а тут может быть пустое поле передано при скорости ветра = 0?
            JSONObject condition = (JSONObject) hour.get("condition");
            String weatherCondition = (String) condition.get("text");
            return "Вчера в это время:\n" +
                    weatherCondition + "\n" +
                    "Температура " + temperature + " °C\n" +
                    "Скорость ветра " + windVelocity + " км/ч\n" +
                    "Направление ветра " + windDirect + " градусов"; //todo переделать на словарные обозначения
        } catch (MalformedURLException e) {
            throw new WeatherRequestException(e);
        } catch (ParseException e) {
            throw new WeatherRequestException(e);
        } catch (UnsupportedEncodingException e) {
            throw new WeatherRequestException(e);
        } catch (ProtocolException e) {
            throw new WeatherRequestException(e);
        } catch (IOException e) {
            throw new WeatherRequestException(e);
        }
    }

    private String getWeatherTomorrow(String city) throws WeatherRequestException {
        try {
            LocalDateTime timeYesterday = getLocalDateTimeInCity(city).minusDays(1);
            URL url = new URLBuilder(urlToJson + "forecast.json")
                    .withParam("key", token)
                    .withParam("q", city)
                    .withParam("lang", "ru")
                    .withParam("dt", String.valueOf(timeYesterday.toLocalDate()))
//                    .withParam("unixdt", String.valueOf(unixTime))
                    .withParam("hour", String.valueOf(timeYesterday.getHour()))
                    .build();
            logger.info("Сформирован URL: " + url);

            String jsonText = getJSONFromURL(url);

            // Считываем json и кастим
            JSONObject jo = (JSONObject) new JSONParser().parse(jsonText);
            //достаем данные
            JSONObject forecast = (JSONObject) jo.get("forecast");
            JSONArray forecastdayArr = (JSONArray) forecast.get("forecastday");
            JSONObject forecastday = (JSONObject) forecastdayArr.get(0);
            JSONObject hour = (JSONObject) forecastday.get("day");
            double temperature = (double) hour.get("avgtemp_c");
            double windVelocity = (double) hour.get("maxwind_kph");
            JSONObject condition = (JSONObject) hour.get("condition");
            String weatherCondition = (String) condition.get("text");
            return "Завтра ожидается:\n" +
                    weatherCondition + "\n" +
                    "Температура " + temperature + " °C\n" +
                    "Максимальная скорость ветра " + windVelocity + " км/ч\n";
        } catch (MalformedURLException e) {
            throw new WeatherRequestException(e);
        } catch (ParseException e) {
            throw new WeatherRequestException(e);
        } catch (UnsupportedEncodingException e) {
            throw new WeatherRequestException(e);
        } catch (ProtocolException e) {
            throw new WeatherRequestException(e);
        } catch (IOException e) {
            throw new WeatherRequestException(e);
        }
    }

    public String getWeather(String city) throws WeatherRequestException {
        String weatherGreeting = "Погода в городе " + city + ".";
        return weatherGreeting + "\n\n" +
                getWeatherYesterday(city) + "\n\n" +
                getWeatherToday(city) + "\n\n" +
                getWeatherTomorrow(city);
    }


}
