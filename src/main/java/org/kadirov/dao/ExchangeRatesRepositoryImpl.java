package org.kadirov.dao;

import org.kadirov.dto.CrossExchangeRateDTO;
import org.kadirov.entity.ExchangeRateEntity;
import org.kadirov.mapper.entity.CrossExchangeRateEntityMapper;
import org.kadirov.mapper.entity.ViceVersaCrossExchangeEntityRateMapper;
import org.kadirov.dao.exception.DuplicateResourcesException;
import org.kadirov.datasource.CurrencyExchangerDataSource;
import org.kadirov.mapper.entity.ExchangeRateMapper;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRatesRepositoryImpl implements ExchangeRatesRepository {

    private final CurrencyExchangerDataSource dataSource;
    private final ExchangeRateMapper exchangeRateMapper;
    private final CrossExchangeRateEntityMapper crossExchangeRateEntityMapper;
    private final ViceVersaCrossExchangeEntityRateMapper viceVersaCrossExchangeEntityRateMapper;

    public ExchangeRatesRepositoryImpl(CurrencyExchangerDataSource dataSource) {
        this.dataSource = dataSource;
        this.exchangeRateMapper = new ExchangeRateMapper();
        this.crossExchangeRateEntityMapper = new CrossExchangeRateEntityMapper();
        this.viceVersaCrossExchangeEntityRateMapper = new ViceVersaCrossExchangeEntityRateMapper();
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
    public Optional<ExchangeRateEntity> selectByBaseCurrencyCodeAndTargetCurrencyCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
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

        return Optional.ofNullable(targetExchangeRate);
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

        if(resultSet.next())
            count = resultSet.getInt(1);

        return count > 0;
    }

    @Override
    @SuppressWarnings("all")
    public ExchangeRateEntity insert(final ExchangeRateEntity exchangeRateEntity) throws SQLException {
        Connection connection = dataSource.getConnection();

        PreparedStatement preparedStatement = connection.prepareStatement(String.format("""
                INSERT INTO exchangerates (base_currency_id, target_currency_id, rate)
                VALUES ('%s', '%s', '%s');
                """, exchangeRateEntity.getBaseCurrency().getId(), exchangeRateEntity.getTargetCurrency().getId(), exchangeRateEntity.getRate()));

        int executedUpdate = preparedStatement.executeUpdate();

        if(executedUpdate <= 0)
            return exchangeRateEntity;

        return selectByBaseCurrencyCodeAndTargetCurrencyCode(exchangeRateEntity.getBaseCurrency().getCode(), exchangeRateEntity.getTargetCurrency().getCode())
                .get();
    }

    @Override
    @SuppressWarnings("all")
    public ExchangeRateEntity updateRateByBaseCurrencyCodeAndTargetCurrencyCode(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) throws SQLException {
        Connection connection = dataSource.getConnection();

        String getBaseCurrencyCode = String.format("SELECT id FROM currencies WHERE code='%s'", baseCurrencyCode);
        String getTargetCurrencyCode = String.format("SELECT id FROM currencies WHERE code='%s'", targetCurrencyCode);

        PreparedStatement preparedStatement = connection.prepareStatement(String.format("""
                UPDATE exchangerates SET rate='%s' WHERE base_currency_id=(%s) AND target_currency_id=(%s);
                """, rate, getBaseCurrencyCode, getTargetCurrencyCode));

        int executedUpdate = preparedStatement.executeUpdate();

        if(executedUpdate <= 0)
            return null;

        return selectByBaseCurrencyCodeAndTargetCurrencyCode(baseCurrencyCode, targetCurrencyCode).get();
    }

    @Override
    public Optional<CrossExchangeRateDTO> selectByFirstTargetCurrencyCodeAndSecondTargetCurrencyCode(String firstTargetCurrencyCode, String secondTargetCurrencyCode) throws SQLException {
        Connection connection = dataSource.getConnection();
        String query = String.format("""
                SELECT r3.*, c.full_name AS b_full_name, c.code AS b_code, c.sign AS b_sign
                FROM (SELECT
                r1.id AS first_id, r1.target_currency_id AS first_target_currency_id, r1.full_name AS ftc_full_name, r1.code AS ftc_code, r1.sign AS ftc_sign, r1.rate AS first_rate,
                r2.id AS second_id, r2.target_currency_id AS second_target_currency_id, r2.full_name AS stc_full_name, r2.code AS stc_code, r2.sign AS stc_sign, r2.rate AS second_rate,
                r1.base_currency_id AS base_currency_id
                FROM (SELECT *
                FROM (SELECT er.*, c.full_name, c.code, c.sign FROM exchangerates er JOIN currencies c ON er.target_currency_id=c.id) res1
                WHERE res1.code="%s") r1
                JOIN (SELECT *
                FROM (SELECT er.*, c.full_name, c.code, c.sign FROM exchangerates er JOIN currencies c ON er.target_currency_id=c.id) res1
                WHERE res1.code="%s") r2
                ON r1.base_currency_id=r2.base_currency_id HAVING r1.target_currency_id!=r1.base_currency_id AND r2.target_currency_id!=r1.base_currency_id) r3
                JOIN currencies c
                ON r3.base_currency_id=c.id;
                """, firstTargetCurrencyCode, secondTargetCurrencyCode);

        PreparedStatement preparedStatement = connection.prepareStatement(query);

        ResultSet resultSet = preparedStatement.executeQuery();

        CrossExchangeRateDTO exchangeRateEntities = null;

        if(resultSet.next())
            exchangeRateEntities = crossExchangeRateEntityMapper.map(resultSet);

        return Optional.ofNullable(exchangeRateEntities);
    }

    @Override
    public Optional<CrossExchangeRateDTO> selectByFirstBaseCurrencyCodeAndSecondBaseCurrencyCode(String firstTargetCurrencyCode, String secondTargetCurrencyCode) throws SQLException {
        Connection connection = dataSource.getConnection();
        String query = String.format("""
                SELECT r3.*, c.full_name AS t_full_name, c.code AS t_code, c.sign AS t_sign
                FROM (SELECT
                r1.id AS first_id, r1.base_currency_id AS first_base_currency_id, r1.full_name AS fbc_full_name,
                r1.code AS fbc_code, r1.sign AS fbc_sign, r1.rate AS first_rate,
                r2.id AS second_id, r2.base_currency_id AS second_base_currency_id, r2.full_name AS sbc_full_name, 
                r2.code AS sbc_code, r2.sign AS sbc_sign, r2.rate AS second_rate,
                r1.target_currency_id AS target_currency_id
                FROM (SELECT * 
                FROM (SELECT er.*, c.full_name, c.code, c.sign FROM exchangerates er JOIN currencies c ON er.base_currency_id=c.id) res1 
                WHERE res1.code="%s") r1
                JOIN (SELECT * 
                FROM (SELECT er.*, c.full_name, c.code, c.sign FROM exchangerates er JOIN currencies c ON er.base_currency_id=c.id) res1 
                WHERE res1.code="%s") r2
                ON r1.target_currency_id=r2.target_currency_id HAVING r1.base_currency_id!=r1.target_currency_id AND r2.base_currency_id!=r1.target_currency_id) r3
                JOIN currencies c
                ON r3.target_currency_id=c.id;
                """, firstTargetCurrencyCode, secondTargetCurrencyCode);

        PreparedStatement preparedStatement = connection.prepareStatement(query);

        ResultSet resultSet = preparedStatement.executeQuery();

        CrossExchangeRateDTO exchangeRateEntities = null;

        if(resultSet.next())
            exchangeRateEntities = viceVersaCrossExchangeEntityRateMapper.map(resultSet);

        return Optional.ofNullable(exchangeRateEntities);
    }

    @Override
    public boolean existsDoubleByFirstTargetCurrencyCodeAndSecondTargetCurrencyCode(String firstTargetCurrencyCode, String secondTargetCurrencyCode) throws SQLException {
        Connection connection = dataSource.getConnection();

        PreparedStatement preparedStatement = connection.prepareStatement(String.format("""
                SELECT  *
                FROM (SELECT * FROM
                (SELECT er.*, c.full_name, c.code, c.sign FROM exchangerates er JOIN currencies c ON er.target_currency_id=c.id) res1 WHERE res1.code="%s") r1
                JOIN (SELECT * FROM
                (SELECT er.*, c.full_name, c.code, c.sign FROM exchangerates er JOIN currencies c ON er.target_currency_id=c.id) res1 WHERE res1.code="%s") r2
                ON r1.base_currency_id=r2.base_currency_id HAVING r1.target_currency_id!=r1.base_currency_id AND r2.target_currency_id!=r2.base_currency_id
                """,
                firstTargetCurrencyCode, secondTargetCurrencyCode));

        ResultSet resultSet = preparedStatement.executeQuery();
        int count = 0;

        if(resultSet.next())
            count = resultSet.getInt(1);

        return count > 0;
    }

    @Override
    public boolean existsDoubleByFirstBaseCurrencyCodeAndSecondBaseCurrencyCode(String firstTargetCurrencyCode, String secondTargetCurrencyCode) throws SQLException {
        Connection connection = dataSource.getConnection();

        PreparedStatement preparedStatement = connection.prepareStatement(String.format("""
                SELECT  *
                FROM (SELECT *
                FROM (SELECT er.*, c.full_name, c.code, c.sign FROM exchangerates er JOIN currencies c ON er.base_currency_id=c.id) res1 WHERE res1.code="%s") r1
                JOIN (SELECT *
                FROM (SELECT er.*, c.full_name, c.code, c.sign FROM exchangerates er JOIN currencies c ON er.base_currency_id=c.id) res1 WHERE res1.code="%s") r2
                ON r1.target_currency_id=r2.target_currency_id HAVING r1.target_currency_id!=r1.base_currency_id AND r2.target_currency_id!=r2.base_currency_id
                """, firstTargetCurrencyCode, secondTargetCurrencyCode));

        ResultSet resultSet = preparedStatement.executeQuery();
        int count = 0;

        if(resultSet.next())
            count = resultSet.getInt(1);

        return count > 0;
    }
}