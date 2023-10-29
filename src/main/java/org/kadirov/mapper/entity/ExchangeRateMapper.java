package org.kadirov.mapper.entity;

import org.kadirov.entity.CurrencyEntity;
import org.kadirov.entity.ExchangeRateEntity;
import org.kadirov.mapper.Mapper;
import org.kadirov.mapper.exception.MappingException;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExchangeRateMapper implements Mapper<ResultSet, ExchangeRateEntity> {
    @Override
    public ExchangeRateEntity map(ResultSet resultSet) throws MappingException {
        int id;
        ExchangeRateEntity exchangeRateEntity;

        try {
            id = resultSet.getInt("id");

            int bcId = resultSet.getInt("b_c.id");
            String bcCode = resultSet.getString("b_c.code");
            String bcFullName = resultSet.getString("b_c.full_name");
            String bcSign = resultSet.getString("b_c.sign");

            int tcId = resultSet.getInt("t_c.id");
            String tcCode = resultSet.getString("t_c.code");
            String tcFullName = resultSet.getString("t_c.full_name");
            String tcSign = resultSet.getString("t_c.sign");

            BigDecimal rate = resultSet.getBigDecimal("rate");

            CurrencyEntity baseCurrency = new CurrencyEntity(bcId, bcCode, bcFullName, bcSign);
            CurrencyEntity targetCurrency = new CurrencyEntity(tcId, tcCode, tcFullName, tcSign);
            exchangeRateEntity = new ExchangeRateEntity(id, baseCurrency, targetCurrency, rate);
        } catch (SQLException sqle) {
            throw new MappingException(sqle);
        }

        return exchangeRateEntity;
    }
}
