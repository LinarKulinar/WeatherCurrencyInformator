package currency;

import URLBuilder.URLBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class CurrencyRequester {
    private String pathXsd = "src/main/resources/currency.xsd";
    private String urlToXml = "http://www.cbr.ru/scripts/XML_daily.asp";
    private static final Logger logger = LoggerFactory.getLogger(CurrencyRequester.class);


    public Currency getCurrency(LocalDate date, CurrencyCode currencyCode) throws MissingRequestedDateException, CurrencyRequestException{
        List<Currency> currencyList = getCurrencyList(date);
        for (Currency currency : currencyList) {
            if (currency.getCharCode() == currencyCode) {
                return currency;
            }
        }
        throw new CurrencyRequestException("Произошла ошибка в логике работы приложения. " +
                "В возвращенном списке из метода getCurrencyList отсутствует валюта, заданная в currencyCode");
    }

    /**
     * В данном методе выполняется запрос к серверу для получения курса валюты в заданный день.
     * Также тут парсится XML, который вернул сервер
     *
     * @param dateInRequest день, в который запрашивается курс валюты
     * @return список распарсенных объектов {@link Currency}
     * @throws MissingRequestedDateException возникает, если сервер не обладает информацией о курсе валюты на заданную дату
     * @throws CurrencyRequestException возникает при ошибках на уровне класса {@link CurrencyRequester}
     */
    public List<Currency> getCurrencyList(LocalDate dateInRequest) throws MissingRequestedDateException, CurrencyRequestException {
        ArrayList<Currency> currencies = new ArrayList<>();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            // Настроим валидацию средствами DOM
            dbf.setValidating(false);
            SchemaFactory factory = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            Schema schema = factory.newSchema(new StreamSource(pathXsd));
            dbf.setSchema(schema);

            // Подгружаем XML по URL
            DateTimeFormatter dateFormatForURL = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            URL url = new URLBuilder(urlToXml)
                    .withParam("date_req", dateInRequest.format(dateFormatForURL))
                    .build();
            logger.info("Сформирован URL: " + url);
            InputStream stream = url.openStream();

            //Парсим XML
            DocumentBuilder docBuilder = dbf.newDocumentBuilder(); // Получили из фабрики билдер, который распарсит XML
            Document document = docBuilder.parse(stream); // Запарсили в структуру Document


            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            LocalDate dateInResponse = LocalDate.parse(document.getDocumentElement().getAttribute("Date"), dateFormat);

            logger.debug("Дата в запросе на сервер: " + dateInRequest.format(dateFormat) + ", " +
                    "а в ответе с сервера: " + dateInResponse.format(dateFormat));

            if (!dateInRequest.equals(dateInResponse)) {
                String message = "На сервере отсутствует необходмая информация о требуемой дате. " +
                        "Запрашивалась дата: " + dateInRequest.format(dateFormat) + ", " +
                        "а сервер вернул курс валюты на: " + dateInResponse.format(dateFormat);
                logger.warn(message);
                throw new MissingRequestedDateException(message);
            }

            // Получение списка всех элементов <Valute> внутри корневого элемента
            NodeList valuteElements = document.getDocumentElement().getElementsByTagName("Valute");

            // Перебор всех элементов <Valute>
            for (int i = 0; i < valuteElements.getLength(); i++) {
                Node valute = valuteElements.item(i);
                NodeList infoValute = valute.getChildNodes();
                // Парсим в объект Currency
                String ID = valute.getAttributes().getNamedItem("ID").getNodeValue();
                int numCode = Integer.parseInt(infoValute.item(0).getTextContent());
                String charCodeString = infoValute.item(1).getTextContent();
                CurrencyCode charCode = CurrencyCode.valueOf(charCodeString);
                int nominal = Integer.parseInt(infoValute.item(2).getTextContent());
                String name = infoValute.item(3).getTextContent();
                double value = Double.valueOf(infoValute.item(4).getTextContent().replace(',', '.'));
                currencies.add(new Currency(dateInResponse, ID, numCode, charCode, nominal, name, value));
            }
            logger.debug("Число различных полученых валют из XML = " + currencies.size());
        } catch (ParserConfigurationException e) {
            throw new CurrencyRequestException(e);
        } catch (SAXException e) {
            throw new CurrencyRequestException(e);
        } catch (UnsupportedEncodingException e) {
            throw new CurrencyRequestException(e);
        } catch (MalformedURLException e) {
            throw new CurrencyRequestException(e);
        } catch (IOException e) {
            throw new CurrencyRequestException(e);
        }
        return currencies;
    }

}