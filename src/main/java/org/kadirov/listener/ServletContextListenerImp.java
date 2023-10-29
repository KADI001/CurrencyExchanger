package org.kadirov.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.kadirov.dao.CurrencyRepository;
import org.kadirov.dao.CurrencyRepositoryImpl;
import org.kadirov.dao.ExchangeRatesRepository;
import org.kadirov.dao.ExchangeRatesRepositoryImpl;
import org.kadirov.datasource.CurrencyExchangerDataSource;
import org.kadirov.service.CurrencyService;
import org.kadirov.service.CurrencyServiceImpl;
import org.kadirov.service.ExchangeRateService;
import org.kadirov.service.ExchangeRateServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@WebListener
public class ServletContextListenerImp implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(ServletContextListenerImp.class);

    private CurrencyRepository currencyRepository;
    private ExchangeRatesRepository exchangeRatesRepository;
    private ObjectMapper objectMapper;
    private CurrencyExchangerDataSource dataSource;
    private ExchangeRateService exchangeRateService;
    private CurrencyService currencyService;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();

        if (!initDataSource())
            return;

        try {
            dataSource.openConnection();
        } catch (SQLException sqle) {
            logger.error("Ошибка при открытии соединения с базой данных", sqle);
            return;
        }

        currencyRepository = new CurrencyRepositoryImpl(dataSource);
        exchangeRatesRepository = new ExchangeRatesRepositoryImpl(dataSource);
        objectMapper = new ObjectMapper();
        exchangeRateService = new ExchangeRateServiceImpl(exchangeRatesRepository);
        currencyService = new CurrencyServiceImpl(currencyRepository);

        servletContext.setAttribute("currencyRepository", currencyRepository);
        servletContext.setAttribute("exchangeRatesRepository", exchangeRatesRepository);
        servletContext.setAttribute("objectMapper", objectMapper);
        servletContext.setAttribute("dbExchangeRateService", exchangeRateService);
        servletContext.setAttribute("dbCurrencyService", currencyService);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            if(dataSource != null)
                dataSource.closeConnection();
        } catch (SQLException sqle) {
            logger.error("Error occurred during close data source connection", sqle);
        }
    }

    private boolean initDataSource() {
        try(InputStream input = getClass().getResourceAsStream("/database.properties")){
            Properties properties = new Properties();
            properties.load(input);

            DriverManager.registerDriver((Driver) Class.forName(properties.getProperty("driver")).getConstructor().newInstance());

            dataSource = new CurrencyExchangerDataSource(
                    properties.getProperty("url"),
                    properties.getProperty("username"),
                    properties.getProperty("password"));
            return true;
        } catch (Exception e) {
            logger.error("Failed to init DataSource", e);
        }

        return false;
    }
}
