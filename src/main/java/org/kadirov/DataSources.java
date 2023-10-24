package org.kadirov;

import org.kadirov.datasource.CurrencyExchangerDataSource;

import java.sql.SQLException;

public class DataSources {
    public static CurrencyExchangerDataSource currencyExchangerDataSource =
            new CurrencyExchangerDataSource("jdbc:postgresql://10.50.51.46:5332/currencyexchanger", "amigoscode", "password");
}
