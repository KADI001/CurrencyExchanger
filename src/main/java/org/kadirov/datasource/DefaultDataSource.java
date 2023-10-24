package org.kadirov.datasource;

import java.sql.Connection;
import java.sql.SQLException;

public class DefaultDataSource implements DataSource {

    @Override
    public void openConnection() throws SQLException {

    }

    @Override
    public void closeConnection() throws SQLException {

    }

    @Override
    public boolean isConnectionClosed() throws SQLException {
        return false;
    }

    @Override
    public Connection getConnection() {
        return null;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUrl() {
        return null;
    }

    @Override
    public String getDataBaseName() {
        return null;
    }
}
