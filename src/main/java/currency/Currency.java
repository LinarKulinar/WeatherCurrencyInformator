package currency;

import java.time.LocalDate;

/**
 * Данный класс описывает сущность "Валюта"
 */
public class Currency implements Comparable<Currency> {

    private LocalDate date;
    private String ID;
    private int numCode;
    private CurrencyCode charCode;
    private int nominal;
    private String name;
    private double value;

    public Currency(LocalDate date, String ID, int numCode, CurrencyCode charCode, int nominal, String name, double value) {
        this.date = date;
        this.ID = ID;
        this.numCode = numCode;
        this.charCode = charCode;
        this.nominal = nominal;
        this.name = name;
        this.value = value;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getID() {
        return ID;
    }

    public int getNumCode() {
        return numCode;
    }

    public CurrencyCode getCharCode() {
        return charCode;
    }

    public int getNominal() {
        return nominal;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }


    @Override
    public String toString() {
        return "Currency{" +
                "date=" + date +
                ", ID='" + ID + '\'' +
                ", numCode=" + numCode +
                ", charCode=" + charCode +
                ", nominal=" + nominal +
                ", name='" + name + '\'' +
                ", value=" + value +
                '}';
    }

    public String toHumanReadableString() {
        int rubles = (int) this.getValue();
        int copecks = (int) Math.round((this.getValue() - rubles) * 100);
        return String.format("%d руб %d коп за %d %s.", rubles, copecks, nominal, name);
    }

    @Override
    public int compareTo(Currency o) {
        return Double.compare(value, o.getValue());
    }
}
