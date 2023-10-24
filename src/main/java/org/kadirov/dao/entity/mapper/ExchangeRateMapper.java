package org.kadirov.dao.entity.mapper;

import org.kadirov.dao.entity.CurrencyEntity;
import org.kadirov.dao.entity.ExchangeRateEntity;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExchangeRateMapper {
    public ExchangeRateEntity map(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");

        int bcId = resultSet.getInt("b_c.id");
        String bcCode = resultSet.getString("b_c.code");
        String bcFullName = resultSet.getString("b_c.full_name");
        String bcSign = resultSet.getString("b_c.sign");

        int tcId = resultSet.getInt("t_c.id");
        String tcCode = resultSet.getString("t_c.code");
        String tcFullName = resultSet.getString("t_c.full_name");
        String tcSign = resultSet.getString("t_c.sign");

        BigDecimal rate = resultSet.getBigDecimal("rate");

        return new ExchangeRateEntity(id, new CurrencyEntity(bcId, bcCode, bcFullName, bcSign), new CurrencyEntity(tcId, tcCode, tcFullName, tcSign), rate);
    }
}
