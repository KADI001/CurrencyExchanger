package org.kadirov.dao;

import org.kadirov.dao.entity.CurrencyEntity;
import org.kadirov.dao.exception.DuplicateResourcesException;
import org.kadirov.dao.exception.RecordsWithEqualsIdException;
import org.kadirov.datasource.CurrencyExchangerDataSource;
import org.kadirov.dao.entity.mapper.CurrencyMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CurrencyRepositoryImpl implements CurrencyRepository {

    private final CurrencyExchangerDataSource dataSource;
    private final CurrencyMapper currencyMapper;

    public CurrencyRepositoryImpl(CurrencyExchangerDataSource dataSource) {
        this.dataSource = dataSource;
        this.currencyMapper = new CurrencyMapper();
    }

    @Override
    public List<CurrencyEntity> selectAll() throws SQLException {
        List<CurrencyEntity> currencies = new ArrayList<>();
        Connection connection = dataSource.getConnection();

        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM currencies;");
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()){
            CurrencyEntity currencyModel = currencyMapper.map(resultSet);
            currencies.add(currencyModel);
        }

        return currencies;
    }

    @Override
    public CurrencyEntity selectById(int id) throws SQLException {
        CurrencyEntity targetCurrencyModel = null;
        Connection connection = dataSource.getConnection();

        PreparedStatement preparedStatement =
                connection.prepareStatement(String.format("SELECT * FROM currencies WHERE id='%s';", id));
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()){
            if(targetCurrencyModel != null)
                throw new RecordsWithEqualsIdException("Found more than one record 'currency' with id: " + id);

            targetCurrencyModel = currencyMapper.map(resultSet);
        }

        return targetCurrencyModel;
    }

    @Override
    public CurrencyEntity selectByCode(String code) throws SQLException {
        CurrencyEntity targetCurrencyModel = null;
        Connection connection = dataSource.getConnection();

        PreparedStatement preparedStatement =
                connection.prepareStatement(String.format("SELECT * FROM currencies WHERE code='%s';", code));
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()){
            if(targetCurrencyModel != null)
                throw new DuplicateResourcesException("Found more than one record 'currency' with code: " + code);

            targetCurrencyModel = currencyMapper.map(resultSet);
        }

        return targetCurrencyModel;
    }

    @Override
    public CurrencyEntity insert(final CurrencyEntity currencyModel) throws SQLException {
        Connection connection = dataSource.getConnection();

        PreparedStatement preparedStatement =
                connection.prepareStatement(
                        String.format("INSERT INTO currencies (code, full_name, sign) VALUES ('%s','%s','%s')",
                                currencyModel.getCode(),
                                currencyModel.getFullName(),
                                currencyModel.getSign()));

        int executedUpdate = preparedStatement.executeUpdate();

        if(executedUpdate <= 0){
            return null;
        }

        return selectByCode(currencyModel.getCode());
    }

    @Override
    public boolean existsByCode(String code) throws SQLException {
        Connection connection = dataSource.getConnection();

        PreparedStatement preparedStatement =
                connection.prepareStatement(String.format("SELECT count(*) FROM currencies WHERE code='%s';", code));
        ResultSet resultSet = preparedStatement.executeQuery();
        int count = 0;

        if(resultSet.next()){
            count = resultSet.getInt(1);
        }

        return count > 0;
    }
}
