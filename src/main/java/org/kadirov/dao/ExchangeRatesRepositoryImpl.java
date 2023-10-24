package org.kadirov.dao;

import org.kadirov.dao.entity.ExchangeRateEntity;
import org.kadirov.dao.exception.DuplicateResourcesException;
import org.kadirov.datasource.CurrencyExchangerDataSource;
import org.kadirov.dao.entity.mapper.ExchangeRateMapper;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExchangeRatesRepositoryImpl implements ExchangeRatesRepository {

    private final CurrencyExchangerDataSource dataSource;
    private final ExchangeRateMapper exchangeRateMapper;

    public ExchangeRatesRepositoryImpl(CurrencyExchangerDataSource dataSource) {
        this.dataSource = dataSource;
        this.exchangeRateMapper = new ExchangeRateMapper();
    }

    @Override
    public List<ExchangeRateEntity> selectAll() throws SQLException {
        List<ExchangeRateEntity> exchangeRateModels = new ArrayList<>();
        Connection connection = dataSource.getConnection();

        PreparedStatement preparedStatement = connection.prepareStatement("""
                SELECT
                cr.*,
                b_c.id AS 'b_c.id',
                b_c.code AS 'b_c.code',
                b_c.full_name AS 'b_c.full_name',
                b_c.sign AS 'b_c.sign',
                t_c.id AS 't_c.id',
                t_c.code AS 't_c.code',
                t_c.full_name AS 't_c.full_name',
                t_c.sign AS 't_c.sign'
                FROM exchangerates cr
                JOIN currencies b_c ON cr.base_currency_id=b_c.id
                JOIN currencies t_c ON cr.target_currency_id=t_c.id;
                """);

        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()){
            ExchangeRateEntity exchangeRateEntity = exchangeRateMapper.map(resultSet);
            exchangeRateModels.add(exchangeRateEntity);
        }

        return exchangeRateModels;
    }

    @Override
    public ExchangeRateEntity selectByBaseCurrencyCodeAndTargetCurrencyCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        ExchangeRateEntity targetExchangeRate = null;
        Connection connection = dataSource.getConnection();

        PreparedStatement preparedStatement = connection.prepareStatement(String.format("""
                SELECT
                cr.*,
                b_c.id AS 'b_c.id',
                b_c.code AS 'b_c.code',
                b_c.full_name AS 'b_c.full_name',
                b_c.sign AS 'b_c.sign',
                t_c.id AS 't_c.id',
                t_c.code AS 't_c.code',
                t_c.full_name AS 't_c.full_name',
                t_c.sign AS 't_c.sign'
                FROM exchangerates cr
                JOIN currencies b_c ON cr.base_currency_id=b_c.id
                JOIN currencies t_c ON cr.target_currency_id=t_c.id
                WHERE b_c.code='%s' AND t_c.code='%s';
                """, baseCurrencyCode, targetCurrencyCode));

        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()){
            if(targetExchangeRate != null)
                throw new DuplicateResourcesException(String.format("Found more than one entry 'exchangeRate' with base_currency_code %s and target_currency_code %s",
                        baseCurrencyCode, targetCurrencyCode));

            targetExchangeRate = exchangeRateMapper.map(resultSet);
        }

        return targetExchangeRate;
    }

    @Override
    public boolean existsByBaseCurrencyCodeAndTargetCurrencyCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        Connection connection = dataSource.getConnection();

        PreparedStatement preparedStatement = connection.prepareStatement(String.format("""
                SELECT
                COUNT(1)
                FROM exchangerates cr
                JOIN currencies b_c ON cr.base_currency_id=b_c.id
                JOIN currencies t_c ON cr.target_currency_id=t_c.id
                WHERE b_c.code='%s' AND t_c.code='%s';
                """, baseCurrencyCode, targetCurrencyCode));

        ResultSet resultSet = preparedStatement.executeQuery();
        int count = 0;

        if(resultSet.next()){
            count = resultSet.getInt(1);
        }

        return count > 0;
    }

    @Override
    public ExchangeRateEntity insert(final ExchangeRateEntity exchangeRateEntity) throws SQLException {
        Connection connection = dataSource.getConnection();

        PreparedStatement preparedStatement = connection.prepareStatement(String.format("""
                INSERT INTO exchangerates (base_currency_id, target_currency_id, rate)
                VALUES ('%s', '%s', '%s');
                """, exchangeRateEntity.getBaseCurrency().getId(), exchangeRateEntity.getTargetCurrency().getId(), exchangeRateEntity.getRate()));

        int executedUpdate = preparedStatement.executeUpdate();

        if(executedUpdate <= 0){
            return null;
        }

        return selectByBaseCurrencyCodeAndTargetCurrencyCode(exchangeRateEntity.getBaseCurrency().getCode(), exchangeRateEntity.getTargetCurrency().getCode());
    }

    @Override
    public ExchangeRateEntity updateRateByBaseCurrencyCodeAndTargetCurrencyCode(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) throws SQLException {
        Connection connection = dataSource.getConnection();

        String getBaseCurrencyCode = String.format("SELECT id FROM currencies WHERE code='%s'", baseCurrencyCode);
        String getTargetCurrencyCode = String.format("SELECT id FROM currencies WHERE code='%s'", targetCurrencyCode);

        PreparedStatement preparedStatement = connection.prepareStatement(String.format("""
                UPDATE exchangerates SET rate='%s' WHERE base_currency_id=(%s) AND target_currency_id=(%s);
                """, rate, getBaseCurrencyCode, getTargetCurrencyCode));

        int executedUpdate = preparedStatement.executeUpdate();

        if(executedUpdate <= 0){
            return null;
        }

        return selectByBaseCurrencyCodeAndTargetCurrencyCode(baseCurrencyCode, targetCurrencyCode);
    }
}
