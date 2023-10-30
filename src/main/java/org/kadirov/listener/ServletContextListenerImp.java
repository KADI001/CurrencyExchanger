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
import org.kadirov.mapper.model.CurrencyResponseMapper;
import org.kadirov.mapper.model.ExchangeRateResponseMapper;
import org.kadirov.service.ExchangeRateService;
import org.kadirov.service.ExchangeRateServiceImpl;
import org.kadirov.util.EnvUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Properties;

@WebListener
public class ServletContextListenerImp implements ServletContextListener {

    public static final String QUERY_SEPARATOR = "--QUERY";
    private static final Logger logger = LoggerFactory.getLogger(ServletContextListenerImp.class);

    private CurrencyRepository currencyRepository;
    private ExchangeRatesRepository exchangeRatesRepository;
    private ObjectMapper objectMapper;
    private CurrencyExchangerDataSource dataSource;
    private ExchangeRateService exchangeRateService;
    private CurrencyResponseMapper currencyResponseMapper;
    private ExchangeRateResponseMapper exchangeRateResponseMapper;

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

        if (!isRequiredTableCreated("currencies")) {
            if (!applySQLFile("initCurrenciesTable.sql"))
                return;
        }

        if (!isRequiredTableCreated("exchangerates")) {
            if (!applySQLFile("initExchangeRatesTable.sql"))
                return;
        }

        if (isTableEmpty("currencies")) {
            if (!applySQLFile("fillTableCurrenciesWithDefaultData.sql"))
                return;
        }

        if (isTableEmpty("exchangerates")) {
            if (!applySQLFile("fillTableExchangeRatesWithDefaultData.sql"))
                return;
        }

        currencyRepository = new CurrencyRepositoryImpl(dataSource);
        exchangeRatesRepository = new ExchangeRatesRepositoryImpl(dataSource);
        objectMapper = new ObjectMapper();
        exchangeRateService = new ExchangeRateServiceImpl(exchangeRatesRepository);
        currencyResponseMapper = new CurrencyResponseMapper();
        exchangeRateResponseMapper = new ExchangeRateResponseMapper(currencyResponseMapper);


        servletContext.setAttribute("currencyRepository", currencyRepository);
        servletContext.setAttribute("exchangeRatesRepository", exchangeRatesRepository);
        servletContext.setAttribute("objectMapper", objectMapper);
        servletContext.setAttribute("dbExchangeRateService", exchangeRateService);
        servletContext.setAttribute("currencyResponseMapper", currencyResponseMapper);
        servletContext.setAttribute("exchangeRateResponseMapper", exchangeRateResponseMapper);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            if (dataSource != null)
                dataSource.closeConnection();
        } catch (SQLException sqle) {
            logger.error("Error occurred during close data source connection", sqle);
        }
    }

    private boolean isTableEmpty(String table) {
        if (dataSource == null)
            throw new RuntimeException("Failed to check that if table is empty cause DataSource is not initialized");

        Connection connection = dataSource.getConnection();

        try {
            if (connection.isClosed())
                throw new RuntimeException("Failed to check that if table is empty cause DataSource.Connection is closed");

            PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM " + table + ";");
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count == 0;
            }
        } catch (SQLException e) {
            logger.error("Exception occurred during table empty check");
        }

        return false;
    }

    private boolean isRequiredTableCreated(String targetTable) {
        if (dataSource == null)
            throw new RuntimeException("Failed to check that if table exists cause DataSource is not initialized");

        Connection connection = dataSource.getConnection();

        try {
            if (connection.isClosed())
                throw new RuntimeException("Failed to check that if table exists cause DataSource.Connection is closed");

            PreparedStatement preparedStatement = connection.prepareStatement("show tables;");
            ResultSet resultSet = preparedStatement.executeQuery();
            String columnName = "Tables_in_currencyexchanger";

            while (resultSet.next()) {
                String tableName = resultSet.getString(columnName);

                if (tableName.equals(targetTable))
                    return true;
            }
        } catch (SQLException e) {
            logger.error("Exception occurred during table exists check");
        }

        return false;
    }

    private boolean applySQLFile(String fileName) {
        if (dataSource == null)
            return false;

        Connection connection = dataSource.getConnection();

        try (InputStream inputStream = getClass().getResourceAsStream("/" + fileName)) {
            if (connection.isClosed())
                return false;

            if (inputStream == null)
                return false;

            String initFile = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            String[] queries = initFile.split(QUERY_SEPARATOR);

            for (String query : queries) {
                logger.debug("Trying apply the query:\n" + query);
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.executeUpdate();
                logger.debug("The query has been successfully applied!");
            }

            return true;
        } catch (Exception e) {
            logger.debug("Failed to apply the query.", e);
            return false;
        }
    }

    private boolean initDataSource() {
        String dbUrl;
        String dbUsername;
        String dbPassword;
        String dbDriver;

        if (EnvUtil.exists("DB_URL") && EnvUtil.exists("DB_USERNAME")
                && EnvUtil.exists("DB_PASSWORD") && EnvUtil.exists("DB_DRIVER")) {

            dbUrl = System.getenv("DB_URL");
            dbUsername = System.getenv("DB_USERNAME");
            dbPassword = System.getenv("DB_PASSWORD");
            dbDriver = System.getenv("DB_DRIVER");

        } else {
            try (InputStream input = getClass().getResourceAsStream("/database.properties")) {
                if(input == null)
                    return false;

                Properties properties = new Properties();
                properties.load(input);

                dbUrl = properties.getProperty("url");
                dbUsername = properties.getProperty("username");
                dbPassword = properties.getProperty("password");
                dbDriver = properties.getProperty("driver");
            } catch (IOException e) {
                logger.error("Failed to load database.properties in Properties type");
                return false;
            }
        }

        if(dbUrl == null || dbUsername == null || dbPassword == null || dbDriver == null) {
            logger.error("Failed to find db connection params: url, username, password, driver");
            return false;
        }

        try {
            DriverManager.registerDriver((Driver) Class.forName(dbDriver).getConstructor().newInstance());
        } catch (Exception e) {
            logger.error("Failed register db driver by parameter driver", e);
            return false;
        }

        dataSource = new CurrencyExchangerDataSource(dbUrl, dbUsername, dbPassword);
        return true;
    }
}
