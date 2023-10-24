package org.kadirov.datasource;

import java.sql.Connection;
import java.sql.SQLException;

public interface DataSource {
    void openConnection() throws SQLException;
    void closeConnection() throws SQLException;

    boolean isConnectionClosed() throws SQLException;
    Connection getConnection();
    String getUsername();
    String getPassword();
    String getUrl();
    String getDataBaseName();
}
