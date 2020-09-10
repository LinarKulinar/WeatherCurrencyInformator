import currency.CurrencyCode;
import currency.CurrencyRequestException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weather.CityValidationException;
import weather.WeatherRequestException;

public class InformatorImplTest {
    Informator informator;
    private static final Logger logger = LoggerFactory.getLogger(InformatorImplTest.class);


    {
        try {
            informator = new InformatorImpl("Moscow", CurrencyCode.EUR);
        } catch (CityValidationException e) {
            e.printStackTrace();
        } catch (WeatherRequestException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCity() throws Exception {
        logger.info("Проверяем возвращение города");
        System.out.println();
        System.out.println(informator.getCity());
        System.out.println();
    }

    @Test
    public void getTime() throws WeatherRequestException {
        logger.info("Проверяем возвращение времени");
        System.out.println();
        System.out.println(informator.getTimeInCurrentCity());
        System.out.println();
    }

    @Test
    public void getWeather() throws WeatherRequestException {
        logger.info("Проверяем возвращение погоды");
        System.out.println();
        System.out.println(informator.getThreeDaysWeather());
        System.out.println();
    }

    @Test
    public void getCurrencyRate() throws CurrencyRequestException {
        logger.info("Проверяем возвращение курса валют");
        System.out.println();
        System.out.println(informator.getThreeDaysCurrencyRate());
        System.out.println();
    }

    @Test
    public void getMaxCurrencyRate() throws CurrencyRequestException {
        logger.info("Проверяем возвращение максимального курса валют");
        System.out.println();
        System.out.println(informator.getThreeDaysMaxCurrencyRateInfo());
        System.out.println();
    }
}
