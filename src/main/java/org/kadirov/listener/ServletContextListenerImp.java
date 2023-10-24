package org.kadirov.listener;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.kadirov.dao.CurrencyRepository;
import org.kadirov.dao.CurrencyRepositoryImpl;
import org.kadirov.dao.ExchangeRatesRepository;
import org.kadirov.dao.ExchangeRatesRepositoryImpl;
import org.kadirov.datasource.CurrencyExchangerDataSource;
import org.kadirov.json.JSONReader;
import org.kadirov.json.JSONReaderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.DriverManager;
import java.sql.SQLException;

@WebListener
public class ServletContextListenerImp implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(ServletContextListenerImp.class);

    private CurrencyRepository currencyRepository;
    private ExchangeRatesRepository exchangeRatesRepository;
    private JSONReader<JsonNode> jsonReader;
    private CurrencyExchangerDataSource dataSource;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();

        try {
            DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
        } catch (SQLException sqle) {
            logger.error("Ошибка при регистрации org.mysql.cj.jdbc.Driver", sqle);
            return;
        }

        dataSource = new CurrencyExchangerDataSource("jdbc:mysql://localhost:3306/currencyexchanger", "bestuser", "bestuser");

        try {
            dataSource.openConnection();
        } catch (SQLException sqle) {
            logger.error("Ошибка при открытии соединения с базой данных", sqle);
            return;
        }

        currencyRepository = new CurrencyRepositoryImpl(dataSource);
        exchangeRatesRepository = new ExchangeRatesRepositoryImpl(dataSource);
        jsonReader = new JSONReaderImpl();

        servletContext.setAttribute("currencyRepository", currencyRepository);
        servletContext.setAttribute("exchangeRatesRepository", exchangeRatesRepository);
        servletContext.setAttribute("jsonReader", jsonReader);
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
}
