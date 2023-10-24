package org.kadirov.datasource;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class CurrencyExchangerDataSource implements DataSource{

    private Connection connection;

    private final String url;
    private final String username;
    private final String password;

    public CurrencyExchangerDataSource(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public void openConnection() throws SQLException {
        closeConnection();
        connection = DriverManager.getConnection(url, username, password);
    }

    @Override
    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed())
            connection.close();

        connection = null;
    }

    @Override
    public boolean isConnectionClosed() throws SQLException {
        return connection == null || connection.isClosed();
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getDataBaseName() {
        return null;
    }
}
