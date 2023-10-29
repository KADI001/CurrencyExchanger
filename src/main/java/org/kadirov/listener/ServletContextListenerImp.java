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

        //todo: Разделить проверку и создание для каждой таблицы
        if(!isRequiredTablesCreated("currencies", "exchangerates")){
            if (!applySQLFile("init.sql"))
                return;
        }

        if(isTableEmpty("currencies")){
            if (!applySQLFile("fillTableCurrenciesWithDefaultData.sql"))
                return;
        }

        if(isTableEmpty("exchangerates")){
            if (!applySQLFile("fillTableExchangeRatesWithDefaultData.sql"))
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

    private boolean isTableEmpty(String table) {
        if(dataSource == null)
            throw new RuntimeException("Failed to check that if table is empty cause DataSource is not initialized");

        Connection connection = dataSource.getConnection();

        try {
            if (connection.isClosed())
                throw new RuntimeException("Failed to check that if table is empty cause DataSource.Connection is closed");

            PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM " + table + ";");
            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()){
                int count = resultSet.getInt(1);
                return count == 0;
            }
        } catch (SQLException e) {
            logger.error("Exception occurred during table empty check");
        }

        return false;
    }

    private boolean isRequiredTablesCreated(String... tables) {
        if(dataSource == null)
            throw new RuntimeException("Failed to check that if table exists cause DataSource is not initialized");

        Connection connection = dataSource.getConnection();

        int amount = tables.length;
        int found = 0;

        try {
            if (connection.isClosed())
                throw new RuntimeException("Failed to check that if table exists cause DataSource.Connection is closed");

            PreparedStatement preparedStatement = connection.prepareStatement("show tables;");
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()){
                if(amount == found)
                    break;

                String tableName = resultSet.getString("Tables_in_currencyexchanger");

                for (String table : tables) {
                    if(tableName.equals(table)){
                        found++;
                        break;
                    }
                }
            }

            return amount == found;
        } catch (SQLException e) {
            logger.error("Exception occurred during table exists check");
            return false;
        }
    }

    private boolean applySQLFile(String fileName){
        if(dataSource == null)
            return false;

        Connection connection = dataSource.getConnection();

        try(InputStream inputStream = getClass().getResourceAsStream("/" + fileName)){
            if (connection.isClosed())
                return false;

            if(inputStream == null)
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
        }catch (Exception e){
            logger.debug("Failed to apply the query.", e);
            return false;
        }
    }

    private boolean initDataSource() {
        //todo: Брать свойства из переменных окружения, а если их там нет, то из database.properties файл
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
